package com.hnue.english.reponsitory;

import com.hnue.english.model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VocabularyReponsitory extends JpaRepository<Vocabulary, Integer> {
}
