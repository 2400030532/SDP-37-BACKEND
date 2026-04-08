package com.easyintern.api.service;

import com.easyintern.api.dto.ApplicationDtos;
import com.easyintern.api.model.User;
import com.easyintern.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final ApplicationService applicationService;

    public ProfileService(UserRepository userRepository, ApplicationService applicationService) {
        this.userRepository = userRepository;
        this.applicationService = applicationService;
    }

    public ApplicationDtos.ProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

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
