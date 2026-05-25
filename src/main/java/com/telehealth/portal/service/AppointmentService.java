package com.telehealth.portal.service;

import com.telehealth.portal.entity.Appointment;
import java.util.List;

public interface AppointmentService {

    Appointment bookAppointment(Appointment appointment);
    
    List<Appointment> getAppointmentsByPatientId(Long patientId);
    
    List<Appointment> getAppointmentsByDoctorId(Long doctorId);
    
    Appointment getAppointmentById(Long appointmentId);
    
    void completeAppointmentWithPrescription(Long appointmentId, String symptoms, String medicines);

    // Dynamic Database Counter metrics Contracts
    long getPendingCountByDoctorId(Long doctorId);
    
    long getCompletedCountByDoctorId(Long doctorId);
    
    double getTotalEarningsByDoctorId(Long doctorId);
    void cancelAppointmentByPatient(Long appointmentId);
}