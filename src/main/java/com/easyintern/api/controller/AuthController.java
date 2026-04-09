package com.easyintern.api.controller;

import com.easyintern.api.dto.AuthDtos.AuthRequest;
import com.easyintern.api.dto.AuthDtos.AuthResponse;
import com.easyintern.api.dto.AuthDtos.PasswordResetRequest;
import com.easyintern.api.dto.AuthDtos.AccountResponse;
import com.easyintern.api.model.User;
import com.easyintern.api.security.JwtService;
import com.easyintern.api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174", "http://localhost:5175"}, allowCredentials = "true")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody AuthRequest request) {
        User created = userService.registerUser(request);
        String token = jwtService.generateToken(
                userService.toUserDetails(created),
                Map.of("role", created.getRole(), "userId", created.getId())
        );
        AuthResponse response = new AuthResponse(
                created.getId(),
                created.getFullName(),
                created.getEmail(),
                created.getRole(),
                token,
                "Account created successfully"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        String loginIdentifier = request.getLoginId() != null && !request.getLoginId().isBlank()
            ? request.getLoginId()
            : request.getEmail();

        if (loginIdentifier == null || loginIdentifier.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "User ID or email is required"));
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Password is required"));
        }

        User user = userService.authenticate(loginIdentifier, request.getPassword(), request.getRole());
        String token = jwtService.generateToken(
                userService.toUserDetails(user),
                Map.of("role", user.getRole(), "userId", user.getId())
        );
        AuthResponse response = new AuthResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                token,
                "Login successful"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordResetRequest request, @RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            userService.changePassword(email, request.getCurrentPassword(), request.getNewPassword(), request.getConfirmPassword());
            return ResponseEntity.ok(new AccountResponse("Password changed successfully", "success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AccountResponse(e.getMessage(), "error"));
        }
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            userService.deleteAccount(email);
            return ResponseEntity.ok(new AccountResponse("Account deleted successfully", "success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AccountResponse(e.getMessage(), "error"));
        }
    }

    @PostMapping("/send-login-notification")
    public ResponseEntity<?> sendLoginNotification(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractEmail(token.replace("Bearer ", ""));
            userService.sendLoginNotification(email);
            return ResponseEntity.ok(new AccountResponse("Login notification sent to your email", "success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AccountResponse(e.getMessage(), "error"));
        }
    }
}
