package com.telehealth.portal.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "doctor_leaves")
public class DoctorLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;

    @Column(name = "leave_date", nullable = false)
    private LocalDate leaveDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DoctorProfile getDoctor() { return doctor; }
    public void setDoctor(DoctorProfile doctor) { this.doctor = doctor; }

    public LocalDate getLeaveDate() { return leaveDate; }
    public void setLeaveDate(LocalDate leaveDate) { this.leaveDate = leaveDate; }
}