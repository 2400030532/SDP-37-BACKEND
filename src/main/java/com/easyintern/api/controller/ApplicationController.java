package com.easyintern.api.controller;

import com.easyintern.api.dto.ApplicationDtos;
import com.easyintern.api.model.ApplicationTask;
import com.easyintern.api.model.InternshipApplication;
import com.easyintern.api.model.User;
import com.easyintern.api.service.ApplicationService;
import com.easyintern.api.service.ProfileService;
import com.easyintern.api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5175", "http://localhost:5180"})
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserService userService;
    private final ProfileService profileService;

    public ApplicationController(ApplicationService applicationService, UserService userService, ProfileService profileService) {
        this.applicationService = applicationService;
        this.userService = userService;
        this.profileService = profileService;
    }

    @PostMapping("/applications/apply")
    public ResponseEntity<ApplicationDtos.ApplicationResponse> apply(@RequestBody ApplicationDtos.ApplyRequest request,
                                                                     Authentication authentication) {
        User user = currentUser(authentication);
        InternshipApplication app = applicationService.apply(user.getId(), request.getInternshipId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(app));
    }

    @GetMapping("/applications/me")
    public ResponseEntity<List<ApplicationDtos.ApplicationResponse>> myApplications(Authentication authentication) {
        User user = currentUser(authentication);
        List<ApplicationDtos.ApplicationResponse> data = applicationService.getByStudent(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(data);
    }

    @PostMapping("/applications/{applicationId}/resume")
    public ResponseEntity<ApplicationDtos.ApplicationResponse> uploadResume(@PathVariable Long applicationId,
                                                                             @RequestParam("file") MultipartFile file,
                                                                             Authentication authentication) throws IOException {
        User user = currentUser(authentication);
        String resumeUrl = saveResume(file, user.getId());
        InternshipApplication app = applicationService.updateResume(applicationId, user.getId(), resumeUrl);
        return ResponseEntity.ok(toResponse(app));
    }

    @PutMapping("/applications/tasks/{taskId}/complete")
    public ResponseEntity<Void> completeTask(@PathVariable Long taskId, Authentication authentication) {
        User user = currentUser(authentication);
        applicationService.markTaskComplete(taskId, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile/me")
    public ResponseEntity<ApplicationDtos.ProfileResponse> myProfile(Authentication authentication) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(profileService.getMyProfile(user.getId()));
    }

    @GetMapping("/admin/applications")
    public ResponseEntity<List<ApplicationDtos.ApplicationResponse>> allApplications() {
        List<ApplicationDtos.ApplicationResponse> data = applicationService.getAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(data);
    }

    @GetMapping("/employer/applications")
    public ResponseEntity<List<ApplicationDtos.ApplicationResponse>> employerApplications() {
        List<ApplicationDtos.ApplicationResponse> data = applicationService.getAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(data);
    }

    @PutMapping("/employer/applications/{applicationId}/resume-decision")
    public ResponseEntity<ApplicationDtos.ApplicationResponse> decideResume(@PathVariable Long applicationId,
                                                                             @RequestBody ApplicationDtos.ResumeDecisionRequest request) {
        InternshipApplication app = applicationService.updateResumeDecision(applicationId, request.getResumeRound());
        return ResponseEntity.ok(toResponse(app));
    }

    @PutMapping("/admin/applications/{applicationId}/rounds")
    public ResponseEntity<ApplicationDtos.ApplicationResponse> updateRounds(@PathVariable Long applicationId,
                                                                             @RequestBody ApplicationDtos.RoundUpdateRequest request) {
        InternshipApplication app = applicationService.updateRounds(applicationId, request);
        return ResponseEntity.ok(toResponse(app));
    }

    @PostMapping("/admin/applications/{applicationId}/tasks")
    public ResponseEntity<ApplicationDtos.TaskResponse> addTask(@PathVariable Long applicationId,
                                                                 @RequestBody ApplicationDtos.TaskCreateRequest request) {
        ApplicationTask task = applicationService.addTask(applicationId, request.getTitle(), request.getDueDate());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApplicationDtos.TaskResponse(task.getId(), task.getTitle(), task.getCompleted(), task.getDueDate()));
    }

    private ApplicationDtos.ApplicationResponse toResponse(InternshipApplication app) {
        List<ApplicationDtos.TaskResponse> tasks = app.getTasks().stream()
                .map(t -> new ApplicationDtos.TaskResponse(t.getId(), t.getTitle(), t.getCompleted(), t.getDueDate()))
                .collect(Collectors.toList());

        int progress = 0;
        if (app.getResumeRound().name().equals("ALLOWED")) progress += 34;
        if (app.getTechnicalRound().name().equals("ALLOWED")) progress += 33;
        if (app.getHrRound().name().equals("ALLOWED")) progress += 33;

        return new ApplicationDtos.ApplicationResponse(
                app.getId(),
                app.getInternship().getId(),
                app.getInternship().getTitle(),
                app.getInternship().getCompany(),
                app.getStatus().name(),
                app.getResumeRound().name(),
                app.getTechnicalRound().name(),
                app.getHrRound().name(),
                app.getResumeUrl(),
                app.getAppliedAt(),
                progress,
                tasks
        );
    }

    private User currentUser(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }

    private String saveResume(MultipartFile file, Long userId) throws IOException {
        String original = file.getOriginalFilename() == null ? "resume.pdf" : file.getOriginalFilename();
        String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".pdf";
        String fileName = "resume_" + userId + "_" + UUID.randomUUID() + extension;

        Path uploadDir = Paths.get("uploads");
        Files.createDirectories(uploadDir);
        Path target = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + fileName;
    }
}
