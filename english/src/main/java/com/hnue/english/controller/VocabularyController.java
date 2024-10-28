package com.hnue.english.controller;

import com.hnue.english.dto.VocabDTO;
import com.hnue.english.model.Vocabulary;
import com.hnue.english.response.ApiResponse;
import com.hnue.english.service.VocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/vocabs")
@RequiredArgsConstructor
public class VocabularyController {
    private final VocabularyService vocabularyService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createVocab(@RequestParam String word, @RequestParam String meaning,
                                                      @RequestParam String exampleSentence, @RequestParam String pronunciation){
        if (word.trim().isEmpty() || meaning.trim().isEmpty() || exampleSentence.trim().isEmpty() || pronunciation.trim().isEmpty()){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
        }
        if (vocabularyService.existsByWord(word)){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Đã tồn tại từ vựng này", "Bad Request"));
        }
        VocabDTO vocabDTO = VocabDTO.builder()
                .word(word).meaning(meaning).exampleSentence(exampleSentence).pronunciation(pronunciation)
                .build();
        Vocabulary v = vocabularyService.createVocab(vocabDTO);
        return ResponseEntity.status(201).body(ApiResponse.success(201, "", v));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllVocab(){
        List<Vocabulary> vocabularies = vocabularyService.getAllVocab();
        if (vocabularies == null){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Không có từ vựng nào!", "Bad Request"));
        }else{
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", vocabularies));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getVocab(@PathVariable("id") int id){
        try {
            Vocabulary vocabulary = vocabularyService.getVocab(id);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", vocabulary));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateVocab(@PathVariable("id") int id, @RequestParam String word, @RequestParam String meaning,
                                                      @RequestParam String exampleSentence, @RequestParam String pronunciation){
        if (word.trim().isEmpty() || meaning.trim().isEmpty() || exampleSentence.trim().isEmpty() || pronunciation.trim().isEmpty()){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
        }
        if (!vocabularyService.preUpdateVocab(id, word)){
            if (vocabularyService.existsByWord(word)){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Đã tồn tại từ vựng này", "Bad Request"));
            }
        }
        VocabDTO vocabDTO = VocabDTO.builder()
                .word(word).meaning(meaning).exampleSentence(exampleSentence).pronunciation(pronunciation)
                .build();
        Vocabulary vocabulary = vocabularyService.updateVocab(id, vocabDTO);
        return ResponseEntity.status(201).body(ApiResponse.success(201, "", vocabulary));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deletaVocab(@PathVariable("id") int id){
        try {
            vocabularyService.deleteVocab(id);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "Xóa thành công từ vựng với id: "+id, null));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }
}
