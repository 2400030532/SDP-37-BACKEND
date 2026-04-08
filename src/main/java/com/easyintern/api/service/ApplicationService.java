package com.easyintern.api.service;

import com.easyintern.api.dto.ApplicationDtos;
import com.easyintern.api.model.*;
import com.easyintern.api.repository.ApplicationTaskRepository;
import com.easyintern.api.repository.InternshipApplicationRepository;
import com.easyintern.api.repository.InternshipRepository;
import com.easyintern.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ApplicationService {

    private final InternshipApplicationRepository applicationRepository;
    private final ApplicationTaskRepository taskRepository;
    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ApplicationService(InternshipApplicationRepository applicationRepository,
                              ApplicationTaskRepository taskRepository,
                              InternshipRepository internshipRepository,
                              UserRepository userRepository,
                              NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.taskRepository = taskRepository;
        this.internshipRepository = internshipRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public InternshipApplication apply(Long userId, Long internshipId) {
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Student not found"));
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Internship not found"));

        if (applicationRepository.findByStudentIdAndInternshipId(userId, internshipId).isPresent()) {
            throw new ResponseStatusException(BAD_REQUEST, "Already applied to this internship");
        }

        InternshipApplication app = new InternshipApplication();
        app.setStudent(student);
        app.setInternship(internship);
        app.setStatus(ApplicationStatus.APPLIED);
        app.setResumeRound(ApplicationRoundStatus.PENDING);
        app.setTechnicalRound(ApplicationRoundStatus.PENDING);
        app.setHrRound(ApplicationRoundStatus.PENDING);

        InternshipApplication saved = applicationRepository.save(app);

        addTask(saved.getId(), "Upload resume for shortlist", null);
        addTask(saved.getId(), "Prepare for technical round", null);
        addTask(saved.getId(), "Attend HR discussion", null);

        return saved;
    }

    public List<InternshipApplication> getByStudent(Long userId) {
        return applicationRepository.findByStudentId(userId);
    }

    public List<InternshipApplication> getAll() {
        return applicationRepository.findAll();
    }

    @Transactional
    public InternshipApplication updateRounds(Long applicationId, ApplicationDtos.RoundUpdateRequest request) {
        InternshipApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Application not found"));

        if (request.getResumeRound() != null) {
            app.setResumeRound(request.getResumeRound());
            notificationService.sendRoundUpdateMail(
                    app.getStudent().getEmail(),
                    app.getInternship().getTitle(),
                    "Resume Shortlisting",
                    request.getResumeRound().name()
            );
        }
        if (request.getTechnicalRound() != null) {
            app.setTechnicalRound(request.getTechnicalRound());
            notificationService.sendRoundUpdateMail(
                    app.getStudent().getEmail(),
                    app.getInternship().getTitle(),
                    "Technical",
                    request.getTechnicalRound().name()
            );
        }
        if (request.getHrRound() != null) {
            app.setHrRound(request.getHrRound());
            notificationService.sendRoundUpdateMail(
                    app.getStudent().getEmail(),
                    app.getInternship().getTitle(),
                    "HR",
                    request.getHrRound().name()
            );
        }

        ApplicationRoundStatus rr = app.getResumeRound();
        ApplicationRoundStatus tr = app.getTechnicalRound();
        ApplicationRoundStatus hr = app.getHrRound();

        if (rr == ApplicationRoundStatus.REJECTED || tr == ApplicationRoundStatus.REJECTED || hr == ApplicationRoundStatus.REJECTED) {
            app.setStatus(ApplicationStatus.REJECTED);
        } else if (rr == ApplicationRoundStatus.ALLOWED && tr == ApplicationRoundStatus.ALLOWED && hr == ApplicationRoundStatus.ALLOWED) {
            app.setStatus(ApplicationStatus.COMPLETED);
        } else if (rr == ApplicationRoundStatus.ALLOWED) {
            app.setStatus(ApplicationStatus.IN_PROGRESS);
        } else {
            app.setStatus(ApplicationStatus.APPLIED);
        }

        return applicationRepository.save(app);
    }

    @Transactional
    public InternshipApplication updateResumeDecision(Long applicationId, ApplicationRoundStatus resumeRoundDecision) {
        InternshipApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Application not found"));

        if (resumeRoundDecision == null ||
                (resumeRoundDecision != ApplicationRoundStatus.ALLOWED && resumeRoundDecision != ApplicationRoundStatus.REJECTED)) {
            throw new ResponseStatusException(BAD_REQUEST, "Resume decision must be ALLOWED or REJECTED");
        }

        app.setResumeRound(resumeRoundDecision);

        if (resumeRoundDecision == ApplicationRoundStatus.REJECTED) {
            app.setStatus(ApplicationStatus.REJECTED);
            app.setTechnicalRound(ApplicationRoundStatus.PENDING);
            app.setHrRound(ApplicationRoundStatus.PENDING);
        } else {
            app.setStatus(ApplicationStatus.IN_PROGRESS);
            if (app.getTechnicalRound() == null) {
                app.setTechnicalRound(ApplicationRoundStatus.PENDING);
            }
            if (app.getHrRound() == null) {
                app.setHrRound(ApplicationRoundStatus.PENDING);
            }
        }

        notificationService.sendRoundUpdateMail(
                app.getStudent().getEmail(),
                app.getInternship().getTitle(),
                "Resume Shortlisting",
                resumeRoundDecision.name()
        );

        return applicationRepository.save(app);
    }

    @Transactional
    public ApplicationTask addTask(Long applicationId, String title, java.time.LocalDateTime dueDate) {
        InternshipApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Application not found"));

        ApplicationTask task = new ApplicationTask();
        task.setApplication(app);
        task.setTitle(title);
        task.setDueDate(dueDate);
        task.setCompleted(false);

        ApplicationTask saved = taskRepository.save(task);
        notificationService.sendTaskAssignedMail(app.getStudent().getEmail(), app.getInternship().getTitle(), title);
        return saved;
    }

    @Transactional
    public ApplicationTask markTaskComplete(Long taskId, Long userId) {
        ApplicationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Task not found"));

        if (!task.getApplication().getStudent().getId().equals(userId)) {
            throw new ResponseStatusException(BAD_REQUEST, "Task does not belong to this student");
        }

        task.setCompleted(true);
        return taskRepository.save(task);
    }

    @Transactional
    public InternshipApplication updateResume(Long applicationId, Long userId, String resumeUrl) {
        InternshipApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Application not found"));

        if (!app.getStudent().getId().equals(userId)) {
            throw new ResponseStatusException(BAD_REQUEST, "Application does not belong to this student");
        }

        app.setResumeUrl(resumeUrl);
        app.getStudent().setResumeUrl(resumeUrl);
        userRepository.save(app.getStudent());
        return applicationRepository.save(app);
    }

    public long countByStudent(Long userId) {
        return applicationRepository.findByStudentId(userId).size();
    }
}
