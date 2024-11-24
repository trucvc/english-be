package com.hnue.english.repository;

import com.hnue.english.model.Topic;
import com.hnue.english.model.TopicProgress;
import com.hnue.english.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TopicProgressRepository extends JpaRepository<TopicProgress, Integer> {
    @Query("SELECT tp FROM TopicProgress tp WHERE tp.user = :user AND tp.topic = :topic")
    Optional<TopicProgress> findByUserAndTopic(@Param("user") User user, @Param("topic") Topic topic);

    @Query("SELECT tp FROM TopicProgress tp WHERE tp.user = :user")
    List<TopicProgress> getAllTopicProgressForUser(@Param("user") User user);
}
