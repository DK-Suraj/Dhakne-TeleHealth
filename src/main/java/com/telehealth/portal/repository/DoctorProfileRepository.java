package com.telehealth.portal.repository;

import com.telehealth.portal.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    // Search doctors by their medical specialization (e.g., Dentist, Cardiologist)
    List<DoctorProfile> findBySpecializationContainingIgnoreCase(String specialization);
}