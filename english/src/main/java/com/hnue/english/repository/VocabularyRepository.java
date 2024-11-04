package com.hnue.english.repository;

import com.hnue.english.model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Integer> {
    boolean existsByWord(String word);
}
