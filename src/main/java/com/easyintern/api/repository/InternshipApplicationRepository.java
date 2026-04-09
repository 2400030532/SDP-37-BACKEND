package com.easyintern.api.repository;

import com.easyintern.api.model.InternshipApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InternshipApplicationRepository extends JpaRepository<InternshipApplication, Long> {
    @Query("""
        select distinct ia
        from InternshipApplication ia
        left join fetch ia.internship
        left join fetch ia.tasks
        where ia.student.id = :studentId
        order by ia.appliedAt desc
        """)
    List<InternshipApplication> findByStudentId(@Param("studentId") Long studentId);

    @Query("""
        select distinct ia
        from InternshipApplication ia
        left join fetch ia.internship
        left join fetch ia.tasks
        where ia.id = :applicationId
        """)
    Optional<InternshipApplication> findByIdWithDetails(@Param("applicationId") Long applicationId);

    @Query("""
        select distinct ia
        from InternshipApplication ia
        left join fetch ia.internship
        left join fetch ia.tasks
        order by ia.appliedAt desc
        """)
    List<InternshipApplication> findAllWithDetails();

    Optional<InternshipApplication> findByStudentIdAndInternshipId(Long studentId, Long internshipId);
}
