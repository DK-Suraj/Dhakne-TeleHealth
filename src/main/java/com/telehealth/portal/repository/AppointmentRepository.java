package com.telehealth.portal.repository;

import com.telehealth.portal.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);
    
    List<Appointment> findByDoctorId(Long doctorId);

    // 🎫 REAL-TIME TRAFFIC OPTIMIZER: Calculates atomic allocation scale size for isolated doctor timeframes
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :appointmentDate AND a.timeSlot = :timeSlot")
    long countAppointmentsForSlot(@Param("doctorId") Long doctorId, 
                                  @Param("appointmentDate") LocalDate appointmentDate, 
                                  @Param("timeSlot") String timeSlot);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.status = com.telehealth.portal.entity.AppointmentStatus.PENDING")
    long countPendingByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.status = com.telehealth.portal.entity.AppointmentStatus.COMPLETED")
    long countCompletedByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT COALESCE(SUM(d.consultationFee), 0.0) FROM Appointment a JOIN a.doctor d WHERE d.id = :doctorId AND a.status = com.telehealth.portal.entity.AppointmentStatus.COMPLETED")
    double calculateTotalEarningsByDoctorId(@Param("doctorId") Long doctorId);
}