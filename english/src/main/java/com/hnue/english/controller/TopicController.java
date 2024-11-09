package com.hnue.english.controller;

import com.hnue.english.dto.*;
import com.hnue.english.model.Topic;
import com.hnue.english.response.ApiResponse;
import com.hnue.english.response.ImportFromJson;
import com.hnue.english.service.CourseService;
import com.hnue.english.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/topics")
@RequiredArgsConstructor
public class TopicController {
    private final TopicService topicService;
    private final CourseService courseService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(false);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createTopic(@RequestParam String topicName, @RequestParam String description,
                                                      @RequestParam(required = false, defaultValue = "0") int courseId){
        if (topicName.isEmpty() || description.isEmpty()){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
        }
        if (topicService.existsByTopicName(topicName)){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Đã tồn tại tên chủ đề tiếng anh này", "Bad Request"));
        }
        TopicDTO topicDTO = TopicDTO.builder()
                .topicName(topicName).description(description).build();
        try {
            Topic t = topicService.createTopic(topicDTO, courseId);
            return ResponseEntity.status(201).body(ApiResponse.success(201, "", t));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
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
    public ResponseEntity<ApiResponse<?>> getTopics(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "1") int size,
                                                    @RequestParam(required = false) String topicName,
                                                    @RequestParam(required = false) String description,
                                                    @RequestParam(required = false, defaultValue = "0") int id,
                                                    @RequestParam(required = false) String sort){
        if (size < 1){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "size phải lớn hơn 0", "Bad Request"));
        }
        Page<Topic> topics = topicService.getTopics(page, size, topicName, description, id, sort);
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", topics));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateTopic(@PathVariable int id, @RequestParam String topicName, @RequestParam String description,
                                                      @RequestParam(required = false, defaultValue = "0") int courseId){
        try {
            if (topicName.isEmpty() || description.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            if (!topicService.preUpdateTopic(id, topicName)){
                if (topicService.existsByTopicName(topicName)){
                    return ResponseEntity.status(400).body(ApiResponse.error(400, "Đã tồn tại tên chủ đề tiếng anh này", "Bad Request"));
                }
            }
            TopicDTO topicDTO = TopicDTO.builder()
                    .topicName(topicName).description(description).build();
            Topic topic = topicService.updateTopic(id, topicDTO, courseId);
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

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<?>> createList(@RequestBody List<ListTopic> list){
        ImportFromJson t = new ImportFromJson();
        List<ListTopic> uniqueTopics = removeDuplicateTopicNames(list);
        List<String> existingTopicNames = topicService.checkExistingTopicNames(uniqueTopics);
        if (!existingTopicNames.isEmpty()) {
            t.setCountError(existingTopicNames.size());
            t.setCountSuccess(uniqueTopics.size() - existingTopicNames.size());
            t.setError(existingTopicNames);
            return ResponseEntity.status(400).body(ApiResponse.success(400, "Đã tồi tại tên", t));
        }

        List<String> nonExistingCourseId = courseService.checkNonExistingIdCourses(uniqueTopics);
        if (!nonExistingCourseId.isEmpty()){
            t.setCountError(nonExistingCourseId.size());
            t.setCountSuccess(uniqueTopics.size() - nonExistingCourseId.size());
            t.setError(nonExistingCourseId);
            return ResponseEntity.status(400).body(ApiResponse.success(400, "Không tồn tại course với id", t));
        }
        t.setCountError(0);
        t.setCountSuccess(uniqueTopics.size());
        topicService.saveAll(uniqueTopics);
        return ResponseEntity.status(201).body(ApiResponse.success(201, "Tạo thành công danh sách topic", t));
    }

    private List<ListTopic> removeDuplicateTopicNames(List<ListTopic> list) {
        Set<String> seenTopicNames = new HashSet<>();
        return list.stream()
                .filter(listTopic -> seenTopicNames.add(listTopic.getTopicName()))
                .collect(Collectors.toList());
    }

    private boolean isImageFile(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp") ||
                contentType.equals("image/bmp");
    }
}
