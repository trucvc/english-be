package com.hnue.english.service;

import com.hnue.english.dto.CourseDTO;
import com.hnue.english.dto.ListCourse;
import com.hnue.english.dto.ListTopic;
import com.hnue.english.dto.UserDTO;
import com.hnue.english.model.Course;
import com.hnue.english.model.Topic;
import com.hnue.english.model.User;
import com.hnue.english.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    public Course createCourse(CourseDTO courseDTO){
        Course course = new Course();
        course.setCourseName(courseDTO.getCourseName());
        course.setDescription(courseDTO.getDescription());
        course.setCourseTarget(courseDTO.getCourseTarget());
        course.setCreatedAt(new Date());
        course.setUpdatedAt(new Date());
        return courseRepository.save(course);
    }

    public Course getCourse(int id){
        Course course = courseRepository.getCourseWithTopic(id).orElseThrow(()-> new RuntimeException("Không tồn tại khóa học với id: "+id));

        List<Topic> theTopic = new ArrayList<>(course.getTopics());
        theTopic.sort(Comparator.comparing(Topic::getCreatedAt));

        course.setTopics(new ArrayList<>(theTopic));
        return course;
    }

    public List<Course> getAllWithTopicsAndVocabsOrderedByCourseName(){
        return courseRepository.findAllWithTopicsAndVocabsOrderedByCourseName();
    }

    public Page<Course> getCourses(int page, int size, String courseName, String description, String courseTarget, String sort){
        Specification<Course> spec = (root, query, criteriaBuilder) -> {
            var predicates= criteriaBuilder.conjunction();

            if (courseName != null && !courseName.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("courseName"), "%" + courseName + "%"));
            }

            if (description != null && !description.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("description"), "%" + description + "%"));
            }

            if (courseTarget != null && !courseTarget.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("courseTarget"), "%" + courseTarget + "%"));
            }

            if (sort != null && !sort.trim().isEmpty()) {
                switch (sort) {
                    case "courseName":
                        query.orderBy(criteriaBuilder.asc(root.get("courseName")));
                        break;
                    case "-courseName":
                        query.orderBy(criteriaBuilder.desc(root.get("courseName")));
                        break;
                    case "description":
                        query.orderBy(criteriaBuilder.asc(root.get("description")));
                        break;
                    case "-description":
                        query.orderBy(criteriaBuilder.desc(root.get("description")));
                        break;
                    case "courseTarget":
                        query.orderBy(criteriaBuilder.asc(root.get("courseTarget")));
                        break;
                    case "-courseTarget":
                        query.orderBy(criteriaBuilder.desc(root.get("courseTarget")));
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
        return courseRepository.findAll(spec, pageable);
    }

    public Course updateCourse(int id, CourseDTO courseDTO){
        Course course = courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tồn tại khóa học với id: "+id));

        course.setCourseName(courseDTO.getCourseName());
        course.setDescription(courseDTO.getDescription());
        course.setCourseTarget(courseDTO.getCourseTarget());
        course.setUpdatedAt(new Date());
        return courseRepository.save(course);
    }

    public void deleteCourse(int id){
        Course course = courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tồn tại khóa học với id: "+id));
        for (Topic theTopic : course.getTopics()){
            theTopic.setCourse(null);
        }
        courseRepository.delete(course);
    }

    public boolean preUpdateCourse(int id, String courseName){
        Course course = courseRepository.getCourseWithTopic(id).orElseThrow(() -> new RuntimeException("Không tồn tại khóa học với id: "+id));
        return course.getCourseName().equals(courseName);
    }

    public boolean existsByCourseName(String courseName){
        return courseRepository.existsByCourseName(courseName);
    }

    public boolean existsById(int id){
        return courseRepository.existsById(id);
    }

    public List<String> checkNonExistingIdCourses(List<ListTopic> list) {
        return list.stream()
                .filter(listTopic -> !existsById(listTopic.getCourseId()))
                .map(listTopic -> String.valueOf(listTopic.getCourseId()))
                .collect(Collectors.toList());
    }

    public List<String> checkExistingCourseNames(List<ListCourse> list) {
        return list.stream()
                .filter(listCourse -> existsByCourseName(listCourse.getCourseName()))
                .map(ListCourse::getCourseName)
                .collect(Collectors.toList());
    }

    public void saveAll(List<ListCourse> list) {
        List<Course> coursesToSave = list.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        courseRepository.saveAll(coursesToSave);
    }

    private Course convertToEntity(ListCourse listCourse) {
        Course course = new Course();
        course.setCourseName(listCourse.getCourseName());
        course.setDescription(listCourse.getDescription());
        course.setCourseTarget(listCourse.getCourseTarget());
        course.setCreatedAt(new Date());
        course.setUpdatedAt(new Date());
        return course;
    }
}
