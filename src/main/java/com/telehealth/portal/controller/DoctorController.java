package com.telehealth.portal.controller;

import com.telehealth.portal.entity.Appointment;
import com.telehealth.portal.entity.DoctorProfile;
import com.telehealth.portal.entity.DoctorLeave;
import com.telehealth.portal.entity.User;
import com.telehealth.portal.service.AppointmentService;
import com.telehealth.portal.service.DoctorService;
import com.telehealth.portal.repository.DoctorLeaveRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorLeaveRepository doctorLeaveRepository;

    // 1. Display Doctor Dashboard with Live Analytics Engines & Configured Leaves
    @GetMapping("/doctor/dashboard")
    public String showDoctorDashboard(HttpSession session, Model model) {
        // Security Check: Verify user session validity and validate ROLE_DOCTOR authority
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null || !currentUser.getRole().name().equals("ROLE_DOCTOR")) {
            return "redirect:/login?error=Please log in to access your portal.";
        }

        // Pass doctor's user metadata to the model attributes
        model.addAttribute("doctorName", currentUser.getFullName());
        model.addAttribute("doctorUserId", currentUser.getId());

        // Fetch associated doctor profile metadata from the database
        DoctorProfile profile = doctorService.getProfileByUserId(currentUser.getId());
        
        if (profile != null) {
            // Bind active appointments linked to this specific practitioner profile
            model.addAttribute("appointments", appointmentService.getAppointmentsByDoctorId(profile.getId()));
            model.addAttribute("profile", profile);
            
            // --- INJECTING RECONFIGURED ANALYTICS TO MODEL CONTEXT ---
            model.addAttribute("pendingCount", appointmentService.getPendingCountByDoctorId(profile.getId()));
            model.addAttribute("completedCount", appointmentService.getCompletedCountByDoctorId(profile.getId()));
            model.addAttribute("totalEarnings", appointmentService.getTotalEarningsByDoctorId(profile.getId()));
            
            // Pass registered future leave dates ledger to the workspace
            List<DoctorLeave> registeredLeaves = doctorLeaveRepository.findByDoctorIdOrderByLeaveDateAsc(profile.getId());
            model.addAttribute("myLeaves", registeredLeaves);
        } else {
            // Fallback for fresh doctor profiles without configured settings
            model.addAttribute("appointments", Collections.emptyList());
            model.addAttribute("profile", new DoctorProfile());
            model.addAttribute("myLeaves", Collections.emptyList());
            
            model.addAttribute("pendingCount", 0);
            model.addAttribute("completedCount", 0);
            model.addAttribute("totalEarnings", 0.0);
        }

        return "doctor-dashboard";
    }

    // 2. Handle Profile Saves and Custom Metadata Updates
    @PostMapping("/doctor/profile/save")
    public String saveDoctorProfile(@ModelAttribute("profile") DoctorProfile inputProfile, 
                                    @RequestParam("userId") Long userId) {
        
        // Retrieve existing profile context, initialize a fresh entity if not found
        DoctorProfile existingProfile = doctorService.getProfileByUserId(userId);
        if (existingProfile == null) {
            existingProfile = new DoctorProfile();
        }
        
        // Create a relational user proxy reference to map foreign key dependency
        User user = new User();
        user.setId(userId);
        
        existingProfile.setUser(user);
        existingProfile.setSpecialization(inputProfile.getSpecialization());
        existingProfile.setExperienceYears(inputProfile.getExperienceYears());
        existingProfile.setConsultationFee(inputProfile.getConsultationFee());
        existingProfile.setAvailabilityHours(inputProfile.getAvailabilityHours());
        
        doctorService.saveProfile(existingProfile);
        return "redirect:/doctor/dashboard?success=Profile updated successfully!";
    }

    // 3. New Leave Scheduler Pipeline Node Activation
    @PostMapping("/doctor/leave/save")
    public String saveDoctorLeave(@RequestParam("leaveDate") String date, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null || !currentUser.getRole().name().equals("ROLE_DOCTOR")) {
            return "redirect:/login";
        }

        DoctorProfile profile = doctorService.getProfileByUserId(currentUser.getId());
        if (profile != null) {
            DoctorLeave doctorLeave = new DoctorLeave();
            doctorLeave.setDoctor(profile);
            doctorLeave.setLeaveDate(LocalDate.parse(date));
            
            doctorLeaveRepository.save(doctorLeave);
        }

        return "redirect:/doctor/dashboard?success=Leave configuration saved securely on ledger matrix!";
    }

    // 4. Launch Dedicated Prescription Console View with Dynamic Patient History Logs
    @GetMapping("/doctor/appointment/prescribe/{id}")
    public String showPrescriptionForm(@PathVariable("id") Long appointmentId, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null || !currentUser.getRole().name().equals("ROLE_DOCTOR")) {
            return "redirect:/login";
        }
        
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        model.addAttribute("appointment", appointment);
        
        if (appointment != null && appointment.getPatient() != null) {
            List<Appointment> historyList = appointmentService.getAppointmentsByPatientId(appointment.getPatient().getId());
            model.addAttribute("pastAppointments", historyList);
        } else {
            model.addAttribute("pastAppointments", Collections.emptyList());
        }
        
        return "prescribe";
    }

    // 5. Process Submitted Medical Prescriptions and Complete the Session
    @PostMapping("/doctor/appointment/prescribe")
    public String submitPrescription(@RequestParam("appointmentId") Long appointmentId,
                                     @RequestParam("symptoms") String symptoms,
                                     @RequestParam("medicines") String medicines,
                                     HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null || !currentUser.getRole().name().equals("ROLE_DOCTOR")) {
            return "redirect:/login";
        }

        appointmentService.completeAppointmentWithPrescription(appointmentId, symptoms, medicines);
        return "redirect:/doctor/dashboard?success=Prescription submitted successfully!";
    }
}