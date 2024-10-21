package com.hnue.english.service;

import com.hnue.english.dto.TopicDTO;
import com.hnue.english.model.Topic;
import com.hnue.english.model.Vocabulary;
import com.hnue.english.reponsitory.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicService {
    private final TopicRepository topicRepository;

    public Topic createTopic(TopicDTO topicDTO){
        Topic topic = new Topic();
        topic.setTopicName(topicDTO.getTopicName());
        topic.setDescription(topicDTO.getDescription());
        topic.setOrder(topic.getOrder());
        return topicRepository.save(topic);
    }

    public Topic getTopic(int id){
        Topic topic = topicRepository.getTopicWithVocabulary(id).orElseThrow(()-> new RuntimeException("Không tồn tại chủ đề với id: "+id));
        return topic;
    }

    public List<Topic> getAllTopic(){
        return topicRepository.findAll();
    }

    public Topic updateTopic(int id, TopicDTO topicDTO){
        Topic topic = topicRepository.getTopicWithVocabulary(id).orElseThrow(()-> new RuntimeException("Không tồn tại chủ đề với id: "+id));
        topic.setTopicName(topicDTO.getTopicName());
        topic.setDescription(topicDTO.getDescription());
        topic.setOrder(topicDTO.getOrder());
        return topicRepository.save(topic);
    }

    public void deleteTopic(int id){
        Topic topic = topicRepository.getTopicWithVocabulary(id).orElseThrow(()-> new RuntimeException("Không tồn tại chủ đề với id: "+id));
        for (Vocabulary vocabulary : topic.getVocabularies()){
            vocabulary.setTopic(null);
        }
        topicRepository.delete(topic);
    }

    public boolean preUpdateTopic(int id, String topicName){
        Topic topic = topicRepository.getTopicWithVocabulary(id).orElseThrow(()-> new RuntimeException("Không tồn tại chủ đề với id: "+id));
        return topic.getTopicName().equals(topicName);
    }

    public boolean existsByTopicName(String topicName){
        return topicRepository.existsByTopicName(topicName);
    }
}
