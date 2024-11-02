package com.hnue.english.controller;

import com.hnue.english.dto.TopicDTO;
import com.hnue.english.dto.VocabDTO;
import com.hnue.english.model.Topic;
import com.hnue.english.model.Vocabulary;
import com.hnue.english.response.ApiResponse;
import com.hnue.english.service.FirebaseStorageService;
import com.hnue.english.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/topics")
@RequiredArgsConstructor
public class TopicController {
    private final TopicService topicService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(false);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createTopic(@RequestParam String topicName, @RequestParam String description,
                                                      @RequestParam(defaultValue = "0") int order, @RequestParam String content){
        if (topicName.isEmpty() || description.isEmpty() || content.isEmpty() || order == 0){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
        }
        List<String> error = new ArrayList<>();
        if (topicService.existsByTopicName(topicName)){
            error.add("Đã tồn tại chủ đề tới tên tiếng anh này!");
        }
        if (topicService.existsByDescription(description)) {
            error.add("Đã tồn tại chủ đề tới tên tiếng việt này!");
        }
        if (!error.isEmpty()){
            return ResponseEntity.status(400).body(ApiResponse.error(400, error, "Bad Request"));
        }
        TopicDTO topicDTO = TopicDTO.builder()
                .topicName(topicName).description(description).order(order).content(content).build();
        Topic t = topicService.createTopic(topicDTO);
        return ResponseEntity.status(201).body(ApiResponse.success(201, "", t));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getTopic(@PathVariable int id){
        try {
            Topic topic = topicService.getTopic(id);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", topic));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllTopic(){
        List<Topic> topics = topicService.getAllTopic();
        if (topics == null){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Không có chủ đề nào!", "Bad Request"));
        }else{
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", topics));
        }
    }

    @GetMapping("/page")
    public ResponseEntity<ApiResponse<?>> getTopics(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "1") int size){
        if (size < 1){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "size phải lớn hơn 0", "Bad Request"));
        }
        Page<Topic> topics = topicService.getTopics(page, size);
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", topics));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateTopic(@PathVariable int id, @RequestParam String topicName, @RequestParam String description,
                                                      @RequestParam(defaultValue = "0") int order, @RequestParam String content){
        try {
            if (topicName.isEmpty() || description.isEmpty() || content.isEmpty() || order == 0){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            if (!topicService.preUpdateTopic(id, topicName)){
                if (topicService.existsByTopicName(topicName)){
                    return ResponseEntity.status(400).body(ApiResponse.error(400, "Đã tồn tại tên chủ đề tiếng anh này", "Bad Request"));
                }
            }
            TopicDTO topicDTO = TopicDTO.builder()
                    .topicName(topicName).description(description).order(order).content(content).build();
            Topic topic = topicService.updateTopic(id, topicDTO);
            return ResponseEntity.status(201).body(ApiResponse.success(201, "", topic));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @PostMapping("/image/{id}")
    public ResponseEntity<ApiResponse<?>> uploadImageTopic(@PathVariable("id") int id, @RequestParam MultipartFile image){
        try {
            if (image.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            String contentType = image.getContentType();
            if (contentType == null || !isImageFile(contentType)){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "File không phải là ảnh", "Bad Request"));
            }
            TopicDTO topicDTO = TopicDTO.builder()
                    .image(image).build();
            Topic topic = topicService.uploadImageTopic(id, topicDTO);
            return ResponseEntity.status(201).body(ApiResponse.success(201, "", topic));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @DeleteMapping("/image/{id}")
    public ResponseEntity<ApiResponse<?>> deleteImageVocab(@PathVariable("id") int id){
        try {
            topicService.deleteImageTopic(id);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "Xóa thành công ảnh chủ đề với id: "+id, null));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteTopic(@PathVariable int id){
        try {
            topicService.deleteTopic(id);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "Xóa thành công chủ đề với id: "+id, null));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    private boolean isImageFile(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp") ||
                contentType.equals("image/bmp");
    }
}
