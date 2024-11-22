package com.hnue.english.service;

import com.hnue.english.model.User;
import com.hnue.english.model.UserProgress;
import com.hnue.english.model.Vocabulary;
import com.hnue.english.repository.UserProgressRepository;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProgressService {
    private final UserProgressRepository userProgressRepository;
    private final TopicService topicService;

    public List<UserProgress> getAllVocabForUser(User user){
        return userProgressRepository.findAllVocabForUser(user);
    }

    public List<UserProgress> saveAllVocabForUser(User user, List<Vocabulary> vocabs){
        List<UserProgress> us = new ArrayList<>();
        for (Vocabulary v : vocabs){
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.HOUR, 1);
            UserProgress u = new UserProgress();
            u.setLastReviewed(new Date());
            u.setReviewInterval(1);
            u.setNextReview(calendar.getTime());
            u.setLevel(1);
            u.setUser(user);
            u.setVocabulary(v);
            us.add(u);
        }
        return userProgressRepository.saveAll(us);
    }

    public Map<Integer, Long> countLevelsByUser(User user){
        List<Object[]> results = userProgressRepository.countLevelsByUser(user);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Integer) result[0],
                        result -> (Long) result[1]
                ));
    }

    public List<UserProgress> getAllVocabForUserWithExam(User user){
        return userProgressRepository.findAllVocabForUserWithExam(user.getUserId());
    }

    public UserProgress getUserProgress(User user, Vocabulary vocabulary){
        return userProgressRepository.getUserProgress(user, vocabulary).orElseThrow(() -> new RuntimeException("Khong con tai us"));
    }

    public UserProgress updateUserProgress(UserProgress us, int status){
        us.setLastReviewed(new Date());
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        if (status == 1){
            if (us.getLevel() == 5){
                calendar.add(Calendar.MONTH, 1);
                us.setNextReview(calendar.getTime());
            }else{
                us.setLevel(us.getLevel()+1);
                us.setNextReview(updateLevel(us.getLevel()));
            }
        }else{
            calendar.add(Calendar.HOUR, 1);
            us.setNextReview(calendar.getTime());
            us.setLevel(1);
        }
        return userProgressRepository.save(us);
    }

    public Date updateLevel(int level){
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        switch (level){
            case 1:
                calendar.add(Calendar.HOUR, 1);
                break;
            case 2:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case 3:
                calendar.add(Calendar.DAY_OF_MONTH, 3);
                break;
            case 4:
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                break;
            case 5:
                calendar.add(Calendar.MONTH, 1);
                break;
        }
        return calendar.getTime();
    }

//    public Map<Integer, List<UserProgress>> getUserProgressByLevel(User user){
//        List<UserProgress> list = userProgressRepository.findUserProgressByLevel(user);
//        Map<Integer, List<UserProgress>> groupedByLevel = list.stream()
//                .collect(Collectors.groupingBy(UserProgress::getLevel));
//        return groupedByLevel;
//    }

    public List<UserProgress> getUserProgressByLevel(String search, int level, User user){
        Specification<UserProgress> spec = (root, query, criteriaBuilder) -> {
            var predicates= criteriaBuilder.conjunction();
            var vocabularyJoin = root.join("vocabulary", JoinType.INNER);
            var userJoin = root.join("user", JoinType.INNER);

            if (search != null && !search.trim().isEmpty()) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.or(
                                criteriaBuilder.like(vocabularyJoin.get("word"), "%" + search + "%"),
                                criteriaBuilder.like(vocabularyJoin.get("meaning"), "%" + search + "%")
                        )
                );
            }

            if (level != 0) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.equal(root.get("level"), level));
            }

            predicates = criteriaBuilder.and(
                    predicates,
                    criteriaBuilder.equal(userJoin.get("id"), user.getUserId())
            );

            query.orderBy(criteriaBuilder.asc(vocabularyJoin.get("word")));

            return predicates;
        };
        return userProgressRepository.findAll(spec);
    }

    public boolean isVocabExistForUser(User user, Vocabulary vocab) {
        return userProgressRepository.existsByUserAndVocabulary(user, vocab);
    }

    public boolean allVocabulariesAssignedToUser(User user, List<Vocabulary> vocabularies) {
        for (Vocabulary vocab : vocabularies) {
            Optional<UserProgress> userProgress = userProgressRepository.getUserProgress(user, vocab);
            if (userProgress.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void deleteUserProgress(UserProgress userProgress){
        userProgressRepository.delete(userProgress);
    }

    @Scheduled(fixedDelay = 5000)
    public void getAllUserProgress(){
        List<UserProgress> list = userProgressRepository.findAll();
        for (UserProgress us : list){
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(us.getLastReviewed());
            if (us.getLevel() == 1){
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                if (calendar.getTime().before(now)){
                    deleteUserProgress(us);
                }
            } else if (us.getLevel() == 2) {
                calendar.add(Calendar.DAY_OF_MONTH, 3);
                if (calendar.getTime().before(now)){
                    updateUserProgress(us, 0);
                }
            } else if (us.getLevel() == 3) {
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                if (calendar.getTime().before(now)){
                    updateUserProgress(us, 0);
                }
            } else if (us.getLevel() == 4) {
                calendar.add(Calendar.DAY_OF_MONTH, 14);
                if (calendar.getTime().before(now)){
                    updateUserProgress(us, 0);
                }
            } else {
                calendar.add(Calendar.MONTH, 1);
                if (calendar.getTime().before(now)){
                    updateUserProgress(us, 0);
                }
            }
        }
    }
}
