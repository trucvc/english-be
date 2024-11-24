package com.hnue.english.service;

import com.hnue.english.model.*;
import com.hnue.english.repository.TopicProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicProgressService {
    private final TopicProgressRepository topicProgressRepository;

    public void createTopicProgressIfNotExist(User user, Topic topic, int isCompleted, Date completedAt) {
        Optional<TopicProgress> existingProgress = topicProgressRepository.findByUserAndTopic(user, topic);

        if (existingProgress.isEmpty()) {
            TopicProgress newProgress = new TopicProgress(isCompleted, completedAt);
            newProgress.setUser(user);
            newProgress.setTopic(topic);
            topicProgressRepository.save(newProgress);
        }
    }

    public boolean allTopicAssignedToUser(User user, List<Topic> topics) {
        for (Topic topic : topics) {
            Optional<TopicProgress> topicProgress = topicProgressRepository.findByUserAndTopic(user, topic);
            if (topicProgress.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public List<TopicProgress> getAllTopicProgress(User user){
        return topicProgressRepository.getAllTopicProgressForUser(user);
    }

    public Map<String, Long> getTop10PopularTopics() {
        List<Object[]> results = topicProgressRepository.findTop10PopularTopics();
        List<Object[]> topResults = results.size() > 10 ? results.subList(0, 10) : results;
        return topResults.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }
}
