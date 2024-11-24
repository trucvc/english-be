package com.hnue.english.controller;

import com.hnue.english.dto.FormDTO;
import com.hnue.english.model.FormSubmission;
import com.hnue.english.model.User;
import com.hnue.english.response.ApiResponse;
import com.hnue.english.service.FormSubmissionService;
import com.hnue.english.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/forms")
@RequiredArgsConstructor
public class FormSubmissionController {
    private final FormSubmissionService formSubmissionService;
    private final UserService userService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder){
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(false);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createForm(HttpServletRequest request, @RequestParam(defaultValue = "-1") int formType,
                                                     @RequestParam String content){
        try {
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);
            User user = userService.fetch(token);
            if (content.isEmpty() || formType == -1){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            if (formType < 0 || formType > 6){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Type nằm trong khoảng 0-6", "Bad Request"));
            }
            FormDTO formDTO = FormDTO.builder()
                    .formType(formType).content(content).status(0)
                    .build();
            FormSubmission formSubmission = formSubmissionService.createFormSubmission(formDTO, user);
            return ResponseEntity.status(201).body(ApiResponse.success(201, "", formSubmission));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllForms(){
        List<FormSubmission> formSubmissions = formSubmissionService.getAllFormSubmission();
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", formSubmissions));
    }

    @GetMapping("/page")
    public ResponseEntity<ApiResponse<?>> getForms(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "1") int size,
                                                   @RequestParam(required = false) String email,
                                                   @RequestParam(required = false, defaultValue = "-1") int type,
                                                   @RequestParam(required = false, defaultValue = "-1") int status,
                                                   @RequestParam(required = false) String sort){
        if (size < 1){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "size phải lớn hơn 0", "Bad Request"));
        }
        Page<FormSubmission> formSubmissions = formSubmissionService.getForm(page, size, email, type, status, sort);
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", formSubmissions));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<?>> updateForm(@PathVariable int id){
        try {
            FormSubmission formSubmission = formSubmissionService.updateFormSubmission(id);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", formSubmission));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @PutMapping("/rejected/{id}")
    public ResponseEntity<ApiResponse<?>> rejectedForm(@PathVariable int id){
        try {
            FormSubmission formSubmission = formSubmissionService.rejectedFormSubmission(id);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", formSubmission));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteForm(@PathVariable int id){
        try {
            formSubmissionService.deleteFormSubmission(id);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "Xóa thành công form với id: "+id, null));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }
}
