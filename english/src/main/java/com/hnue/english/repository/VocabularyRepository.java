package com.hnue.english.repository;

import com.hnue.english.model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Integer>, JpaSpecificationExecutor<Vocabulary> {
    @Query(value = "SELECT * FROM vocabulary v WHERE v.topic_id = :topicId AND v.vocab_id != :vocabId ORDER BY RAND() LIMIT 2", nativeQuery = true)
    List<Vocabulary> findTwoRandomVocabs(@Param("vocabId") int vocabId, @Param("topicId") int topicId);

    boolean existsByWord(String word);
}
