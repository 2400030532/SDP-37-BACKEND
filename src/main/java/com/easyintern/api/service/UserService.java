package com.easyintern.api.service;

import com.easyintern.api.dto.AuthDtos.AuthRequest;
import com.easyintern.api.dto.AuthDtos.EmployerAdminResponse;
import com.easyintern.api.dto.AuthDtos.EmployerUpdateRequest;
import com.easyintern.api.model.Employer;
import com.easyintern.api.model.User;
import com.easyintern.api.repository.EmployerRepository;
import com.easyintern.api.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@gmail.com";
    private static final String LEGACY_ADMIN_EMAIL = "admin@easyintern.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin@123A";

    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, EmployerRepository employerRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employerRepository = employerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(AuthRequest request) {
        String normalizedRole = request.getRole() == null ? "student" : request.getRole().toLowerCase();
        if (!"student".equals(normalizedRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only student self-signup is allowed. Employer accounts are created by admin.");
        }

        String normalizedEmail = normalizeEmail(request.getEmail());
        String normalizedPen = normalizeRequiredField(request.getPen(), "PEN / Student ID");

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        if (userRepository.existsByPen(normalizedPen)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "PEN / Student ID is already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName() == null ? null : request.getFullName().trim());
        user.setPen(normalizedPen);
        user.setPhone(request.getPhone() == null ? null : request.getPhone().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setLocation(request.getLocation() == null ? null : request.getLocation().trim());
        user.setRole(normalizedRole);

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email or PEN / Student ID is already registered");
        }
    }

    @Transactional
    public User createEmployerByAdmin(String fullName, String phone, String email, String password, String location) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required");
        }

        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User employer = new User();
        employer.setFullName(fullName.trim());
        employer.setPhone(phone);
        employer.setEmail(normalizedEmail);
        employer.setPassword(passwordEncoder.encode(password));
        employer.setLocation(location);
        employer.setRole("employer");
        employer.setPen(null);

        try {
            User savedEmployer = userRepository.save(employer);

            Employer employerProfile = new Employer();
            employerProfile.setUser(savedEmployer);
            employerProfile.setFullName(savedEmployer.getFullName());
            employerProfile.setPhone(savedEmployer.getPhone());
            employerProfile.setEmail(savedEmployer.getEmail());
            employerProfile.setLocation(savedEmployer.getLocation());
            employerRepository.save(employerProfile);

            return savedEmployer;
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
    }

    @Transactional(readOnly = true)
    public List<EmployerAdminResponse> getEmployersByAdmin() {
        return employerRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toEmployerAdminResponse)
                .toList();
    }

    @Transactional
    public EmployerAdminResponse updateEmployerByAdmin(Long employerId, EmployerUpdateRequest request) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));

        User user = employer.getUser();

        String nextEmail = request.getEmail() == null || request.getEmail().isBlank()
                ? employer.getEmail()
                : normalizeEmail(request.getEmail());

        if (!nextEmail.equalsIgnoreCase(employer.getEmail()) && userRepository.existsByEmail(nextEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        String nextFullName = request.getFullName() == null ? employer.getFullName() : request.getFullName().trim();
        String nextPhone = request.getPhone() == null ? employer.getPhone() : request.getPhone().trim();
        String nextLocation = request.getLocation() == null ? employer.getLocation() : request.getLocation().trim();

        employer.setFullName(nextFullName);
        employer.setPhone(nextPhone);
        employer.setEmail(nextEmail);
        employer.setLocation(nextLocation);

        user.setFullName(nextFullName);
        user.setPhone(nextPhone);
        user.setEmail(nextEmail);
        user.setLocation(nextLocation);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        employerRepository.save(employer);
        userRepository.save(user);

        return toEmployerAdminResponse(employer);
    }

    @Transactional
    public void deleteEmployerByAdmin(Long employerId) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));

        User user = employer.getUser();
        employerRepository.delete(employer);
        userRepository.delete(user);
    }

    public User authenticate(String email, String password, String role) {
        ensureDefaultAdminAccount();

        String identifier = email == null ? "" : email.trim();
        String normalizedRole = role == null ? null : role.trim().toLowerCase();

        if (identifier.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID or email is required");
        }

        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        User user;
        if (identifier.matches("\\d+")) {
            Long userId = Long.parseLong(identifier);
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user ID/email or password"));
        } else {
            user = userRepository.findByEmail(identifier.toLowerCase())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user ID/email or password"));
        }

        if (DEFAULT_ADMIN_EMAIL.equalsIgnoreCase(user.getEmail())
                && "admin".equals(normalizedRole)) {
            if (!"admin".equalsIgnoreCase(user.getRole())) {
                user.setRole("admin");
            }
            if (!passwordEncoder.matches(DEFAULT_ADMIN_PASSWORD, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
            }
            user = userRepository.save(user);
        }

        if (normalizedRole != null && !normalizedRole.isBlank()
            && (user.getRole() == null || !normalizedRole.equalsIgnoreCase(user.getRole()))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Selected role does not match this account");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password");
        }

        return user;
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public UserDetails toUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()))
        );
    }

    private void ensureDefaultAdminAccount() {
        java.util.Optional<User> existing = userRepository.findByEmail(DEFAULT_ADMIN_EMAIL);
        if (existing.isPresent()) {
            User user = existing.get();
            if (!"admin".equalsIgnoreCase(user.getRole())) {
                user.setRole("admin");
                user.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
                userRepository.save(user);
            }
            return;
        }

        java.util.Optional<User> legacy = userRepository.findByEmail(LEGACY_ADMIN_EMAIL);
        if (legacy.isPresent()) {
            User user = legacy.get();
            user.setEmail(DEFAULT_ADMIN_EMAIL);
            user.setRole("admin");
            user.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
            try {
                userRepository.save(user);
            } catch (DataIntegrityViolationException ex) {
                user.setEmail(LEGACY_ADMIN_EMAIL);
                userRepository.save(user);
            }
            return;
        }

        User admin = new User();
        admin.setFullName("EasyIntern Admin");
        admin.setEmail(DEFAULT_ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
        admin.setRole("admin");
        userRepository.save(admin);
    }

    public void changePassword(String email, String currentPassword, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password and confirm password do not match");
        }

        if (newPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be at least 8 characters");
        }

        User user = getByEmail(email);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void deleteAccount(String email) {
        User user = getByEmail(email);
        userRepository.delete(user);
    }

    public void sendLoginNotification(String email) {
        User user = getByEmail(email);
        // TODO: Implement email sending
        // For now, just log that we would send an email
        System.out.println("Login notification: Email would be sent to " + email + " at " + java.time.LocalDateTime.now());
    }

    private EmployerAdminResponse toEmployerAdminResponse(Employer employer) {
        return new EmployerAdminResponse(
                employer.getId(),
                employer.getUser().getId(),
                employer.getFullName(),
                employer.getPhone(),
                employer.getEmail(),
                employer.getLocation(),
                employer.getCreatedAt() == null ? null : employer.getCreatedAt().toString()
        );
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        return email.trim().toLowerCase();
    }

    private String normalizeRequiredField(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value.trim();
    }
}

