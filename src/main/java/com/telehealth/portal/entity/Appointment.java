package com.telehealth.portal.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient; 

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor; 

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "time_slot", nullable = false)
    private String timeSlot; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status; 

    // Dynamic high-performance ticket positioning field for clinical queues
    @Column(name = "token_number")
    private Integer tokenNumber;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Prescription prescription;

    // --- Constructors ---
    public Appointment() {}

    public Appointment(Long id, User patient, DoctorProfile doctor, LocalDate appointmentDate, String timeSlot, AppointmentStatus status, Integer tokenNumber) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentDate = appointmentDate;
        this.timeSlot = timeSlot;
        this.status = status;
        this.tokenNumber = tokenNumber;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getPatient() { return patient; }
    public void setPatient(User patient) { this.patient = patient; }

    public DoctorProfile getDoctor() { return doctor; }
    public void setDoctor(DoctorProfile doctor) { this.doctor = doctor; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public Integer getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(Integer tokenNumber) { this.tokenNumber = tokenNumber; }

    public Prescription getPrescription() { return prescription; }
    public void setPrescription(Prescription prescription) { this.prescription = prescription; }
}