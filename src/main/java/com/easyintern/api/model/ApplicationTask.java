package com.easyintern.api.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_tasks")
public class ApplicationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private InternshipApplication application;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Boolean completed;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @PrePersist
    protected void prePersist() {
        if (completed == null) {
            completed = false;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InternshipApplication getApplication() {
        return application;
    }

    public void setApplication(InternshipApplication application) {
        this.application = application;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
}
