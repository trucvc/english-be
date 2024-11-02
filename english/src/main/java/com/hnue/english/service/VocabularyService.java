package com.hnue.english.service;

import com.hnue.english.dto.VocabDTO;
import com.hnue.english.model.Vocabulary;
import com.hnue.english.reponsitory.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabularyService {
    private final VocabularyRepository vocabularyRepository;
    private final FirebaseStorageService firebaseStorageService;

    public Vocabulary createVocab(VocabDTO vocabDTO){
        Vocabulary vocabulary = new Vocabulary(vocabDTO.getWord(), vocabDTO.getMeaning(), vocabDTO.getExampleSentence(), vocabDTO.getPronunciation());
        vocabulary.setCreatedAt(new Date());
        return vocabularyRepository.save(vocabulary);
    }

    public Vocabulary getVocab(int id){
        Vocabulary vocabulary = vocabularyRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tồn tại từ vựng với id: "+id));
        return vocabulary;
    }

    public Page<Vocabulary> getVocabs(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return vocabularyRepository.findAll(pageable);
    }

    public List<Vocabulary> getAllVocab(){
        return vocabularyRepository.findAll();
    }

    public Vocabulary updateVocab(int id, VocabDTO vocabDTO){
        Vocabulary vocabulary = vocabularyRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tồn tại từ vựng với id: "+id));
        vocabulary.setWord(vocabDTO.getWord());
        vocabulary.setMeaning(vocabDTO.getMeaning());
        if (!vocabDTO.getPronunciation().isEmpty()){
            firebaseStorageService.deleteFile(vocabulary.getPronunciation());
            vocabulary.setPronunciation(vocabDTO.getPronunciation());
        }
        vocabulary.setExampleSentence(vocabDTO.getExampleSentence());
        vocabulary.setUpdatedAt(new Date());
        return vocabularyRepository.save(vocabulary);
    }

    public Vocabulary uploadImageVocab(int id, VocabDTO vocabDTO){
        Vocabulary vocabulary = vocabularyRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tồn tại từ vựng với id: "+id));
        String url = "";
        try {
            url = firebaseStorageService.uploadFile(vocabDTO.getImage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (vocabulary.getImage() == null || vocabulary.getImage().isEmpty()){
            vocabulary.setImage(url);
        }else {
            try {
                firebaseStorageService.deleteFile(vocabulary.getImage());
                vocabulary.setImage(url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return vocabularyRepository.save(vocabulary);
    }

    public void deleteImageVocab(int id){
        Vocabulary vocabulary = vocabularyRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tồn tại từ vựng với id: "+id));
        if (vocabulary.getImage() != null && !vocabulary.getImage().isEmpty()){
            try {
                firebaseStorageService.deleteFile(vocabulary.getImage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            vocabulary.setImage("");
            vocabularyRepository.save(vocabulary);
        }
    }

    public void deleteVocab(int id){
        Vocabulary vocabulary = vocabularyRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tồn tại từ vựng với id: "+id));
        try {
            firebaseStorageService.deleteFile(vocabulary.getPronunciation());
            if (vocabulary.getImage() != null && !vocabulary.getImage().isEmpty()){
                firebaseStorageService.deleteFile(vocabulary.getImage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        vocabularyRepository.delete(vocabulary);
    }

    public boolean preUpdateVocab(int id, String word){
        Vocabulary vocabulary = vocabularyRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tồn tại từ vựng với id: "+id));
        return vocabulary.getWord().equals(word);
    }

    public boolean existsByWord(String word){
        return vocabularyRepository.existsByWord(word);
    }
}
