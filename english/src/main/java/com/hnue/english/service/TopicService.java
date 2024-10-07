package com.hnue.english.service;

import com.hnue.english.model.Topic;
import com.hnue.english.model.Vocabulary;
import com.hnue.english.reponsitory.TopicReponsitory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicService {
    private final TopicReponsitory topicReponsitory;

    public void createTopic(Topic theTopic){
        topicReponsitory.save(theTopic);
    }

    public Topic getTopic(int id){
        Topic topic = topicReponsitory.getTopicWithVocabulary(id).orElseThrow(()-> new DateTimeException("Không tồn tại chủ đề với id: "+id));
        return topic;
    }

    public List<Topic> getAllTopic(){
        return topicReponsitory.findAll();
    }

    public Topic updateTopic(int id, Topic theTopic){
        Topic topic = topicReponsitory.getTopicWithVocabulary(id).orElseThrow(()-> new DateTimeException("Không tồn tại chủ đề với id: "+id));
        topic.setTopicName(theTopic.getTopicName());
        topic.setDescription(theTopic.getDescription());
        topic.setOrder(theTopic.getOrder());
        return topicReponsitory.save(topic);
    }

    public void deleteTopic(int id){
        Topic topic = topicReponsitory.getTopicWithVocabulary(id).orElseThrow(()-> new DateTimeException("Không tồn tại chủ đề với id: "+id));
        for (Vocabulary vocabulary : topic.getVocabularies()){
            vocabulary.setTopic(null);
        }
        topicReponsitory.delete(topic);
    }
}
