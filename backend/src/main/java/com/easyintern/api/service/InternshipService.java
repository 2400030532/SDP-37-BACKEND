package com.easyintern.api.service;

import com.easyintern.api.model.Internship;
import com.easyintern.api.repository.InternshipRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class InternshipService {

    private final InternshipRepository internshipRepository;

    public InternshipService(InternshipRepository internshipRepository) {
        this.internshipRepository = internshipRepository;
    }

    public List<Internship> getAllInternships() {
        return internshipRepository.findAll();
    }

    public List<Internship> getActiveInternships() {
        return internshipRepository.findAllActiveInternships();
    }

    public Optional<Internship> getInternshipById(Long id) {
        return internshipRepository.findById(id);
    }

    public Internship createInternship(Internship internship) {
        return internshipRepository.save(internship);
    }

    public Internship updateInternship(Long id, Internship internship) {
        return internshipRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(internship.getTitle());
                    existing.setCompany(internship.getCompany());
                    existing.setDescription(internship.getDescription());
                    existing.setLocation(internship.getLocation());
                    existing.setStipend(internship.getStipend());
                    existing.setDuration(internship.getDuration());
                    existing.setDeadline(internship.getDeadline());
                    existing.setSkills(internship.getSkills());
                    existing.setActive(internship.getActive());
                    return internshipRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Internship not found with id: " + id));
    }

    public void deleteInternship(Long id) {
        internshipRepository.deleteById(id);
    }

    public List<Internship> searchInternships(String keyword) {
        return internshipRepository.searchInternships(keyword);
    }

    public List<Internship> getInternshipsByCompany(String company) {
        return internshipRepository.findByCompany(company);
    }
}
