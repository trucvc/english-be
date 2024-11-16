package com.hnue.english.service;

import com.hnue.english.dto.ListTopic;
import com.hnue.english.dto.ListVocab;
import com.hnue.english.dto.TopicDTO;
import com.hnue.english.model.Course;
import com.hnue.english.model.Topic;
import com.hnue.english.model.Vocabulary;
import com.hnue.english.repository.TopicRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicService {
    private final TopicRepository topicRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final CourseService courseService;

    public Topic createTopic(TopicDTO topicDTO, int courseId){
        Topic topic = new Topic();
        topic.setTopicName(topicDTO.getTopicName());
        topic.setDescription(topicDTO.getDescription());
        if (courseId != 0){
            Course course = courseService.getCourse(courseId);
            course.add(topic);
        }
        topic.setCreatedAt(new Date());
        topic.setUpdatedAt(new Date());
        return topicRepository.save(topic);
    }

    public Topic getTopic(int id){
        Topic topic = topicRepository.getTopicWithVocabulary(id).orElseThrow(()-> new RuntimeException("Không tồn tại chủ đề với id: "+id));
        return topic;
    }

    public List<Topic> getAllTopic(){
        return topicRepository.findAll();
    }

    public List<Topic> getAllWithVocabs(String topicName, String description, int courseId, String sort) {
        Specification<Topic> spec = (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            if (topicName != null && !topicName.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("topicName"), "%" + topicName + "%"));
            }

            if (description != null && !description.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("description"), "%" + description + "%"));
            }

            if (courseId != 0) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.equal(root.join("course", JoinType.INNER).get("id"), courseId));
            }

            Subquery<Integer> vocabularySubquery = query.subquery(Integer.class);
            Root<Vocabulary> vocabularyRoot = vocabularySubquery.from(Vocabulary.class);

            vocabularySubquery.select(vocabularyRoot.get("id"))
                    .where(criteriaBuilder.equal(vocabularyRoot.get("topic"), root));

            predicates = criteriaBuilder.and(predicates, criteriaBuilder.exists(vocabularySubquery));

            query.where(predicates);

            if (sort != null && !sort.trim().isEmpty()) {
                switch (sort) {
                    case "topicName":
                        query.orderBy(criteriaBuilder.asc(root.get("topicName")));
                        break;
                    case "-topicName":
                        query.orderBy(criteriaBuilder.desc(root.get("topicName")));
                        break;
                    case "description":
                        query.orderBy(criteriaBuilder.asc(root.get("description")));
                        break;
                    case "-description":
                        query.orderBy(criteriaBuilder.desc(root.get("description")));
                        break;
                    case "updatedAt":
                        query.orderBy(criteriaBuilder.asc(root.get("updatedAt")));
                        break;
                    case "-updatedAt":
                        query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));
                        break;
                    default:
                        break;
                }
            }

            return predicates;
        };
        return topicRepository.findAll(spec);
    }


    public Page<Topic> getTopics(int page, int size, String topicName, String description, int id, String sort){
        Specification<Topic> spec = (root, query, criteriaBuilder) -> {
            var predicates= criteriaBuilder.conjunction();

            if (topicName != null && !topicName.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("topicName"), "%" + topicName + "%"));
            }

            if (description != null && !description.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("description"), "%" + description + "%"));
            }

            if (id != 0) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.equal(root.join("course", JoinType.INNER).get("id"), id));
            }

            if (sort != null && !sort.trim().isEmpty()) {
                switch (sort) {
                    case "topicName":
                        query.orderBy(criteriaBuilder.asc(root.get("topicName")));
                        break;
                    case "-topicName":
                        query.orderBy(criteriaBuilder.desc(root.get("topicName")));
                        break;
                    case "description":
                        query.orderBy(criteriaBuilder.asc(root.get("description")));
                        break;
                    case "-description":
                        query.orderBy(criteriaBuilder.desc(root.get("description")));
                        break;
                    case "updatedAt":
                        query.orderBy(criteriaBuilder.asc(root.get("updatedAt")));
                        break;
                    case "-updatedAt":
                        query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));
                        break;
                    default:
                        break;
                }
            }

            return predicates;
        };
        Pageable pageable = PageRequest.of(page, size);
        return topicRepository.findAll(spec, pageable);
    }

    public Topic updateTopic(int id, TopicDTO topicDTO, int courseId){
        Topic topic = topicRepository.getTopicWithVocabulary(id).orElseThrow(()-> new RuntimeException("Không tồn tại chủ đề với id: "+id));
        topic.setTopicName(topicDTO.getTopicName());
        topic.setDescription(topicDTO.getDescription());
        if (courseId != 0){
            Course course = courseService.getCourse(courseId);
            course.add(topic);
        }
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

    public boolean existsById(int id){
        return topicRepository.existsById(id);
    }

    public List<String> checkNonExistingIdTopics(List<ListVocab> list) {
        return list.stream()
                .filter(listVocab -> !existsById(listVocab.getTopicId()))
                .map(listVocab -> String.valueOf(listVocab.getTopicId()))
                .collect(Collectors.toList());
    }

    public List<String> checkExistingTopicNames(List<ListTopic> list) {
        return list.stream()
                .filter(listTopic -> existsByTopicName(listTopic.getTopicName()))
                .map(ListTopic::getTopicName)
                .collect(Collectors.toList());
    }

    public void saveAll(List<ListTopic> list) {
        List<Topic> coursesToSave = list.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        topicRepository.saveAll(coursesToSave);
    }

    private Topic convertToEntity(ListTopic listTopic) {
        Topic topic = new Topic();
        topic.setTopicName(listTopic.getTopicName());
        topic.setDescription(listTopic.getDescription());
        Course course = courseService.getCourse(listTopic.getCourseId());
        course.add(topic);
        topic.setCreatedAt(new Date());
        topic.setUpdatedAt(new Date());
        return topic;
    }
}
