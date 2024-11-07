package com.hnue.english.controller;

import com.hnue.english.dto.UserDTO;
import com.hnue.english.model.User;
import com.hnue.english.response.ApiResponse;
import com.hnue.english.response.ImportFromJson;
import com.hnue.english.response.LoginResponse;
import com.hnue.english.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
//    private final PagedResourcesAssembler<User> pagedResourcesAssembler;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(false);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createUser(@RequestParam String email, @RequestParam String password,
                                                     @RequestParam String fullName, @RequestParam String subscriptionPlan){
        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() || subscriptionPlan.isEmpty()){
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
//        PagedModel<EntityModel<User>> pagedModel = pagedResourcesAssembler.toModel(users);
        return ResponseEntity.status(200).body(ApiResponse.success(200, "", users));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateUser(@PathVariable int id, @RequestParam(required = false) String password,
                                                     @RequestParam String fullName, @RequestParam String subscriptionPlan,
                                                     @RequestParam String role, @RequestParam(required = false) int paid){
        try {
            if (fullName.isEmpty() || role.isEmpty() || subscriptionPlan.isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            UserDTO userDTO = UserDTO.builder()
                    .password(password)
                    .fullName(fullName).subscriptionPlan(subscriptionPlan)
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
        List<UserDTO> uniqueUsers = removeDuplicateEmails(list);
        List<String> existingEmails = userService.checkExistingEmails(uniqueUsers);
        ImportFromJson u = new ImportFromJson();
        if (!existingEmails.isEmpty()) {
            u.setCountError(existingEmails.size());
            u.setCountSuccess(uniqueUsers.size() - existingEmails.size());
            u.setError(existingEmails);
            return ResponseEntity.status(400).body(ApiResponse.success(400, "Danh sách có lỗi", u));
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
}
