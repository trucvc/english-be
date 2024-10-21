package com.hnue.english.service;

import com.hnue.english.dto.CourseDTO;
import com.hnue.english.model.Course;
import com.hnue.english.model.Topic;
import com.hnue.english.reponsitory.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    public Course createCourse(CourseDTO courseDTO){
        Course course = new Course();
        course.setCourseName(courseDTO.getCourseName());
        course.setDescription(courseDTO.getDescription());
        return courseRepository.save(course);
    }

    public Course getCourse(int id){
        Course course = courseRepository.getCourseWithTopic(id).orElseThrow(()-> new RuntimeException("Không tồn tại khóa học với id: "+id));

        List<Topic> theTopic = new ArrayList<>(course.getTopics());
        theTopic.sort(Comparator.comparing(Topic::getOrder));

        course.setTopics(new ArrayList<>(theTopic));
        return course;
    }

    public List<Course> getAllCourse(){
        return courseRepository.findAll();
    }

    public Course updateCourse(int id, CourseDTO courseDTO){
        Course course = courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tồn tại khóa học với id: "+id));

        course.setCourseName(courseDTO.getCourseName());
        course.setDescription(courseDTO.getDescription());

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
}
