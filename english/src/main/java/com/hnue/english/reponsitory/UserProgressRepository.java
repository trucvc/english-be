package com.hnue.english.reponsitory;

import com.hnue.english.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProgressRepository extends JpaRepository<UserProgress, Integer> {
}
