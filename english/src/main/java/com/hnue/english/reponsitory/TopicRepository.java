package com.hnue.english.reponsitory;

import com.hnue.english.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Integer> {
    @Query("SELECT t FROM Topic t JOIN FETCH t.vocabularies WHERE t.id = :id")
    Optional<Topic> getTopicWithVocabulary(@Param("id") int id);
}
