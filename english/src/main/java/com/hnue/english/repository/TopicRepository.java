package com.hnue.english.repository;

import com.hnue.english.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Integer>, JpaSpecificationExecutor<Topic> {
    @Query("SELECT t FROM Topic t LEFT JOIN FETCH t.vocabularies WHERE t.id = :id")
    Optional<Topic> getTopicWithVocabulary(@Param("id") int id);

    boolean existsByTopicName(String topicName);

    boolean existsByDescription(String description);
}
