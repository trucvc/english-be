package com.hnue.english.repository;

import com.hnue.english.model.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FormSubmissionRepository extends JpaRepository<FormSubmission, Integer>, JpaSpecificationExecutor<FormSubmission> {
}
