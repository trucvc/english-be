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

    @Query("SELECT u.subscriptionPlan, COUNT(u) FROM User u GROUP BY u.subscriptionPlan ORDER BY " +
            "CASE " +
            "  WHEN u.subscriptionPlan = '6_months' THEN 1 " +
            "  WHEN u.subscriptionPlan = '1_year' THEN 2 " +
            "  WHEN u.subscriptionPlan = '3_years' THEN 3 " +
            "  WHEN u.subscriptionPlan = 'none' THEN 4 " +
            "  ELSE 5 " +
            "END")
    List<Object[]> findSubscriptionPlanCounts();

    @Query("SELECT u FROM User u WHERE u.subscriptionPlan != 'none' AND u.subscriptionEndDate > CURRENT_TIMESTAMP ORDER BY u.subscriptionEndDate")
    List<User> findUsersWithExpiringSubscriptions();

    @Query("SELECT FUNCTION('MONTH', u.createdAt) AS month, COUNT(u) AS totalUsers " +
            "FROM User u " +
            "WHERE FUNCTION('YEAR', u.createdAt) = FUNCTION('YEAR', CURRENT_DATE) " +
            "GROUP BY FUNCTION('MONTH', u.createdAt) " +
            "ORDER BY month")
    List<Object[]> countUsersByMonthCurrentYear();
}
