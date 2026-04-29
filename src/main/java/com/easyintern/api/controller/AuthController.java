package com.easyintern.api.controller;

import com.easyintern.api.dto.AuthDtos.AuthRequest;
import com.easyintern.api.dto.AuthDtos.AuthResponse;
import com.easyintern.api.model.User;
import com.easyintern.api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5175", "http://localhost:5180"})
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody AuthRequest request) {
        User created = userService.registerUser(request);
        AuthResponse response = new AuthResponse(created.getId(), created.getFullName(), created.getEmail(), created.getRole(), "Account created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        User user = userService.authenticate(request.getEmail(), request.getPassword(), request.getRole());
        AuthResponse response = new AuthResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), "Login successful");
        return ResponseEntity.ok(response);
    }
}
