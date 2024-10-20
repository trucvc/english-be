package com.hnue.english.service;

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

    public void createTopic(Topic theTopic){
        topicRepository.save(theTopic);
    }

    public Topic getTopic(int id){
        Topic topic = topicRepository.getTopicWithVocabulary(id).orElseThrow(()-> new RuntimeException("Không tồn tại chủ đề với id: "+id));
        return topic;
    }

    public List<Topic> getAllTopic(){
        return topicRepository.findAll();
    }

    public Topic updateTopic(int id, Topic theTopic){
        Topic topic = topicRepository.getTopicWithVocabulary(id).orElseThrow(()-> new RuntimeException("Không tồn tại chủ đề với id: "+id));
        topic.setTopicName(theTopic.getTopicName());
        topic.setDescription(theTopic.getDescription());
        topic.setOrder(theTopic.getOrder());
        return topicRepository.save(topic);
    }

    public void deleteTopic(int id){
        Topic topic = topicRepository.getTopicWithVocabulary(id).orElseThrow(()-> new RuntimeException("Không tồn tại chủ đề với id: "+id));
        for (Vocabulary vocabulary : topic.getVocabularies()){
            vocabulary.setTopic(null);
        }
        topicRepository.delete(topic);
    }
}
