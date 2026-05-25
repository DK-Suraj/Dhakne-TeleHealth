package com.telehealth.portal.repository;

import com.telehealth.portal.entity.Prescription;
import com.telehealth.portal.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    // Fetch prescription details using an appointment reference
    Optional<Prescription> findByAppointment(Appointment appointment);
}