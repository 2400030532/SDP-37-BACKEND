package com.easyintern.api.dto;

import com.easyintern.api.model.ApplicationRoundStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ApplicationDtos {

    private ApplicationDtos() {
    }

    public static class ApplyRequest {
        private Long internshipId;

        public Long getInternshipId() {
            return internshipId;
        }

        public void setInternshipId(Long internshipId) {
            this.internshipId = internshipId;
        }
    }

    public static class RoundUpdateRequest {
        private ApplicationRoundStatus resumeRound;
        private ApplicationRoundStatus technicalRound;
        private ApplicationRoundStatus hrRound;

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
    }

    public static class TaskCreateRequest {
        private String title;
        private LocalDateTime dueDate;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public LocalDateTime getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDateTime dueDate) {
            this.dueDate = dueDate;
        }
    }

    public static class ResumeDecisionRequest {
        private ApplicationRoundStatus resumeRound;

        public ApplicationRoundStatus getResumeRound() {
            return resumeRound;
        }

        public void setResumeRound(ApplicationRoundStatus resumeRound) {
            this.resumeRound = resumeRound;
        }
    }

    public static class TaskResponse {
        private Long id;
        private String title;
        private Boolean completed;
        private LocalDateTime dueDate;

        public TaskResponse(Long id, String title, Boolean completed, LocalDateTime dueDate) {
            this.id = id;
            this.title = title;
            this.completed = completed;
            this.dueDate = dueDate;
        }

        public Long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Boolean getCompleted() {
            return completed;
        }

        public LocalDateTime getDueDate() {
            return dueDate;
        }
    }

    public static class ApplicationResponse {
        private Long id;
        private Long internshipId;
        private String internshipTitle;
        private String company;
        private String status;
        private String resumeRound;
        private String technicalRound;
        private String hrRound;
        private String resumeUrl;
        private LocalDateTime appliedAt;
        private Integer progress;
        private List<TaskResponse> tasks;

        public ApplicationResponse(Long id, Long internshipId, String internshipTitle, String company, String status,
                                   String resumeRound, String technicalRound, String hrRound, String resumeUrl,
                                   LocalDateTime appliedAt, Integer progress, List<TaskResponse> tasks) {
            this.id = id;
            this.internshipId = internshipId;
            this.internshipTitle = internshipTitle;
            this.company = company;
            this.status = status;
            this.resumeRound = resumeRound;
            this.technicalRound = technicalRound;
            this.hrRound = hrRound;
            this.resumeUrl = resumeUrl;
            this.appliedAt = appliedAt;
            this.progress = progress;
            this.tasks = tasks;
        }

        public Long getId() {
            return id;
        }

        public Long getInternshipId() {
            return internshipId;
        }

        public String getInternshipTitle() {
            return internshipTitle;
        }

        public String getCompany() {
            return company;
        }

        public String getStatus() {
            return status;
        }

        public String getResumeRound() {
            return resumeRound;
        }

        public String getTechnicalRound() {
            return technicalRound;
        }

        public String getHrRound() {
            return hrRound;
        }

        public String getResumeUrl() {
            return resumeUrl;
        }

        public LocalDateTime getAppliedAt() {
            return appliedAt;
        }

        public Integer getProgress() {
            return progress;
        }

        public List<TaskResponse> getTasks() {
            return tasks;
        }
    }

    public static class ProfileResponse {
        private Long userId;
        private String fullName;
        private String email;
        private String phone;
        private String pen;
        private String location;
        private String role;
        private String resumeUrl;
        private Long internshipsCount;

        public ProfileResponse(Long userId, String fullName, String email, String phone, String pen, String location,
                               String role, String resumeUrl, Long internshipsCount) {
            this.userId = userId;
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.pen = pen;
            this.location = location;
            this.role = role;
            this.resumeUrl = resumeUrl;
            this.internshipsCount = internshipsCount;
        }

        public Long getUserId() {
            return userId;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public String getPen() {
            return pen;
        }

        public String getLocation() {
            return location;
        }

        public String getRole() {
            return role;
        }

        public String getResumeUrl() {
            return resumeUrl;
        }

        public Long getInternshipsCount() {
            return internshipsCount;
        }
    }
}
