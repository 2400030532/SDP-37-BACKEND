package com.easyintern.api.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "internship_applications")
public class InternshipApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "resume_round", nullable = false)
    private ApplicationRoundStatus resumeRound;

    @Enumerated(EnumType.STRING)
    @Column(name = "technical_round", nullable = false)
    private ApplicationRoundStatus technicalRound;

    @Enumerated(EnumType.STRING)
    @Column(name = "hr_round", nullable = false)
    private ApplicationRoundStatus hrRound;

    @Column(name = "resume_url")
    private String resumeUrl;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationTask> tasks = new ArrayList<>();

    @PrePersist
    protected void prePersist() {
        this.appliedAt = LocalDateTime.now();
        if (status == null) {
            status = ApplicationStatus.APPLIED;
        }
        if (resumeRound == null) {
            resumeRound = ApplicationRoundStatus.PENDING;
        }
        if (technicalRound == null) {
            technicalRound = ApplicationRoundStatus.PENDING;
        }
        if (hrRound == null) {
            hrRound = ApplicationRoundStatus.PENDING;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Internship getInternship() {
        return internship;
    }

    public void setInternship(Internship internship) {
        this.internship = internship;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public ApplicationRoundStatus getResumeRound() {
        return resumeRound;
    }

    public void setResumeRound(ApplicationRoundStatus resumeRound) {
        this.resumeRound = resumeRound;
    }

    public ApplicationRoundStatus getTechnicalRound() {
        return technicalRound;
    }

    public void setTechnicalRound(ApplicationRoundStatus technicalRound) {
        this.technicalRound = technicalRound;
    }

    public ApplicationRoundStatus getHrRound() {
        return hrRound;
    }

    public void setHrRound(ApplicationRoundStatus hrRound) {
        this.hrRound = hrRound;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public List<ApplicationTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<ApplicationTask> tasks) {
        this.tasks = tasks;
    }
}
