package com.easyintern.api.repository;

import com.easyintern.api.model.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface EmployerRepository extends JpaRepository<Employer, Long> {
    @EntityGraph(attributePaths = "user")
    List<Employer> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "user")
    Optional<Employer> findByUserId(Long userId);

    @EntityGraph(attributePaths = "user")
    Optional<Employer> findById(Long id);

    Optional<Employer> findByEmail(String email);
}