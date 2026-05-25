package com.telehealth.portal.service.impl;

import com.telehealth.portal.entity.Appointment;
import com.telehealth.portal.entity.AppointmentStatus;
import com.telehealth.portal.entity.Prescription;
import com.telehealth.portal.repository.AppointmentRepository;
import com.telehealth.portal.repository.PrescriptionRepository;
import com.telehealth.portal.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Override
    public Prescription addPrescription(Prescription prescription, Long appointmentId) {
        // Retrieve the corresponding medical appointment or throw an exception if invalid
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + appointmentId));

        // Automatically mark the medical appointment status lifecycle loop to COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        // Bind the relational data references and append the generation timestamp
        prescription.setAppointment(appointment);
        prescription.setGeneratedAt(LocalDateTime.now());

        // Persist the complete prescription model stream into the database
        return prescriptionRepository.save(prescription);
    }
}