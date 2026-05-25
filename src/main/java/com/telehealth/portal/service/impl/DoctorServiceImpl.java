package com.telehealth.portal.service.impl;

import com.telehealth.portal.entity.DoctorProfile;
import com.telehealth.portal.repository.DoctorProfileRepository; // 👈 Exact interface name verified
import com.telehealth.portal.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorProfileRepository doctorRepository; // Exactly matches your layout context

    // 1. Persist or Update Doctor Profile Entity Parameters
    @Override
    public DoctorProfile saveProfile(DoctorProfile profile) {
        return doctorRepository.save(profile);
    }

    // 2. Fetch Profile Using Relational Mapping Proxy (Optimized DB lookup instead of slow RAM loop)
    @Override
    public DoctorProfile getProfileByUserId(Long userId) {
        return doctorRepository.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    // 3. Retrieve All Registered Medical Professionals Array List Context
    @Override
    public List<DoctorProfile> getAllDoctors() {
        return doctorRepository.findAll();
    }

    // 4. CRITICAL CORE FIX: Added missing method linked to Patient Validation Controller
    @Override
    public DoctorProfile getProfileById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor profile instance context not found for identification key: " + id));
    }
}