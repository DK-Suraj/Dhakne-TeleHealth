package com.telehealth.portal.repository;

import com.telehealth.portal.entity.DoctorLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorLeaveRepository extends JpaRepository<DoctorLeave, Long> {
    
    // Check if a doctor has a configured leave on a specific target date
    boolean existsByDoctorIdAndLeaveDate(Long doctorId, LocalDate leaveDate);
    
    // Fetch all future leaves of a specific doctor to display on their dashboard
    List<DoctorLeave> findByDoctorIdOrderByLeaveDateAsc(Long doctorId);
}