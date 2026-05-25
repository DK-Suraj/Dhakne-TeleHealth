package com.telehealth.portal.service;

import com.telehealth.portal.entity.DoctorProfile;
import java.util.List;

public interface DoctorService {
    // Save or update a doctor's professional profile details
    DoctorProfile saveProfile(DoctorProfile profile);
    
    // Find a profile using the associated User ID
    DoctorProfile getProfileByUserId(Long userId);
    
    // List all available doctors for patients to see
    List<DoctorProfile> getAllDoctors();
    DoctorProfile getProfileById(Long id);
}