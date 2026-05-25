package com.telehealth.portal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "doctor_profiles")
public class DoctorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user; 

    @Column(nullable = false)
    private String specialization;

    @Column(name = "experience_years", nullable = false)
    private int experienceYears;

    @Column(name = "consultation_fee", nullable = false)
    private double consultationFee;

    @Column(name = "availability_hours")
    private String availabilityHours;

    // --- No-Args Constructor ---
    public DoctorProfile() {}

    // --- All-Args Constructor ---
    public DoctorProfile(Long id, User user, String specialization, int experienceYears, double consultationFee, String availabilityHours) {
        this.id = id;
        this.user = user;
        this.specialization = specialization;
        this.experienceYears = experienceYears;
        this.consultationFee = consultationFee;
        this.availabilityHours = availabilityHours;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    public String getAvailabilityHours() { return availabilityHours; }
    public void setAvailabilityHours(String availabilityHours) { this.availabilityHours = availabilityHours; }
}