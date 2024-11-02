package com.hnue.english.controller;

import com.hnue.english.dto.CourseDTO;
import com.hnue.english.model.Course;
import com.hnue.english.response.ApiResponse;
import com.hnue.english.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/course")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(false);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createCourse(@RequestParam String courseName, @RequestParam String description,
                                                       @RequestParam String courseTarget){
        if (courseName.isEmpty() || description.isEmpty() || courseTarget.isEmpty()){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
        }
        if (courseService.existsByCourseName(courseName)){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Đã tồn tại tên khóa học này", "Bad Request"));
        }
        CourseDTO courseDTO = CourseDTO.builder()
                .courseName(courseName)
                .description(description)
                .courseTarget(courseTarget)
                .build();
        Course c = courseService.createCourse(courseDTO);
        return ResponseEntity.status(201).body(ApiResponse.success(201, "", c));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getCourse(@PathVariable int id){
        try {
            Course course = courseService.getCourse(id);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", course));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllCourse(){
        List<Course> courses = courseService.getAllCourse();
        if (courses == null){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Không có khóa học nào!", "Bad Request"));
        }else{
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", courses));
        }
    }

    @GetMapping("/page")
    public ResponseEntity<ApiResponse<?>> getCourses(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "1") int size){
        if (size < 1){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "size phải lớn hơn 0", "Bad Request"));
        }
        Page<Course> courses = courseService.getCourses(page, size);
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", courses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateCourse(@PathVariable int id, @RequestParam String courseName,
                                                       @RequestParam String description, @RequestParam String courseTarget){
        try {
            if (courseName.isEmpty() || description.isEmpty() || courseTarget.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            if (!courseService.preUpdateCourse(id, courseName)){
                if (courseService.existsByCourseName(courseName)){
                    return ResponseEntity.status(400).body(ApiResponse.error(400, "Đã tồn tại tên khóa học này", "Bad Request"));
                }
            }
            CourseDTO courseDTO = CourseDTO.builder()
                    .courseName(courseName)
                    .description(description)
                    .courseTarget(courseTarget)
                    .build();
            Course course = courseService.updateCourse(id, courseDTO);
            return ResponseEntity.status(201).body(ApiResponse.success(201, "", course));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteCourse(@PathVariable int id){
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "Xóa thành công khóa học với id: "+id, null));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }
}
