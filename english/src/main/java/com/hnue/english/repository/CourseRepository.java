package com.hnue.english.repository;

import com.hnue.english.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Integer>, JpaSpecificationExecutor<Course> {
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.topics WHERE c.id = :id")
    Optional<Course> getCourseWithTopic(@Param("id") int id);

    boolean existsByCourseName(String courseName);

    @Query("SELECT c FROM Course c JOIN FETCH c.topics")
    List<Course> findAllCourseWithTopic();
}
