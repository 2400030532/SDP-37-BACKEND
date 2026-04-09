package com.easyintern.api.service;

import com.easyintern.api.dto.ApplicationDtos;
import com.easyintern.api.model.Employer;
import com.easyintern.api.model.User;
import com.easyintern.api.repository.EmployerRepository;
import com.easyintern.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;
    private final ApplicationService applicationService;

    public ProfileService(UserRepository userRepository, EmployerRepository employerRepository, ApplicationService applicationService) {
        this.userRepository = userRepository;
        this.employerRepository = employerRepository;
        this.applicationService = applicationService;
    }

    public ApplicationDtos.ProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        if ("employer".equalsIgnoreCase(user.getRole())) {
            Employer employer = employerRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Employer profile not found"));

            return new ApplicationDtos.ProfileResponse(
                    user.getId(),
                    employer.getFullName(),
                    employer.getEmail(),
                    employer.getPhone(),
                    null,
                    employer.getLocation(),
                    user.getRole(),
                    null,
                    0L
            );
        }

        return new ApplicationDtos.ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getPen(),
                user.getLocation(),
                user.getRole(),
                user.getResumeUrl(),
                applicationService.countByStudent(userId)
        );
    }
}
