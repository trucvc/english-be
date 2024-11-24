package com.hnue.english.service;

import com.hnue.english.model.*;
import com.hnue.english.repository.CourseProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    public List<CourseProgress> getAllCourseProgress(User user){
        return courseProgressRepository.getAllCourseProgressForUser(user);
    }

    public Map<String, Long> getTop10PopularCourses() {
        List<Object[]> results = courseProgressRepository.findTop10PopularCourses();
        List<Object[]> topResults = results.size() > 10 ? results.subList(0, 10) : results;
        return topResults.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }
}
