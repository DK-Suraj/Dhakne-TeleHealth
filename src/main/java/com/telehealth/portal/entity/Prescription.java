package com.telehealth.portal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment; 

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String symptoms; 

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String medicines; 

    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt; 

    // --- Automatically Assign Timestamp Before Database Insert ---
    @PrePersist
    protected void onCreate() {
        this.generatedAt = LocalDateTime.now();
    }

    // --- No-Args Constructor ---
    public Prescription() {}

    // --- All-Args Constructor ---
    public Prescription(Long id, Appointment appointment, String symptoms, String medicines, LocalDateTime generatedAt) {
        this.id = id;
        this.appointment = appointment;
        this.symptoms = symptoms;
        this.medicines = medicines;
        this.generatedAt = generatedAt;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getMedicines() { return medicines; }
    public void setMedicines(String medicines) { this.medicines = medicines; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}