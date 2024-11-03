package com.hnue.english.controller;

import com.hnue.english.dto.UserDTO;
import com.hnue.english.model.User;
import com.hnue.english.response.ApiResponse;
import com.hnue.english.response.LoginResponse;
import com.hnue.english.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(false);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createUser(@RequestParam String email, @RequestParam String password,
                                                     @RequestParam String fullName, @RequestParam(required = false) String subscriptionPlan,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date subscriptionStartDate,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date subscriptionEndDate,
                                                     @RequestParam String role){
        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() || role.isEmpty()){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(emailRegex)) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Email không hợp lệ", "Bad Request"));
        }
        if (userService.existsByEmail(email)){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Email này đã tồn tại", "Bad Request"));
        }
        UserDTO userDTO = UserDTO.builder()
                .email(email).password(password)
                .fullName(fullName).subscriptionPlan(subscriptionPlan)
                .subscriptionStartDate(subscriptionStartDate).subscriptionEndDate(subscriptionEndDate)
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
                                                   @RequestParam(defaultValue = "1") int size){
        if (size < 1){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "size phải lớn hơn 0", "Bad Request"));
        }
        Page<User> users = userService.getUsers(page, size);
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", users));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateUser(@PathVariable int id, @RequestParam(required = false) String password,
                                                     @RequestParam String fullName, @RequestParam(required = false) String subscriptionPlan,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date subscriptionStartDate,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date subscriptionEndDate,
                                                     @RequestParam String role, @RequestParam(required = false) int paid){
        try {
            if (fullName.isEmpty() || role.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            if (subscriptionPlan == null || subscriptionPlan.isBlank()){
                subscriptionPlan = "none";
            }
            UserDTO userDTO = UserDTO.builder()
                    .password(password)
                    .fullName(fullName).subscriptionPlan(subscriptionPlan)
                    .subscriptionStartDate(subscriptionStartDate).subscriptionEndDate(subscriptionEndDate)
                    .role(role).paid(paid)
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
            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(email);
            userDTO.setPassword(password);
            userDTO.setFullName(fullName);
            userService.register(userDTO);
            return ResponseEntity.status(201).body(ApiResponse.success(201, "", userDTO));
        }catch (Exception e){
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
        }
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<?>> createList(@RequestBody List<UserDTO> list){
        List<UserDTO> uniqueUsers = removeDuplicateEmails(list);
        List<String> existingEmails = userService.checkExistingEmails(uniqueUsers);
        if (!existingEmails.isEmpty()) {
            return ResponseEntity.status(400).body(ApiResponse.error(400, existingEmails, "Bad Request"));
        }
        return ResponseEntity.status(201).body(ApiResponse.success(201, "Tạo thành công danh sách người dùng", null));
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
}
