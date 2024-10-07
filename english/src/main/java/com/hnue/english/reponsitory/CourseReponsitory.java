package com.hnue.english.reponsitory;

import com.hnue.english.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseReponsitory extends JpaRepository<Course, Integer> {
    @Query("SELECT c FROM Course c JOIN FETCH c.topics WHERE c.id = :id")
    Optional<Course> getCourseWithTopic(@Param("id") int id);
}
