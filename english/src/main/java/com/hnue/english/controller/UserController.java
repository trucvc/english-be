package com.hnue.english.controller;

import com.hnue.english.dto.Otp;
import com.hnue.english.dto.UserDTO;
import com.hnue.english.dto.VocabReview;
import com.hnue.english.dto.VocabSelected;
import com.hnue.english.model.*;
import com.hnue.english.response.ApiResponse;
import com.hnue.english.response.ExamResponse;
import com.hnue.english.response.ImportFromJson;
import com.hnue.english.response.LoginResponse;
import com.hnue.english.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final CourseService courseService;
    private final TopicService topicService;
    private final VocabularyService vocabularyService;
    private final UserProgressService userProgressService;
    private final CourseProgressService courseProgressService;
    private final TopicProgressService topicProgressService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

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
        String usernameRegex = "^[A-Za-zÀ-ỹả-ỹ ]{3,255}$";
        if (!fullName.matches(usernameRegex)) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Tên không hợp lệ", "Bad Request"));
        }
        String emailRegex = "^[A-Za-z][A-Za-z0-9_+&*-]*(?:\\.[A-Za-z0-9_+&*-]+)*@"
                + "(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,7}$";
        if (!email.matches(emailRegex)) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Email không hợp lệ", "Bad Request"));
        }
        String passwordRegex = "^(?!.*\\s)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";
        if (!password.matches(passwordRegex)) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Mật khẩu phải có ít nhất một chữ hoa, một chữ thường, một chữ số, một ký tự đặc biệt và tối thiểu 8 ký tự và không chứa khoảng trắng", "Bad Request"));
        }
        if (email.equals(password)){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Email và mật khẩu không được trùng nhau", "Bad Request"));
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
    public ResponseEntity<ApiResponse<?>> updateUser(@PathVariable int id,
                                                     @RequestParam String fullName, @RequestParam String subscriptionPlan,
                                                     @RequestParam String role){
        try {
            if (fullName.isEmpty() || role.isEmpty() || subscriptionPlan.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            String usernameRegex = "^[A-Za-zÀ-ỹả-ỹ ]{3,255}$";
            if (!fullName.matches(usernameRegex)) {
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Tên không hợp lệ", "Bad Request"));
            }
            UserDTO userDTO = UserDTO.builder()
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
            String usernameRegex = "^[A-Za-zÀ-ỹả-ỹ ]{3,255}$";
            if (!fullName.matches(usernameRegex)) {
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Tên không hợp lệ", "Bad Request"));
            }
            String emailRegex = "^[A-Za-z][A-Za-z0-9_+&*-]*(?:\\.[A-Za-z0-9_+&*-]+)*@"
                    + "(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,7}$";
            if (!email.matches(emailRegex)) {
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Email không hợp lệ", "Bad Request"));
            }
            String passwordRegex = "^(?!.*\\s)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";
            if (!password.matches(passwordRegex)) {
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Mật khẩu phải có ít nhất một chữ hoa, một chữ thường, một chữ số, một ký tự đặc biệt và tối thiểu 8 ký tự và không chứa khoảng trắng", "Bad Request"));
            }
            if (email.equals(password)){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Email và mật khẩu không được trùng nhau", "Bad Request"));
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
        String usernameRegex = "^[A-Za-zÀ-ỹả-ỹ ]{3,255}$";
        List<String> name = new ArrayList<>();
        for (UserDTO dto : uniqueUsers){
            if (!dto.getFullName().matches(usernameRegex)){
                name.add(dto.getFullName());
            }
        }
        if (!name.isEmpty()){
            u.setCountError(name.size());
            u.setCountSuccess(uniqueUsers.size() - name.size());
            u.setError(name);
            return ResponseEntity.status(400).body(ApiResponse.success(400, "Tên không hợp lệ", u));
        }
        String emailRegex = "^[A-Za-z][A-Za-z0-9_+&*-]*(?:\\.[A-Za-z0-9_+&*-]+)*@"
                + "(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,7}$";
        List<String> email = new ArrayList<>();
        for (UserDTO dto : uniqueUsers){
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
        String passwordRegex = "^(?!.*\\s)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";
        List<String> pass = new ArrayList<>();
        for (UserDTO dto : uniqueUsers){
            if (!dto.getPassword().matches(passwordRegex)){
                pass.add(dto.getPassword());
            }
        }
        if (!pass.isEmpty()){
            u.setCountError(pass.size());
            u.setCountSuccess(uniqueUsers.size() - pass.size());
            u.setError(pass);
            return ResponseEntity.status(400).body(ApiResponse.success(400, "Mật khẩu phải có ít nhất một chữ hoa, một chữ thường, một chữ số, một ký tự đặc biệt và tối thiểu 8 ký tự và không chứa khoảng trắng", u));
        }
        List<String> checkEmailAndPass = new ArrayList<>();
        for (UserDTO dto : uniqueUsers){
            if (dto.getEmail().equals(dto.getPassword())){
                checkEmailAndPass.add(dto.getPassword());
            }
        }
        if (!checkEmailAndPass.isEmpty()){
            u.setCountError(checkEmailAndPass.size());
            u.setCountSuccess(uniqueUsers.size() - checkEmailAndPass.size());
            u.setError(checkEmailAndPass);
            return ResponseEntity.status(400).body(ApiResponse.success(400, "Email và mật khẩu không được trùng nhau", u));
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
            List<Vocabulary> learn = new ArrayList<>();
            for (int id : list.getId()){
                vocabularies.add(vocabularyService.getVocab(id));
                learn.add(vocabularyService.getVocab(id));
            }
//            List<String> existingUserVocab = new ArrayList<>();
            for (Vocabulary vo : vocabularies){
                if (userProgressService.isVocabExistForUser(user, vo)){
                    learn.remove(vo);
                }
            }
//            if (!existingUserVocab.isEmpty()){
//                v.setCountError(existingUserVocab.size());
//                v.setCountSuccess(list.getId().size() - existingUserVocab.size());
//                v.setError(existingUserVocab);
//                return ResponseEntity.status(400).body(ApiResponse.success(400, "Đã tồn tại vocab với id", v));
//            }
            if (!learn.isEmpty()){
                List<UserProgress> us = userProgressService.saveAllVocabForUser(user, learn);

//                List<Topic> topics = topicService.getAllTopicWithVocab();
                for (UserProgress u : us){
//                    List<Vocabulary> vocab = topic.getVocabularies();
//                    if (userProgressService.allVocabulariesAssignedToUser(user, vocab)){
                        topicProgressService.createTopicProgressIfNotExist(user, u.getVocabulary().getTopic(), 1, new Date());
                    //}
                }

                List<Course> courses = courseService.getAllCourseWithTopic();
                for (Course course : courses){
                    List<Topic> topic = course.getTopics();
                    if (topicProgressService.allTopicAssignedToUser(user, topic)){
                        courseProgressService.createCourseProgressIfNotExist(user, course, 1, new Date());
                    }
                }
            }

            return ResponseEntity.status(200).body(ApiResponse.success(200, "Selected vocabulary saved for review", null));
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
                int n = 1;
                long count = 0;
                while (n < 6){
                    level.put(n,count);
                    n++;
                }
                return ResponseEntity.status(200).body(ApiResponse.success(200, "", level));
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
            List<ExamResponse> exam = new ArrayList<>();
            for (UserProgress u : us){
                List<Vocabulary> incorrect = vocabularyService.getTwoRandomVocabs(u.getVocabulary());
                ExamResponse ex = new ExamResponse(random(), u.getVocabulary(), incorrect);
                exam.add(ex);
            }
            if (exam.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Bạn chưa có từ vựng để ôn tập", "Bad Request"));
            }else{
                return ResponseEntity.status(200).body(ApiResponse.success(200, "", exam));
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @PostMapping("/complete_review")
    public ResponseEntity<ApiResponse<?>> completeReview(HttpServletRequest request, @RequestBody List<VocabReview> reviews){
        try {
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);
            User user = userService.fetch(token);
            List<UserProgress> progresses = new ArrayList<>();
            for (VocabReview review : reviews){
                Vocabulary vocabulary = vocabularyService.getVocab(review.getId());
                UserProgress us = userProgressService.getUserProgress(user, vocabulary);
                UserProgress u = userProgressService.updateUserProgress(us, review.getStatus());
                progresses.add(u);
            }
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", progresses));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @GetMapping("/wordbook")
    public ResponseEntity<ApiResponse<?>> wordbook(HttpServletRequest request,
                                                   @RequestParam(required = false) String search, @RequestParam(required = false, defaultValue = "0") int level){
        try {
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);
            User user = userService.fetch(token);
            List<UserProgress> us = userProgressService.getUserProgressByLevel(search, level, user);
//            if (level.isEmpty()){
//                return ResponseEntity.status(400).body(ApiResponse.error(400, "Bạn chưa có từ vựng nào", "Bad Request"));
//            }else{
                return ResponseEntity.status(200).body(ApiResponse.success(200, "", us));
            //}
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @DeleteMapping("/wordbook/{id}")
    public ResponseEntity<ApiResponse<?>> deleteWordbook(@PathVariable int id, HttpServletRequest request){
        try {
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);
            User user = userService.fetch(token);
            userProgressService.deleteUS(user, id);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "Xoa thanh cong vocab trong workbook voi id "+id, null));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @GetMapping("/course_progress")
    public ResponseEntity<ApiResponse<?>> courseProgress(HttpServletRequest request){
        try {
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);
            User user = userService.fetch(token);
            List<CourseProgress> courseProgresses = courseProgressService.getAllCourseProgress(user);
            if (courseProgresses.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Chưa có khóa học nào hoàn thành", "Bad Request"));
            }else{
                return ResponseEntity.status(200).body(ApiResponse.success(200, "", courseProgresses));
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @GetMapping("/topic_progress")
    public ResponseEntity<ApiResponse<?>> topicProgress(HttpServletRequest request){
        try {
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);
            User user = userService.fetch(token);
            List<TopicProgress> topicProgresses = topicProgressService.getAllTopicProgress(user);
            if (topicProgresses.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Chưa có chủ đề nào hoàn thành", "Bad Request"));
            }else{
                return ResponseEntity.status(200).body(ApiResponse.success(200, "", topicProgresses));
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @PostMapping("/change_password")
    public ResponseEntity<ApiResponse<?>> changePassword(HttpServletRequest request, @RequestParam String oldPassword,
                                                         @RequestParam String newPassword){
        try {
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);
            User user = userService.fetch(token);
            if (oldPassword.isEmpty() || newPassword.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            String passwordRegex = "^(?!.*\\s)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";
            if (!newPassword.matches(passwordRegex)) {
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Mật khẩu phải có ít nhất một chữ hoa, một chữ thường, một chữ số, một ký tự đặc biệt và tối thiểu 8 ký tự và không chứa khoảng trắng", "Bad Request"));
            }
            if (!passwordEncoder.matches(oldPassword, user.getPassword())){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Mật khẩu cũ không chính xác", "Bad Request"));
            }
            if (oldPassword.equals(newPassword)){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Mật khẩu mới phải khác mật khẩu cũ", "Bad Request"));
            }
            user.setPassword(newPassword);
            User u = userService.changePassword(user);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", u));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<?>> resetPassword(@RequestParam String email){
        try {
            if (email.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            if (!userService.existsByEmail(email)){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không tồn tại email này", "Bad Request"));
            }
            String otp = emailService.sendOtpEmail(email);
            Otp o = new Otp(email, otp, LocalDateTime.now().plusMinutes(5));
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", o));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @PostMapping("/new_password")
    public ResponseEntity<ApiResponse<?>> newPassword(@RequestParam String email, @RequestParam String password){
        try {
            if (password.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            String passwordRegex = "^(?!.*\\s)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";
            if (!password.matches(passwordRegex)) {
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Mật khẩu phải có ít nhất một chữ hoa, một chữ thường, một chữ số, một ký tự đặc biệt và tối thiểu 8 ký tự và không chứa khoảng trắng", "Bad Request"));
            }
            User user = userService.getUserByEmail(email);
            user.setPassword(password);
            User u = userService.changePassword(user);
            return ResponseEntity.status(200).body(ApiResponse.success(200, "", u));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    public int random(){
        Random random = new Random();
        return random.nextInt(6)+1;
    }
}
