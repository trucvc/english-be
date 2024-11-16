package com.hnue.english.repository;

import com.hnue.english.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicProgressRepository extends JpaRepository<Topic, Integer> {
}
