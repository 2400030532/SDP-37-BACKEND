package com.easyintern.api.repository;

import com.easyintern.api.model.InternshipApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InternshipApplicationRepository extends JpaRepository<InternshipApplication, Long> {
    List<InternshipApplication> findByStudentId(Long studentId);
    Optional<InternshipApplication> findByStudentIdAndInternshipId(Long studentId, Long internshipId);
}
