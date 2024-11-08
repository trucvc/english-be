package com.hnue.english.repository;

import com.hnue.english.model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Integer>, JpaSpecificationExecutor<Vocabulary> {
    boolean existsByWord(String word);
}
