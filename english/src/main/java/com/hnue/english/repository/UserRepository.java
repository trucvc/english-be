package com.hnue.english.repository;

import com.hnue.english.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.subscriptionPlan != 'none'")
    List<User> getAllUser();

    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :start AND :end")
    List<User> findUsersCreatedBetween(@Param("start") Date start, @Param("end") Date end);

    @Query("SELECT u.subscriptionPlan, COUNT(u) FROM User u GROUP BY u.subscriptionPlan")
    List<Object[]> findUserSegmentsBySubscription();

    @Query("SELECT u FROM User u WHERE u.subscriptionPlan != 'none' AND u.subscriptionEndDate > CURRENT_TIMESTAMP ORDER BY u.subscriptionEndDate")
    List<User> findUsersWithExpiringSubscriptions();
}
