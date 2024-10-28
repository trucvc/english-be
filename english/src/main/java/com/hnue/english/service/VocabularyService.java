package com.hnue.english.service;

import com.hnue.english.dto.VocabDTO;
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

    public Vocabulary createVocab(VocabDTO vocabDTO){
        Vocabulary vocabulary = new Vocabulary(vocabDTO.getWord(), vocabDTO.getMeaning(), vocabDTO.getExampleSentence(), vocabDTO.getPronunciation());
        return vocabularyRepository.save(vocabulary);
    }

    public Vocabulary getVocab(int id){
        Vocabulary vocabulary = vocabularyRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tồn tại từ vựng với id: "+id));
        return vocabulary;
    }

    public List<Vocabulary> getAllVocab(){
        return vocabularyRepository.findAll();
    }

    public Vocabulary updateVocab(int id, VocabDTO vocabDTO){
        Vocabulary vocabulary = vocabularyRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tồn tại từ vựng với id: "+id));
        vocabulary.setWord(vocabDTO.getWord());
        vocabulary.setMeaning(vocabDTO.getMeaning());
        vocabulary.setPronunciation(vocabDTO.getPronunciation());
        vocabulary.setExampleSentence(vocabDTO.getExampleSentence());
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
}
