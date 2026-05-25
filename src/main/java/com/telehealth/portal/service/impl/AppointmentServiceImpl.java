package com.telehealth.portal.service.impl;

import com.telehealth.portal.entity.Appointment;
import com.telehealth.portal.entity.AppointmentStatus;
import com.telehealth.portal.entity.Prescription;
import com.telehealth.portal.entity.DoctorProfile;
import com.telehealth.portal.repository.AppointmentRepository;
import com.telehealth.portal.repository.DoctorLeaveRepository;
import com.telehealth.portal.repository.DoctorProfileRepository;
import com.telehealth.portal.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorLeaveRepository doctorLeaveRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    // 1. Transactional Registration Engine with Integrated Load Balancer and Leave Interception Gates
    @Override
    @Transactional
    public Appointment bookAppointment(Appointment appointment) {
        Long targetDoctorProfileId = null;

        if (appointment.getDoctor() != null && appointment.getDoctor().getId() != null) {
            targetDoctorProfileId = appointment.getDoctor().getId();
        }

        // =========================================================================
        // 🚨 RELATIONAL MAPPING INTEGRITY CHECK & PROXY RECOVERY
        // =========================================================================
        if (targetDoctorProfileId != null) {
            final Long currentIdToCheck = targetDoctorProfileId;
            boolean existsDirectly = doctorProfileRepository.findById(currentIdToCheck).isPresent();
            
            if (!existsDirectly) {
                DoctorProfile matchedProfile = doctorProfileRepository.findAll().stream()
                        .filter(p -> p.getUser() != null && p.getUser().getId().equals(currentIdToCheck))
                        .findFirst()
                        .orElse(null);
                
                if (matchedProfile != null) {
                    targetDoctorProfileId = matchedProfile.getId();
                    appointment.setDoctor(matchedProfile); 
                }
            }
        }

        // =========================================================================
        // 🛑 CONSTRAINT GATE 1: DOCTOR AVAILABILITY & LEAVE MANAGEMENT SYSTEM
        // =========================================================================
        if (targetDoctorProfileId != null && appointment.getAppointmentDate() != null) {
            boolean isDoctorOnLeave = doctorLeaveRepository.existsByDoctorIdAndLeaveDate(targetDoctorProfileId, appointment.getAppointmentDate());
            if (isDoctorOnLeave) {
                throw new IllegalArgumentException("Doctor is not available on this date!");
            }
        }

        // =========================================================================
        // 📊 CONSTRAINT GATE 2: CONCURRENCY CONSTRAINT & HIGH-TRAFFIC CAPACITY CONTROL
        // =========================================================================
        long activeBookingsCount = appointmentRepository.countAppointmentsForSlot(
                targetDoctorProfileId, 
                appointment.getAppointmentDate(), 
                appointment.getTimeSlot()
        );

        final int CRITICAL_SLOT_CAPACITY_LIMIT = 5; // Configured Threshold Boundary for Slot Load Balancing
        if (activeBookingsCount >= CRITICAL_SLOT_CAPACITY_LIMIT) {
            throw new IllegalArgumentException("This time slot (" + appointment.getTimeSlot() + ") is fully booked! Please select another slot or date.");
        }

        // =========================================================================
        // 🎫 LINEAR LIVE QUEUE POSITION ALLOCATION (AUTO-INCREMENT SEQUENCING)
        // =========================================================================
        int computedTokenSequence = (int) (activeBookingsCount + 1);
        appointment.setTokenNumber(computedTokenSequence);
        
        System.out.println(">>> TELEHEALTH SYSTEM DISPATCHER: Assigned Queue Ticket #" + computedTokenSequence + " inside Time Window Slot [" + appointment.getTimeSlot() + "]");

        appointment.setStatus(AppointmentStatus.PENDING);
        return appointmentRepository.save(appointment);
    }

    @Override
    public List<Appointment> getAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Override
    public List<Appointment> getAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Override
    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Target appointment instance context not found."));
    }

    @Override
    @Transactional
    public void completeAppointmentWithPrescription(Long appointmentId, String symptoms, String medicines) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment context target not found."));

        Prescription prescription = new Prescription();
        prescription.setSymptoms(symptoms);
        prescription.setMedicines(medicines);
        prescription.setAppointment(appointment); 

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setPrescription(prescription); 

        appointmentRepository.save(appointment);
    }

    @Override
    public long getPendingCountByDoctorId(Long doctorId) {
        return appointmentRepository.countPendingByDoctorId(doctorId);
    }

    @Override
    public long getCompletedCountByDoctorId(Long doctorId) {
        return appointmentRepository.countCompletedByDoctorId(doctorId);
    }

    @Override
    public double getTotalEarningsByDoctorId(Long doctorId) {
        return appointmentRepository.calculateTotalEarningsByDoctorId(doctorId);
    }

    // =========================================================================
    // ⚙️ AUTOMATED QUEUE RE-INDEXING ENGINE (LIVE TOKEN SHIFTING WORKFLOW)
    // =========================================================================
    @Override
    @Transactional
    public void cancelAppointmentByPatient(Long appointmentId) {
        // Fetch the target appointment record
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment record not found."));

        // State Validation: Enforce strict check to permit cancellation only for PENDING requests
        if (appointment.getStatus() == null || !appointment.getStatus().name().equals("PENDING")) {
            throw new IllegalStateException("Completed or processed sessions cannot be cancelled.");
        }

        // Cache parameters needed to adjust the trailing queue after deletion
        Long doctorId = appointment.getDoctor().getId();
        java.time.LocalDate appointmentDate = appointment.getAppointmentDate();
        String timeSlot = appointment.getTimeSlot();
        Integer cancelledTokenNumber = appointment.getTokenNumber();

        // Perform the deletion operation from the persistence context
        appointmentRepository.delete(appointment);
        System.out.println(">>> WIPE RECOGNIZED: Cancelled Appointment ID " + appointmentId + " (Token #" + cancelledTokenNumber + ")");

        // Dynamically shift and re-index trailing tokens to maintain sequence continuity
        if (cancelledTokenNumber != null) {
            List<Appointment> trailingQueue = appointmentRepository.findByDoctorId(doctorId).stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.PENDING 
                            && a.getAppointmentDate().equals(appointmentDate) 
                            && a.getTimeSlot().equals(timeSlot)
                            && a.getTokenNumber() > cancelledTokenNumber)
                    .collect(Collectors.toList());

            // Decouple index positions down by one structural rank allocation step
            for (Appointment remainingAppt : trailingQueue) {
                int shiftedToken = remainingAppt.getTokenNumber() - 1;
                remainingAppt.setTokenNumber(shiftedToken);
                appointmentRepository.save(remainingAppt);
            }
            
            System.out.println(">>> QUEUE OPTIMIZATION SUCCESSFUL: Re-indexed " + trailingQueue.size() + " subsequent patient slots for [" + timeSlot + "].");
        }
    }
}