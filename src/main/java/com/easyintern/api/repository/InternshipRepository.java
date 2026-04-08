package com.easyintern.api.repository;

import com.easyintern.api.model.Internship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {

    List<Internship> findByCompany(String company);

    List<Internship> findByActive(Boolean active);

    @Query("SELECT i FROM Internship i WHERE i.active = true ORDER BY i.postedDate DESC")
    List<Internship> findAllActiveInternships();

    @Query("SELECT i FROM Internship i WHERE i.title LIKE %:keyword% OR i.company LIKE %:keyword% OR i.skills LIKE %:keyword%")
    List<Internship> searchInternships(@Param("keyword") String keyword);
}
