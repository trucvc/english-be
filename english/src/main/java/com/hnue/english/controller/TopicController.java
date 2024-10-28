package com.hnue.english.controller;

import com.hnue.english.dto.TopicDTO;
import com.hnue.english.model.Topic;
import com.hnue.english.response.ApiResponse;
import com.hnue.english.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/topics")
@RequiredArgsConstructor
public class TopicController {
    private final TopicService topicService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createTopic(@RequestParam String topicName, @RequestParam(required = false) String description, @RequestParam(defaultValue = "0") int order){
        if (topicName.trim().isEmpty() || order == 0){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
        }
        if (topicService.existsByTopicName(topicName)){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Đã tồn tại tên chủ đề này", "Bad Request"));
        }
        TopicDTO topicDTO = TopicDTO.builder()
                .topicName(topicName).description(description).order(order).build();
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

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateTopic(@PathVariable int id, @RequestParam String topicName, @RequestParam(required = false) String description, @RequestParam(defaultValue = "0") int order){
        if (topicName.trim().isEmpty() || order == 0){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
        }
        if (!topicService.preUpdateTopic(id, topicName)){
            if (topicService.existsByTopicName(topicName)){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Đã tồn tại tên chủ đề này", "Bad Request"));
            }
        }
        TopicDTO topicDTO = TopicDTO.builder()
                .topicName(topicName).description(description).order(order).build();
        Topic topic = topicService.updateTopic(id, topicDTO);
        return ResponseEntity.status(201).body(ApiResponse.success(201, "", topic));
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
}
