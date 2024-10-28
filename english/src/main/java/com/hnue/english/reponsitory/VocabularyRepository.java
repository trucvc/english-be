package com.hnue.english.reponsitory;

import com.hnue.english.model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Integer> {
    boolean existsByWord(String word);
}
