package com.easyintern.api.repository;

import com.easyintern.api.model.ApplicationTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationTaskRepository extends JpaRepository<ApplicationTask, Long> {
}
