package com.hnue.english.service;

import com.hnue.english.model.Vocabulary;
import com.hnue.english.reponsitory.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabularyService {
    private final VocabularyRepository vocabularyRepository;

    public void createVocab(Vocabulary theVocabulary){
        vocabularyRepository.save(theVocabulary);
    }

    public Vocabulary getVocab(int id){
        Vocabulary vocabulary = vocabularyRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tồn tại từ vựng với id: "+id));
        return vocabulary;
    }

    public List<Vocabulary> getAllVocab(){
        return vocabularyRepository.findAll();
    }

    public Vocabulary updateVocab(int id, Vocabulary theVocab){
        Vocabulary vocabulary = vocabularyRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tồn tại từ vựng với id: "+id));
        vocabulary.setWord(theVocab.getWord());
        vocabulary.setMeaning(theVocab.getMeaning());
        vocabulary.setPronunciation(theVocab.getPronunciation());
        vocabulary.setExampleSentence(theVocab.getExampleSentence());
        return vocabularyRepository.save(vocabulary);
    }

    public void deleteVocab(int id){
        Vocabulary vocabulary = vocabularyRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tồn tại từ vựng với id: "+id));
        vocabularyRepository.delete(vocabulary);
    }
}
