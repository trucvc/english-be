package com.hnue.english.service;

import com.hnue.english.model.*;
import com.hnue.english.repository.CourseProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseProgressService {
    private final CourseProgressRepository courseProgressRepository;

    public void createCourseProgressIfNotExist(User user, Course course, int isCompleted, Date completedAt) {
        Optional<CourseProgress> existingProgress = courseProgressRepository.findByUserAndCourse(user, course);

        if (existingProgress.isEmpty()) {
            CourseProgress newProgress = new CourseProgress(isCompleted, completedAt);
            newProgress.setUser(user);
            newProgress.setCourse(course);
            courseProgressRepository.save(newProgress);
        }
    }

    public List<CourseProgress> getAllCourseProgress(){
        return courseProgressRepository.findAll();
    }
}
