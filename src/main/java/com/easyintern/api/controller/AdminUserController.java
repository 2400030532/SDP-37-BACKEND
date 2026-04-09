package com.easyintern.api.controller;

import com.easyintern.api.dto.AuthDtos;
import com.easyintern.api.dto.AuthDtos.EmployerAdminResponse;
import com.easyintern.api.dto.AuthDtos.EmployerUpdateRequest;
import com.easyintern.api.model.User;
import com.easyintern.api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174", "http://localhost:5175"}, allowCredentials = "true")
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

    @GetMapping("/employers")
    public ResponseEntity<List<EmployerAdminResponse>> getEmployers() {
        return ResponseEntity.ok(userService.getEmployersByAdmin());
    }

    @PutMapping("/employers/{employerId}")
    public ResponseEntity<EmployerAdminResponse> updateEmployer(
            @PathVariable Long employerId,
            @RequestBody EmployerUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.updateEmployerByAdmin(employerId, request));
    }

    @DeleteMapping("/employers/{employerId}")
    public ResponseEntity<Void> deleteEmployer(@PathVariable Long employerId) {
        userService.deleteEmployerByAdmin(employerId);
        return ResponseEntity.noContent().build();
    }
}
