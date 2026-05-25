package com.telehealth.portal.service;

import com.telehealth.portal.entity.Prescription;

public interface PrescriptionService {
    // Saves a prescription and marks the corresponding appointment as COMPLETED
    Prescription addPrescription(Prescription prescription, Long appointmentId);
}