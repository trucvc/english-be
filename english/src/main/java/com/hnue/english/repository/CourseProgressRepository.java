package com.hnue.english.repository;

import com.hnue.english.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseProgressRepository extends JpaRepository<CourseProgress, Integer> {
    @Query("SELECT cp FROM CourseProgress cp WHERE cp.user = :user AND cp.course = :course")
    Optional<CourseProgress> findByUserAndCourse(@Param("user") User user, @Param("course") Course course);
}
