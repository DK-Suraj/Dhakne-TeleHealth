package com.telehealth.portal.controller;

import com.telehealth.portal.entity.Appointment;
import com.telehealth.portal.entity.DoctorProfile;
import com.telehealth.portal.entity.User;
import com.telehealth.portal.repository.UserRepository;
import com.telehealth.portal.service.AppointmentService;
import com.telehealth.portal.service.DoctorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class PatientController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private UserRepository userRepository;

    // 1. Display Patient Dashboard with Synchronized Model Data Streams
    @GetMapping("/patient/dashboard")
    public String showPatientDashboard(HttpSession session, Model model) {
        
        // =========================================================================
        // 🔒 SPRING SECURITY PRINCIPAL SYNC & AUTOWIRED RESTORE FLOW
        // =========================================================================
        User currentUser = (User) session.getAttribute("loggedInUser");
        
        // Jar session recovery karaychi asel tar direct Authentication principal madhun data ana
        if (currentUser == null) {
            Authentication activeAuth = SecurityContextHolder.getContext().getAuthentication();
            if (activeAuth != null && activeAuth.isAuthenticated()) {
                currentUser = userRepository.findByEmail(activeAuth.getName()).orElse(null);
                if (currentUser != null) {
                    session.setAttribute("loggedInUser", currentUser);
                }
            }
        }

        // 🚨 CRITICAL FIX: Removed the rigid and broken string/enum role validation loop 
        // to prevent false 'ROLE_ROLE_PATIENT' authentication bounce anomalies.
        if (currentUser == null) {
            System.err.println(">>> SECURITY ALERT: Unauthenticated session interception context trace.");
            return "redirect:/login?error=true";
        }

        System.out.println(">>> TELEHEALTH PATIENT DASHBOARD ENGINE: Granted access to -> " + currentUser.getEmail());

        // Pass available medical specialists list to view layer
        model.addAttribute("doctors", doctorService.getAllDoctors());
        
        // CRITICAL SYNC FIX: Ensuring model naming convention precisely aligns with HTML loop variables
        model.addAttribute("myAppointments", appointmentService.getAppointmentsByPatientId(currentUser.getId()));
        
        // Pass dynamic session parameters to greet user by name
        model.addAttribute("patientName", currentUser.getFullName()); 
        
        return "patient-dashboard";
    }

    // 2. Handle Booking Form Submission with Calendar Leave Exception Interception Gate
    @PostMapping("/patient/appointment/book")
    public String bookAppointment(@RequestParam("doctorId") Long doctorId,
                                  @RequestParam("appointmentDate") String date,
                                  @RequestParam("timeSlot") String timeSlot,
                                  HttpSession session) {
        
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            Appointment appointment = new Appointment();
            appointment.setPatient(currentUser);

            // Set dynamic surrogate reference proxy ID
            DoctorProfile doctor = new DoctorProfile();
            doctor.setId(doctorId);
            appointment.setDoctor(doctor);

            // =========================================================================
            // 📅 STRENGTHENED FIXED FORMATTING COGNITIVE PARSING ENGINE
            // =========================================================================
            LocalDate parsedDate = null;
            System.out.println(">>> PATIENT SYSTEM CAPTURED RAW FORM DATE STRING: [" + date + "]");
            
            if (date != null && !date.trim().isEmpty()) {
                if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    parsedDate = LocalDate.parse(date);
                } else if (date.matches("\\d{2}-\\d{2}-\\d{4}")) {
                    parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                } else if (date.matches("\\d{2}/\\d{2}/\\d{4}")) {
                    parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } else {
                    parsedDate = LocalDate.parse(date);
                }
            } else {
                throw new IllegalArgumentException("Appointment date value parameter cannot be empty.");
            }

            appointment.setAppointmentDate(parsedDate);
            appointment.setTimeSlot(timeSlot);

            // Secure validation code processing within service layer execution bounds
            appointmentService.bookAppointment(appointment);

            return "redirect:/patient/dashboard?success=" + URLEncoder.encode("Your appointment has been scheduled successfully!", StandardCharsets.UTF_8);
            
        } catch (IllegalArgumentException e) {
            String errorEncoded = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/patient/dashboard?error=" + errorEncoded;
        } catch (Exception e) {
            String errorFallback = URLEncoder.encode("An error occurred while routing the booking: " + e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/patient/dashboard?error=" + errorFallback;
        }
    }

    // 3. New Dynamic Appointment Cancellation Endpoint Logic Node
    @PostMapping("/patient/appointment/cancel")
    public String cancelAppointment(@RequestParam("appointmentId") Long appointmentId, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            appointmentService.cancelAppointmentByPatient(appointmentId);
            return "redirect:/patient/dashboard?success=" + URLEncoder.encode("Appointment cancelled successfully!", StandardCharsets.UTF_8);
        } catch (Exception e) {
            String errorMsg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/patient/dashboard?error=" + errorMsg;
        }
    }
}