package com.hnue.english.controller;

import com.hnue.english.model.Course;
import com.hnue.english.model.Topic;
import com.hnue.english.model.User;
import com.hnue.english.model.Vocabulary;
import com.hnue.english.response.ApiResponse;
import com.hnue.english.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/statistical")
@RequiredArgsConstructor
public class StatisticalController {
    private final UserService userService;
    private final UserProgressService userProgressService;
    private final CourseService courseService;
    private final CourseProgressService courseProgressService;
    private final TopicService topicService;
    private final TopicProgressService topicProgressService;
    private final VocabularyService vocabularyService;

    @GetMapping("/active_count")
    public ResponseEntity<ApiResponse<?>> countUser(){
        List<User> users = userService.getAllUsers();
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", users.size()));
    }

    @GetMapping("/new")
    public ResponseEntity<ApiResponse<?>> newUser(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date start,
                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date end){
        if (start.after(end)){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "start lớn hơn end", "Bad Request"));
        }
        List<User> users = userService.getUsersCreatedBetween(start, end);
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", users.size()));
    }

    @GetMapping("/segments")
    public ResponseEntity<ApiResponse<?>> segments(){
        Map<String, Long> map = userService.getUserSegments();
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", map));
    }

    @GetMapping("/count_course")
    public ResponseEntity<ApiResponse<?>> countCourse(){
        List<Course> courses = courseService.getAllCourses();
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", courses.size()));
    }

    @GetMapping("/popular_course")
    public ResponseEntity<ApiResponse<?>> popularCourse(){
        Map<String, Long> courses = courseProgressService.getTop10PopularCourses();
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", courses));
    }

    @GetMapping("/count_topic")
    public ResponseEntity<ApiResponse<?>> countTopic(){
        List<Topic> topics = topicService.getAllTopic();
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", topics.size()));
    }

    @GetMapping("/popular_topic")
    public ResponseEntity<ApiResponse<?>> popularTopic(){
        Map<String, Long> topics = topicProgressService.getTop10PopularTopics();
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", topics));
    }

    @GetMapping("/count_vocab")
    public ResponseEntity<ApiResponse<?>> countVocab(){
        List<Vocabulary> vocabularies = vocabularyService.getAllVocab();
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", vocabularies.size()));
    }

    @GetMapping("/popular_vocab")
    public ResponseEntity<ApiResponse<?>> popularVocab(){
        Map<String, Long> vocabularies = userProgressService.getTop10PopularVocabs();
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", vocabularies));
    }

    @GetMapping("/expiring_soon")
    public ResponseEntity<ApiResponse<?>> expiringUser(){
        List<User> users = userService.getUsersWithExpiringSubscriptions();
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", users));
    }

    @GetMapping("/course")
    public ResponseEntity<ApiResponse<?>> course(){
        List<Course> courses = courseService.getAllWithTopicsAndVocabsOrderedByCourseName("","","","");
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", courses.size()));
    }

    @GetMapping("/topic")
    public ResponseEntity<ApiResponse<?>> topic(){
        List<Topic> topics = topicService.getAllWithVocabs("", "", 0, "");
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", topics.size()));
    }

    @GetMapping("/sub")
    public ResponseEntity<ApiResponse<?>> sub(){
        Map<Integer, Long> sub = userService.countUsersByMonthCurrentYear();
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", sub));
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<?>> revenue(){
        Map<String, Long> map = userService.getUserSegments();
        BigDecimal sum = calculateRevenueByPlan(map);
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", sum));
    }

    private BigDecimal calculateRevenueByPlan(Map<String, Long> userSegments) {
        Map<String, BigDecimal> subscriptionPrices = Map.of(
                "6_months", new BigDecimal("399000.0"),
                "1_year", new BigDecimal("699000.0"),
                "3_years", new BigDecimal("1199000.0")
        );

        return userSegments.entrySet().stream()
                .filter(entry -> subscriptionPrices.containsKey(entry.getKey()))
                .map(entry -> subscriptionPrices.get(entry.getKey())
                        .multiply(new BigDecimal(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @GetMapping("/vip")
    public ResponseEntity<ApiResponse<?>> vip(){
        List<User> users = userService.getAllUsers();
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", usersVip(users).size()));
    }

    private List<User> usersVip(List<User> users){
        return users.stream()
                .filter(user -> !user.getSubscriptionPlan().equals("none"))
                .collect(Collectors.toList());
    }
}
