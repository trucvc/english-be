package com.hnue.english.service;

import com.hnue.english.model.Topic;
import com.hnue.english.model.User;
import com.hnue.english.model.UserProgress;
import com.hnue.english.model.Vocabulary;
import com.hnue.english.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
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

    public Map<Integer, List<UserProgress>> getUserProgressByLevel(User user){
        List<UserProgress> list = userProgressRepository.findUserProgressByLevel(user);
        Map<Integer, List<UserProgress>> groupedByLevel = list.stream()
                .collect(Collectors.groupingBy(UserProgress::getLevel));
        return groupedByLevel;
    }

    public boolean isVocabExistForUser(User user, Vocabulary vocab) {
        return userProgressRepository.existsByUserAndVocabulary(user, vocab);
    }
}
