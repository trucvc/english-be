package com.hnue.english.controller;

import com.hnue.english.dto.VocabDTO;
import com.hnue.english.model.Vocabulary;
import com.hnue.english.response.ApiResponse;
import com.hnue.english.service.FirebaseStorageService;
import com.hnue.english.service.VocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/vocabs")
@RequiredArgsConstructor
public class VocabularyController {
    private final VocabularyService vocabularyService;
    private final FirebaseStorageService firebaseStorageService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(false);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createVocab(@RequestParam String word, @RequestParam String meaning,
                                                      @RequestParam String exampleSentence, @RequestParam MultipartFile pronunciation){
        if (word.isEmpty() || meaning.isEmpty() || exampleSentence.isEmpty() || pronunciation.isEmpty()){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
        }
        if (vocabularyService.existsByWord(word)){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Đã tồn tại từ vựng này", "Bad Request"));
        }
        String contentType = pronunciation.getContentType();
        if (contentType == null || !isAudioFile(contentType)) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, "File không phải file âm thanh", "Bad Request"));
        }
        String url = "";
        try {
            url = firebaseStorageService.uploadFile(pronunciation);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
        VocabDTO vocabDTO = VocabDTO.builder()
                .word(word).meaning(meaning).exampleSentence(exampleSentence).pronunciation(url)
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

    @GetMapping("/page")
    public ResponseEntity<ApiResponse<?>> getVocabs(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "1") int size){
        if (size < 1){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "size phải lớn hơn 0", "Bad Request"));
        }
        Page<Vocabulary> vocabs = vocabularyService.getVocabs(page, size);
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", vocabs));
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
                                                      @RequestParam String exampleSentence, @RequestParam(required = false) MultipartFile pronunciation){
        try {
            if (word.isEmpty() || meaning.isEmpty() || exampleSentence.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            if (!vocabularyService.preUpdateVocab(id, word)){
                if (vocabularyService.existsByWord(word)){
                    return ResponseEntity.status(400).body(ApiResponse.error(400, "Đã tồn tại từ vựng này", "Bad Request"));
                }
            }
            String url = "";
            if (!pronunciation.isEmpty()){
                try {
                    url = firebaseStorageService.uploadFile(pronunciation);
                } catch (Exception e) {
                    return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
                }
            }
            VocabDTO vocabDTO = new VocabDTO();
            if (url.isEmpty()){
                vocabDTO = VocabDTO.builder()
                        .word(word).meaning(meaning).exampleSentence(exampleSentence).pronunciation("")
                        .build();
            }else{
                vocabDTO = VocabDTO.builder()
                        .word(word).meaning(meaning).exampleSentence(exampleSentence).pronunciation(url)
                        .build();
            }

            Vocabulary vocabulary = vocabularyService.updateVocab(id, vocabDTO);
            return ResponseEntity.status(201).body(ApiResponse.success(201, "", vocabulary));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteVocab(@PathVariable("id") int id){
        try {
            vocabularyService.deleteVocab(id);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "Xóa thành công từ vựng với id: "+id, null));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @PostMapping("/image/{id}")
    public ResponseEntity<ApiResponse<?>> uploadImageVocab(@PathVariable("id") int id, @RequestParam MultipartFile image){
        try {
            if (image.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            String contentType = image.getContentType();
            if (contentType == null || !isImageFile(contentType)){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "File không phải là ảnh", "Bad Request"));
            }
            VocabDTO vocabDTO = VocabDTO.builder()
                    .image(image).build();
            Vocabulary vocabulary = vocabularyService.uploadImageVocab(id, vocabDTO);
            return ResponseEntity.status(201).body(ApiResponse.success(201, "", vocabulary));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @DeleteMapping("/image/{id}")
    public ResponseEntity<ApiResponse<?>> deleteImageVocab(@PathVariable("id") int id){
        try {
            vocabularyService.deleteImageVocab(id);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "Xóa thành công ảnh từ vựng với id: "+id, null));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    private boolean isAudioFile(String contentType) {
        return contentType.equals("audio/mpeg") ||
                contentType.equals("audio/wav") ||
                contentType.equals("audio/mp3") ||
                contentType.equals("audio/ogg") ||
                contentType.equals("audio/flac");
    }

    private boolean isImageFile(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp") ||
                contentType.equals("image/bmp");
    }
}
