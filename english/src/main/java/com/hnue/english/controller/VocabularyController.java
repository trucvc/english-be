package com.hnue.english.controller;

import com.hnue.english.dto.ListTopic;
import com.hnue.english.dto.ListVocab;
import com.hnue.english.dto.VocabDTO;
import com.hnue.english.model.Vocabulary;
import com.hnue.english.response.ApiResponse;
import com.hnue.english.response.ImportFromJson;
import com.hnue.english.service.ExternalApiService;
import com.hnue.english.service.FirebaseStorageService;
import com.hnue.english.service.TopicService;
import com.hnue.english.service.VocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/vocabs")
@RequiredArgsConstructor
public class VocabularyController {
    private final VocabularyService vocabularyService;
    private final ExternalApiService apiService;
    private final FirebaseStorageService firebaseStorageService;
    private final TopicService topicService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(false);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createVocab(@RequestParam String word, @RequestParam String meaning,
                                                      @RequestParam(required = false, defaultValue = "0") int topicId){
        if (word.isEmpty() || meaning.isEmpty()){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
        }
        if (vocabularyService.existsByWord(word)){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Đã tồn tại từ vựng này", "Bad Request"));
        }
        try {
            Map<String, Object> map = apiService.getWordDefinitionAsMap(word);
            List<Map<String, Object>> pronunciationList = (List<Map<String, Object>>) map.get("pronunciation");
            List<Map<String, Object>> definitionList = (List<Map<String, Object>>) map.get("definition");
            VocabDTO vocabDTO = VocabDTO.builder()
                    .word(word).meaning(meaning)
                    .exampleSentence((String) definitionList.getFirst().get("text"))
                    .pronunciation((String) pronunciationList.getFirst().get("pron"))
                    .audio((String) pronunciationList.getFirst().get("url"))
                    .build();
            Vocabulary v = vocabularyService.createVocab(vocabDTO, topicId);
            return ResponseEntity.status(201).body(ApiResponse.success(201, "", v));
            //return ResponseEntity.status(201).body(ApiResponse.success(201, "", pronunciationList.get(0).get("url")));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
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
    public ResponseEntity<ApiResponse<?>> getVocabs(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "1") int size,
                                                    @RequestParam(required = false) String word,
                                                    @RequestParam(required = false) String meaning,
                                                    @RequestParam(required = false, defaultValue = "0") int id,
                                                    @RequestParam(required = false) String sort){
        if (size < 1){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "size phải lớn hơn 0", "Bad Request"));
        }
        Page<Vocabulary> vocabs = vocabularyService.getVocabs(page, size, word, meaning, id, sort);
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

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<?>> createList(@RequestBody List<ListVocab> list){
        ImportFromJson v = new ImportFromJson();
        List<ListVocab> uniqueVocabs = removeDuplicateWords(list);
        List<String> existingWords = vocabularyService.checkExistingWords(uniqueVocabs);
        if (!existingWords.isEmpty()) {
            v.setCountError(existingWords.size());
            v.setCountSuccess(uniqueVocabs.size() - existingWords.size());
            v.setError(existingWords);
            return ResponseEntity.status(400).body(ApiResponse.success(400, "Đã tồi tại tên", v));
        }

        List<String> nonExistingTopicId = topicService.checkNonExistingIdTopics(uniqueVocabs);
        if (!nonExistingTopicId.isEmpty()){
            v.setCountError(nonExistingTopicId.size());
            v.setCountSuccess(uniqueVocabs.size() - nonExistingTopicId.size());
            v.setError(nonExistingTopicId);
            return ResponseEntity.status(400).body(ApiResponse.success(400, "Không tồn tại topic với id", v));
        }

        List<String> checkValidWords = apiService.checkValidWords(uniqueVocabs);
        if (!checkValidWords.isEmpty()){
            v.setCountError(checkValidWords.size());
            v.setCountSuccess(uniqueVocabs.size() - checkValidWords.size());
            v.setError(checkValidWords);
            return ResponseEntity.status(400).body(ApiResponse.success(400, "Từ không hợp lệ", v));
        }
        List<ListVocab> vocabs = words(uniqueVocabs);
        v.setCountError(0);
        v.setCountSuccess(uniqueVocabs.size());
        vocabularyService.saveAll(vocabs);
        return ResponseEntity.status(201).body(ApiResponse.success(201, "Tạo thành công danh sách vocab", v));
    }

    private List<ListVocab> removeDuplicateWords(List<ListVocab> list) {
        Set<String> seenWords = new HashSet<>();
        return list.stream()
                .filter(listVocab -> seenWords.add(listVocab.getWord()))
                .collect(Collectors.toList());
    }

    public List<ListVocab> words(List<ListVocab> list){
        for (ListVocab vocab : list){
            Map<String, Object> map = apiService.getWordDefinitionAsMap(vocab.getWord());
            List<Map<String, Object>> pronunciationList = (List<Map<String, Object>>) map.get("pronunciation");
            List<Map<String, Object>> definitionList = (List<Map<String, Object>>) map.get("definition");
            vocab.setExampleSentence((String) definitionList.getFirst().get("text"));
            vocab.setPronunciation((String) pronunciationList.getFirst().get("pron"));
            vocab.setAudio((String) pronunciationList.getFirst().get("url"));
        }
        return list;
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
