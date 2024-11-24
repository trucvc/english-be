package com.hnue.english.service;

import com.hnue.english.dto.FormDTO;
import com.hnue.english.model.Course;
import com.hnue.english.model.FormSubmission;
import com.hnue.english.model.User;
import com.hnue.english.repository.FormSubmissionRepository;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FormSubmissionService {
    private final FormSubmissionRepository formSubmissionRepository;

    public FormSubmission createFormSubmission(FormDTO formDTO, User user){
        FormSubmission formSubmission = new FormSubmission(formDTO.getFormType(), formDTO.getContent(), formDTO.getStatus());
        formSubmission.setCreatedAt(new Date());
        formSubmission.setUpdatedAt(new Date());
        user.addFormSubmission(formSubmission);
        return formSubmissionRepository.save(formSubmission);
    }

    public FormSubmission getFormSubmission(int id){
        FormSubmission formSubmission = formSubmissionRepository.findById(id).orElseThrow(() -> new RuntimeException("Không có phiếu nào với id: " + id));
        return formSubmission;
    }

    public Page<FormSubmission> getForm(int page, int size, String email, int type, int status, String sort){
        Specification<FormSubmission> spec = (root, query, criteriaBuilder) -> {
            var predicates= criteriaBuilder.conjunction();
            var userJoin = root.join("user", JoinType.INNER);

            if (email != null && !email.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(userJoin.get("email"), "%" + email + "%"));
            }

            if (type != -1) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.equal(root.get("formType"), type));
            }

            if (status != -1) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.equal(root.get("status"), status));
            }

            if (sort != null && !sort.trim().isEmpty()) {
                switch (sort) {
                    case "email":
                        query.orderBy(criteriaBuilder.asc(userJoin.get("email")));
                        break;
                    case "-email":
                        query.orderBy(criteriaBuilder.desc(userJoin.get("email")));
                        break;
                    case "updatedAt":
                        query.orderBy(criteriaBuilder.asc(root.get("updatedAt")));
                        break;
                    case "-updatedAt":
                        query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));
                        break;
                    default:
                        break;
                }
            }

            return predicates;
        };
        Pageable pageable = PageRequest.of(page, size);
        return formSubmissionRepository.findAll(spec, pageable);
    }

    public List<FormSubmission> getAllFormSubmission(){
        return formSubmissionRepository.findAll();
    }

    public FormSubmission updateFormSubmission(int id){
        FormSubmission formSubmission = formSubmissionRepository.findById(id).orElseThrow(() -> new RuntimeException("Không có phiếu nào với id: " + id));
        if (formSubmission.getStatus() < 3){
            int status = formSubmission.getStatus();
            formSubmission.setStatus(status+1);
        }
        formSubmission.setUpdatedAt(new Date());
        return formSubmissionRepository.save(formSubmission);
    }

    public FormSubmission rejectedFormSubmission(int id){
        FormSubmission formSubmission = formSubmissionRepository.findById(id).orElseThrow(() -> new RuntimeException("Không có phiếu nào với id: " + id));
        formSubmission.setStatus(4);
        formSubmission.setUpdatedAt(new Date());
        return formSubmissionRepository.save(formSubmission);
    }

    public void deleteFormSubmission(int id){
        FormSubmission formSubmission = formSubmissionRepository.findById(id).orElseThrow(() -> new RuntimeException("Không có phiếu nào với id: " + id));
        formSubmissionRepository.delete(formSubmission);
    }
}
