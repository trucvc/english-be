package com.hnue.english.service;

import com.hnue.english.model.FormSubmission;
import com.hnue.english.reponsitory.FormSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FormSubmissionService {
    private final FormSubmissionRepository formSubmissionRepository;

    public void createFormSubmission(FormSubmission theFormSubmission){
        formSubmissionRepository.save(theFormSubmission);
    }

    public FormSubmission getFormSubmission(int id){
        FormSubmission formSubmission = formSubmissionRepository.findById(id).orElseThrow(() -> new RuntimeException("Không có phiếu nào với id: " + id));
        return formSubmission;
    }

    public List<FormSubmission> getAllFormSubmission(){
        return formSubmissionRepository.findAll();
    }

    public FormSubmission updateFormSubmission(int id, FormSubmission theFormSubmission){
        FormSubmission formSubmission = formSubmissionRepository.findById(id).orElseThrow(() -> new RuntimeException("Không có phiếu nào với id: " + id));
        formSubmission.setFormType(theFormSubmission.getFormType());
        formSubmission.setContent(theFormSubmission.getContent());
        formSubmission.setStatus(theFormSubmission.getStatus());
        return formSubmission;
    }

    public void deleteFormSubmission(int id){
        FormSubmission formSubmission = formSubmissionRepository.findById(id).orElseThrow(() -> new RuntimeException("Không có phiếu nào với id: " + id));
        formSubmissionRepository.delete(formSubmission);
    }
}
