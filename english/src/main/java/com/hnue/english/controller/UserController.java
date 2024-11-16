package com.hnue.english.controller;

import com.hnue.english.dto.UserDTO;
import com.hnue.english.dto.VocabReview;
import com.hnue.english.dto.VocabSelected;
import com.hnue.english.model.User;
import com.hnue.english.model.UserProgress;
import com.hnue.english.model.Vocabulary;
import com.hnue.english.response.ApiResponse;
import com.hnue.english.response.ImportFromJson;
import com.hnue.english.response.LoginResponse;
import com.hnue.english.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final VocabularyService vocabularyService;
    private final UserProgressService userProgressService;
    private final CourseProgressService courseProgressService;
    private final TopicProgressService topicProgressService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(false);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createUser(@RequestParam String email, @RequestParam String password,
                                                     @RequestParam String fullName, @RequestParam String subscriptionPlan,
                                                     @RequestParam String role){
        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() || subscriptionPlan.isEmpty() || role.isEmpty()){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(emailRegex)) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Email không hợp lệ", "Bad Request"));
        }
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";
        if (!password.matches(passwordRegex)) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Mật khẩu phải có ít nhất một chữ hoa, một chữ thường, một chữ số, một ký tự đặc biệt và tối thiểu 8 ký tự", "Bad Request"));
        }
        if (userService.existsByEmail(email)){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Email này đã tồn tại", "Bad Request"));
        }
        UserDTO userDTO = UserDTO.builder()
                .email(email).password(password)
                .fullName(fullName).subscriptionPlan(subscriptionPlan)
                .role(role)
                .build();
        User u = userService.createUser(userDTO);
        return ResponseEntity.status(201).body(ApiResponse.success(201, "", u));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getUser(@PathVariable int id){
        try {
            User user = userService.getUser(id);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", user));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllUser(){
        List<User> users = userService.getAllUsers();
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", users));
    }

    @GetMapping("/page")
    public ResponseEntity<ApiResponse<?>> getUsers(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "1") int size,
                                                   @RequestParam(required = false) String email, @RequestParam(required = false) String fullName,
                                                   @RequestParam(required = false) String role, @RequestParam(required = false) String subscriptionPlan,
                                                   @RequestParam(required = false) String sort){
        if (size < 1){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "size phải lớn hơn 0", "Bad Request"));
        }
        Page<User> users = userService.getUsers(page, size, email, fullName, role, subscriptionPlan, sort);
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", users));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateUser(@PathVariable int id, @RequestParam(required = false) String password,
                                                     @RequestParam String fullName, @RequestParam String subscriptionPlan,
                                                     @RequestParam String role){
        try {
            if (fullName.isEmpty() || role.isEmpty() || subscriptionPlan.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            if (!password.isEmpty()){
                String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";
                if (!password.matches(passwordRegex)) {
                    return ResponseEntity.status(400).body(ApiResponse.error(400, "Mật khẩu phải có ít nhất một chữ hoa, một chữ thường, một chữ số, một ký tự đặc biệt và tối thiểu 8 ký tự", "Bad Request"));
                }
            }
            UserDTO userDTO = UserDTO.builder()
                    .password(password)
                    .fullName(fullName).subscriptionPlan(subscriptionPlan)
                    .role(role)
                    .build();
            User user = userService.updateUser(id, userDTO);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", user));
        }catch (Exception e){
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable int id){
        try {
            userService.deleteUser(id);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "Xóa thành công user với id: "+id, null));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @DeleteMapping("/sub/{id}")
    public ResponseEntity<ApiResponse<?>> deleteSubUser(@PathVariable int id){
        try {
            userService.deleteSubscription(id);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "Xóa thành công gói đăng kí của user với id: "+id, null));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @PostMapping ("/register")
    public ResponseEntity<?> register(@RequestParam String email,
                                      @RequestParam String password,    
                                      @RequestParam String fullName){
        try {
            if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            if (!email.matches(emailRegex)) {
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Email không hợp lệ", "Bad Request"));
            }
            String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";
            if (!password.matches(passwordRegex)) {
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Mật khẩu phải có ít nhất một chữ hoa, một chữ thường, một chữ số, một ký tự đặc biệt và tối thiểu 8 ký tự", "Bad Request"));
            }
            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(email);
            userDTO.setPassword(password);
            userDTO.setFullName(fullName);
            User user = userService.register(userDTO);
            return ResponseEntity.status(201).body(ApiResponse.success(201, "", user));
        }catch (Exception e){
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<?>> createList(@RequestBody List<UserDTO> list){
        ImportFromJson u = new ImportFromJson();
        List<UserDTO> uniqueUsers = removeDuplicateEmails(list);
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        List<String> email = new ArrayList<>();
        for (UserDTO dto : list){
            if (!dto.getEmail().matches(emailRegex)){
                email.add(dto.getEmail());
            }
        }
        if (!email.isEmpty()){
            u.setCountError(email.size());
            u.setCountSuccess(uniqueUsers.size() - email.size());
            u.setError(email);
            return ResponseEntity.status(400).body(ApiResponse.success(400, "Email không hợp lệ", u));
        }
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";
        List<String> pass = new ArrayList<>();
        for (UserDTO dto : list){
            if (!dto.getPassword().matches(passwordRegex)){
                pass.add(dto.getPassword());
            }
        }
        if (!pass.isEmpty()){
            u.setCountError(pass.size());
            u.setCountSuccess(uniqueUsers.size() - pass.size());
            u.setError(pass);
            return ResponseEntity.status(400).body(ApiResponse.success(400, "Mật khẩu phải có ít nhất một chữ hoa, một chữ thường, một chữ số, một ký tự đặc biệt và tối thiểu 8 ký tự", u));
        }
        List<String> existingEmails = userService.checkExistingEmails(uniqueUsers);
        if (!existingEmails.isEmpty()) {
            u.setCountError(existingEmails.size());
            u.setCountSuccess(uniqueUsers.size() - existingEmails.size());
            u.setError(existingEmails);
            return ResponseEntity.status(400).body(ApiResponse.success(400, "Đã tồi tại email", u));
        }else{
            u.setCountError(0);
            u.setCountSuccess(uniqueUsers.size());
            userService.saveAll(uniqueUsers);
            return ResponseEntity.status(201).body(ApiResponse.success(201, "Tạo thành công danh sách người dùng", u));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestParam String email, @RequestParam String password){
        try {
            String token = userService.login(email.trim(), password.trim());
            User user = userService.getUserByEmail(email);
            LoginResponse loginResponse = new LoginResponse(token, user);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", loginResponse));
        }catch (Exception e){
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @GetMapping("/account")
    public ResponseEntity<ApiResponse<?>> fetch(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        User user = userService.fetch(token);
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", user));
    }

    @GetMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refresh(HttpServletRequest request, HttpServletResponse response){
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Không thể tạo mới token", "Bad Request"));
        }
        String token = authHeader.substring(7);
        try {
            String newToken = userService.refresh(token);
            response.setHeader("Authorization", "Bearer " + newToken);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", newToken));
        }catch (Exception e){
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Token không hợp lệ", "Bad Request"));
        }
        String token = authHeader.substring(7);
        userService.logout(token);
        return ResponseEntity.status(200).body(ApiResponse.success(200, "Đăng xuất thành công", null));
    }

    private List<UserDTO> removeDuplicateEmails(List<UserDTO> userDTOList) {
        Set<String> seenEmails = new HashSet<>();
        return userDTOList.stream()
                .filter(userDTO -> seenEmails.add(userDTO.getEmail()))
                .collect(Collectors.toList());
    }

    @GetMapping("/learned_vocabulary")
    public ResponseEntity<ApiResponse<?>> getAllVocabForUser(HttpServletRequest request){
        try {
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);
            User user = userService.fetch(token);
            List<Vocabulary> vocabularies = new ArrayList<>();
            for (UserProgress us : userProgressService.getAllVocabForUser(user)){
                vocabularies.add(us.getVocabulary());
            }
            if (vocabularies.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Bạn chưa có từ vựng để ôn tập", "Bad Request"));
            }else{
                return ResponseEntity.status(200).body(ApiResponse.success(200, "", vocabularies));
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @PostMapping("/selected_vocab")
    public ResponseEntity<ApiResponse<?>> saveAllVocabForUser(HttpServletRequest request, @RequestBody VocabSelected list){
        try {
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);
            User user = userService.fetch(token);
            List<String> existingVocab = vocabularyService.checkExistingIds(list.getId());
            ImportFromJson v = new ImportFromJson();
            if (!existingVocab.isEmpty()){
                v.setCountError(existingVocab.size());
                v.setCountSuccess(list.getId().size() - existingVocab.size());
                v.setError(existingVocab);
                return ResponseEntity.status(400).body(ApiResponse.success(400, "Không tồn tại vocab với id", v));
            }
            List<Vocabulary> vocabularies = new ArrayList<>();
            for (int id : list.getId()){
                vocabularies.add(vocabularyService.getVocab(id));
            }
            List<String> existingUserVocab = new ArrayList<>();
            for (Vocabulary vo : vocabularies){
                if (userProgressService.isVocabExistForUser(user, vo)){
                    existingUserVocab.add(String.valueOf(vo.getId()));
                }
            }
            if (!existingUserVocab.isEmpty()){
                v.setCountError(existingUserVocab.size());
                v.setCountSuccess(list.getId().size() - existingUserVocab.size());
                v.setError(existingUserVocab);
                return ResponseEntity.status(400).body(ApiResponse.success(400, "Đã tồn tại vocab với id", v));
            }
            List<UserProgress> us = userProgressService.saveAllVocabForUser(user, vocabularies);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "Selected vocabulary saved for review", us));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @GetMapping("/review_stats")
    public ResponseEntity<ApiResponse<?>> countLevelsByUser(HttpServletRequest request){
        try {
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);
            User user = userService.fetch(token);
            Map<Integer, Long> level = new HashMap<>();
            level = userProgressService.countLevelsByUser(user);
            if (level.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Ban chưa học từ nào", "Bad Request"));
            }else{
                return ResponseEntity.status(200).body(ApiResponse.success(200, "", level));
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @GetMapping("/review_vocab")
    public ResponseEntity<ApiResponse<?>> reviewVocabByUser(HttpServletRequest request){
        try {
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);
            User user = userService.fetch(token);
            List<UserProgress> us = userProgressService.getAllVocabForUserWithExam(user);
            if (us.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Bạn chưa có từ vựng để ôn tập", "Bad Request"));
            }else{
                return ResponseEntity.status(200).body(ApiResponse.success(200, "", us));
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @PostMapping("/complete_review")
    public ResponseEntity<ApiResponse<?>> completeReview(HttpServletRequest request, @RequestBody VocabReview review){
        try {
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);
            User user = userService.fetch(token);
            Vocabulary vocabulary = vocabularyService.getVocab(review.getId());
            UserProgress us = userProgressService.getUserProgress(user, vocabulary);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", userProgressService.updateUserProgress(us, review.getStatus())));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @GetMapping("/wordbook")
    public ResponseEntity<ApiResponse<?>> wordbook(HttpServletRequest request){
        try {
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);
            User user = userService.fetch(token);
            Map<Integer, List<UserProgress>> level = userProgressService.getUserProgressByLevel(user);
            if (level.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Bạn chưa có từ vựng nào", "Bad Request"));
            }else{
                return ResponseEntity.status(200).body(ApiResponse.success(200, "", level));
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }
}
