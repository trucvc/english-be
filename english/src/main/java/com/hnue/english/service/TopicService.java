package com.hnue.english.service;

import com.hnue.english.dto.TopicDTO;
import com.hnue.english.model.Topic;
import com.hnue.english.model.Vocabulary;
import com.hnue.english.reponsitory.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.DateTimeException;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicService {
    private final TopicRepository topicRepository;
    private final FirebaseStorageService firebaseStorageService;

    public Topic createTopic(TopicDTO topicDTO){
        Topic topic = new Topic();
        topic.setTopicName(topicDTO.getTopicName());
        topic.setDescription(topicDTO.getDescription());
        topic.setDisplayOrder(topicDTO.getOrder());
        topic.setContent(topicDTO.getContent());
        topic.setCreatedAt(new Date());
        return topicRepository.save(topic);
    }

    public Topic getTopic(int id){
        Topic topic = topicRepository.getTopicWithVocabulary(id).orElseThrow(()-> new RuntimeException("Không tồn tại chủ đề với id: "+id));
        return topic;
    }

    public List<Topic> getAllTopic(){
        return topicRepository.findAll();
    }

    public Page<Topic> getTopics(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return topicRepository.findAll(pageable);
    }

    public Topic updateTopic(int id, TopicDTO topicDTO){
        Topic topic = topicRepository.getTopicWithVocabulary(id).orElseThrow(()-> new RuntimeException("Không tồn tại chủ đề với id: "+id));
        topic.setTopicName(topicDTO.getTopicName());
        topic.setDescription(topicDTO.getDescription());
        topic.setDisplayOrder(topicDTO.getOrder());
        topic.setContent(topicDTO.getContent());
        topic.setUpdatedAt(new Date());
        return topicRepository.save(topic);
    }

    public Topic uploadImageTopic(int id, TopicDTO topicDTO){
        Topic topic = topicRepository.getTopicWithVocabulary(id).orElseThrow(()-> new RuntimeException("Không tồn tại chủ đề với id: "+id));
        String url = "";
        try {
            url = firebaseStorageService.uploadFile(topicDTO.getImage());
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException(e);
        }
        if (topic.getImage() == null || topic.getImage().isEmpty()){
            topic.setImage(url);
        }else{
            try {
                firebaseStorageService.deleteFile(topic.getImage());
                topic.setImage(url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return topicRepository.save(topic);
    }

    public void deleteImageTopic(int id){
        Topic topic = topicRepository.getTopicWithVocabulary(id).orElseThrow(()-> new RuntimeException("Không tồn tại chủ đề với id: "+id));
        if (topic.getImage() != null && !topic.getImage().isEmpty()){
            try {
                firebaseStorageService.deleteFile(topic.getImage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            topic.setImage("");
            topicRepository.save(topic);
        }
    }

    public void deleteTopic(int id){
        Topic topic = topicRepository.getTopicWithVocabulary(id).orElseThrow(()-> new RuntimeException("Không tồn tại chủ đề với id: "+id));
        try {
            if (topic.getImage() != null && !topic.getImage().isEmpty()){
                firebaseStorageService.deleteFile(topic.getImage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public boolean existsByDescription(String description){
        return topicRepository.existsByDescription(description);
    }
}
