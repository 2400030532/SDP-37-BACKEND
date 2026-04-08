package com.easyintern.api.controller;

import com.easyintern.api.dto.AuthDtos;
import com.easyintern.api.model.User;
import com.easyintern.api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5175", "http://localhost:5180"})
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/employer")
    public ResponseEntity<AuthDtos.AuthResponse> createEmployer(@RequestBody AuthDtos.EmployerCreateRequest request) {
        User employer = userService.createEmployerByAdmin(
                request.getFullName(),
                request.getPhone(),
                request.getEmail(),
                request.getPassword(),
                request.getLocation()
        );

        AuthDtos.AuthResponse response = new AuthDtos.AuthResponse(
                employer.getId(),
                employer.getFullName(),
                employer.getEmail(),
                employer.getRole(),
                null,
                "Employer account created successfully"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
