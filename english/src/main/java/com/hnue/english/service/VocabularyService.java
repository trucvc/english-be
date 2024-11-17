package com.hnue.english.service;

import com.hnue.english.dto.ListVocab;
import com.hnue.english.dto.VocabDTO;
import com.hnue.english.model.Topic;
import com.hnue.english.model.Vocabulary;
import com.hnue.english.repository.VocabularyRepository;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VocabularyService {
    private final VocabularyRepository vocabularyRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final TopicService topicService;

    public Vocabulary createVocab(VocabDTO vocabDTO, int topicId){
        Vocabulary vocabulary = new Vocabulary(vocabDTO.getWord(), vocabDTO.getMeaning(), vocabDTO.getExampleSentence(), vocabDTO.getPronunciation());
        vocabulary.setAudio(vocabDTO.getAudio());
        if (topicId != 0){
            Topic topic = topicService.getTopic(topicId);
            topic.add(vocabulary);
        }
        vocabulary.setCreatedAt(new Date());
        vocabulary.setUpdatedAt(new Date());
        return vocabularyRepository.save(vocabulary);
    }

    public Vocabulary getVocab(int id){
        Vocabulary vocabulary = vocabularyRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tồn tại từ vựng với id: "+id));
        return vocabulary;
    }

    public List<Vocabulary> getAllVocabs(String word, String meaning, int id, String sort){
        Specification<Vocabulary> spec = (root, query, criteriaBuilder) -> {
            var predicates= criteriaBuilder.conjunction();

            if (word != null && !word.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("word"), "%" + word + "%"));
            }

            if (meaning != null && !meaning.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("meaning"), "%" + meaning + "%"));
            }

            if (id != 0) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.equal(root.join("topic", JoinType.INNER).get("id"), id));
            }

            if (sort != null && !sort.trim().isEmpty()) {
                switch (sort) {
                    case "word":
                        query.orderBy(criteriaBuilder.asc(root.get("word")));
                        break;
                    case "-word":
                        query.orderBy(criteriaBuilder.desc(root.get("word")));
                        break;
                    case "meaning":
                        query.orderBy(criteriaBuilder.asc(root.get("meaning")));
                        break;
                    case "-meaning":
                        query.orderBy(criteriaBuilder.desc(root.get("meaning")));
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
        return vocabularyRepository.findAll(spec);
    }

    public Page<Vocabulary> getVocabs(int page, int size, String word, String meaning, int id, String sort){
        Specification<Vocabulary> spec = (root, query, criteriaBuilder) -> {
            var predicates= criteriaBuilder.conjunction();

            if (word != null && !word.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("word"), "%" + word + "%"));
            }

            if (meaning != null && !meaning.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("meaning"), "%" + meaning + "%"));
            }

            if (id != 0) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.equal(root.join("topic", JoinType.INNER).get("id"), id));
            }

            if (sort != null && !sort.trim().isEmpty()) {
                switch (sort) {
                    case "word":
                        query.orderBy(criteriaBuilder.asc(root.get("word")));
                        break;
                    case "-word":
                        query.orderBy(criteriaBuilder.desc(root.get("word")));
                        break;
                    case "meaning":
                        query.orderBy(criteriaBuilder.asc(root.get("meaning")));
                        break;
                    case "-meaning":
                        query.orderBy(criteriaBuilder.desc(root.get("meaning")));
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
        return vocabularyRepository.findAll(spec, pageable);
    }

    public List<Vocabulary> getAllVocab(){
        return vocabularyRepository.findAll();
    }

    public Vocabulary updateVocab(int id, VocabDTO vocabDTO, int topicId){
        Vocabulary vocabulary = vocabularyRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tồn tại từ vựng với id: "+id));
        vocabulary.setWord(vocabDTO.getWord());
        vocabulary.setMeaning(vocabDTO.getMeaning());
        vocabulary.setExampleSentence(vocabDTO.getExampleSentence());
        vocabulary.setPronunciation(vocabDTO.getPronunciation());
        vocabulary.setAudio(vocabDTO.getAudio());
        if (topicId != 0){
            Topic topic = topicService.getTopic(topicId);
            topic.add(vocabulary);
        }
        vocabulary.setUpdatedAt(new Date());
        return vocabularyRepository.save(vocabulary);
    }

    public void deleteVocab(int id){
        Vocabulary vocabulary = vocabularyRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tồn tại từ vựng với id: "+id));
        vocabularyRepository.delete(vocabulary);
    }

    public boolean preUpdateVocab(int id, String word){
        Vocabulary vocabulary = vocabularyRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tồn tại từ vựng với id: "+id));
        return vocabulary.getWord().equals(word);
    }

    public boolean existsByWord(String word){
        return vocabularyRepository.existsByWord(word);
    }

    public boolean existsById(int id){return vocabularyRepository.existsById(id);}

    public List<String> checkExistingIds(List<Integer> list) {
        return list.stream()
                .filter(id -> !existsById(id))
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    public List<String> checkExistingWords(List<ListVocab> list) {
        return list.stream()
                .filter(listVocab -> existsByWord(listVocab.getWord()))
                .map(ListVocab::getWord)
                .collect(Collectors.toList());
    }

    public void saveAll(List<ListVocab> list) {
        List<Vocabulary> coursesToSave = list.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        vocabularyRepository.saveAll(coursesToSave);
    }

    private Vocabulary convertToEntity(ListVocab listVocab) {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setWord(listVocab.getWord());
        vocabulary.setMeaning(listVocab.getMeaning());
        vocabulary.setExampleSentence(listVocab.getExampleSentence());
        vocabulary.setPronunciation(listVocab.getPronunciation());
        vocabulary.setAudio(listVocab.getAudio());
        Topic topic = topicService.getTopic(listVocab.getTopicId());
        topic.add(vocabulary);
        vocabulary.setCreatedAt(new Date());
        vocabulary.setUpdatedAt(new Date());
        return vocabulary;
    }

    public List<Vocabulary> getTwoRandomVocabs(Vocabulary vocabulary){
        return vocabularyRepository.findTwoRandomVocabs(vocabulary.getId(), vocabulary.getTopic().getId());
    }
}
