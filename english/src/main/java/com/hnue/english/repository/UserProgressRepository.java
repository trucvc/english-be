package com.hnue.english.repository;

import com.hnue.english.model.User;
import com.hnue.english.model.UserProgress;
import com.hnue.english.model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Integer>, JpaSpecificationExecutor<UserProgress> {
    @Query("SELECT us FROM UserProgress us WHERE us.user = :user")
    List<UserProgress> findAllVocabForUser(@Param("user") User user);

    @Query("SELECT us.level, COUNT(us) FROM UserProgress us WHERE us.user = :user GROUP BY us.level")
    List<Object[]> countLevelsByUser(@Param("user") User user);

    @Query(value = "SELECT * FROM user_progress us WHERE us.user_id = :userId ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<UserProgress> findAllVocabForUserWithExam(@Param("userId") int userId);

    @Query("SELECT us FROM UserProgress us WHERE us.user = :user AND us.vocabulary = :vocab")
    Optional<UserProgress> getUserProgress(@Param("user") User user, @Param("vocab") Vocabulary vocabulary);

    @Query("SELECT us FROM UserProgress us WHERE us.user = :user ORDER BY us.level")
    List<UserProgress> findUserProgressByLevel(@Param("user") User user);

    boolean existsByUserAndVocabulary(User user, Vocabulary vocabulary);
}
