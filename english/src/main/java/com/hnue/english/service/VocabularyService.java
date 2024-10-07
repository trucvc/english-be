package com.hnue.english.service;

import com.hnue.english.model.Vocabulary;
import com.hnue.english.reponsitory.VocabularyReponsitory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabularyService {
    private final VocabularyReponsitory vocabularyReponsitory;

    public void createVocab(Vocabulary theVocabulary){
        vocabularyReponsitory.save(theVocabulary);
    }

    public Vocabulary getVocab(int id){
        Vocabulary vocabulary = vocabularyReponsitory.findById(id).orElseThrow(()-> new DateTimeException("Không tồn tại từ vựng với id: "+id));
        return vocabulary;
    }

    public List<Vocabulary> getAllVocab(){
        return vocabularyReponsitory.findAll();
    }

    public Vocabulary updateVocab(int id, Vocabulary theVocab){
        Vocabulary vocabulary = vocabularyReponsitory.findById(id).orElseThrow(()-> new DateTimeException("Không tồn tại từ vựng với id: "+id));
        vocabulary.setWord(theVocab.getWord());
        vocabulary.setMeaning(theVocab.getMeaning());
        vocabulary.setPronunciation(theVocab.getPronunciation());
        vocabulary.setExampleSentence(theVocab.getExampleSentence());
        return vocabularyReponsitory.save(vocabulary);
    }

    public void deleteVocab(int id){
        Vocabulary vocabulary = vocabularyReponsitory.findById(id).orElseThrow(()-> new DateTimeException("Không tồn tại từ vựng với id: "+id));
        vocabularyReponsitory.delete(vocabulary);
    }
}
