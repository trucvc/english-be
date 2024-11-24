package com.hnue.english.repository;

import com.hnue.english.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseProgressRepository extends JpaRepository<CourseProgress, Integer> {
    @Query("SELECT cp FROM CourseProgress cp WHERE cp.user = :user AND cp.course = :course")
    Optional<CourseProgress> findByUserAndCourse(@Param("user") User user, @Param("course") Course course);

    @Query("SELECT cp FROM CourseProgress cp WHERE cp.user = :user")
    List<CourseProgress> getAllCourseProgressForUser(@Param("user") User user);

    @Query("SELECT cp.course.courseName, COUNT(cp) FROM CourseProgress cp WHERE cp.isCompleted = 1 GROUP BY cp.course.courseName ORDER BY COUNT(cp) DESC")
    List<Object[]> findTop10PopularCourses();
}
