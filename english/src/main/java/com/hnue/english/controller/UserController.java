package com.hnue.english.controller;

import com.hnue.english.component.JwtTokenUtil;
import com.hnue.english.dto.UserDTO;
import com.hnue.english.model.User;
import com.hnue.english.response.ApiResponse;
import com.hnue.english.response.LoginResponse;
import com.hnue.english.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createUser(@RequestParam String email, @RequestParam String password,
                                                     @RequestParam String fullName, @RequestParam(required = false) String subscriptionPlan,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date subscriptionStartDate,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date subscriptionEndDate,
                                                     @RequestParam String role){
        if (email.trim().isEmpty() || password.trim().isEmpty() || fullName.trim().isEmpty() || role.trim().isEmpty()){
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
                .email(email.trim()).password(password.trim())
                .fullName(fullName.trim()).subscriptionPlan(subscriptionPlan)
                .subscriptionStartDate(subscriptionStartDate).subscriptionEndDate(subscriptionEndDate)
                .role(role)
                .build();
        userService.createUser(userDTO);
        return ResponseEntity.status(201).body(ApiResponse.success(201, "", userDTO));
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

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateUser(@PathVariable int id, @RequestParam String password,
                                                     @RequestParam String fullName, @RequestParam(required = false) String subscriptionPlan,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date subscriptionStartDate,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date subscriptionEndDate,
                                                     @RequestParam String role){
        try {
            if (password.trim().isEmpty() || fullName.trim().isEmpty() || role.trim().isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            UserDTO userDTO = UserDTO.builder()
                    .password(password.trim())
                    .fullName(fullName.trim()).subscriptionPlan(subscriptionPlan)
                    .subscriptionStartDate(subscriptionStartDate).subscriptionEndDate(subscriptionEndDate)
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

    @PostMapping ("/register")
    public ResponseEntity<?> register(@RequestParam String email,
                                      @RequestParam String password,
                                      @RequestParam String fullName){
        try {
            if (email.trim().isEmpty() || password.trim().isEmpty() || fullName.trim().isEmpty()){
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Không để trống dữ liệu", "Bad Request"));
            }
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            if (!email.matches(emailRegex)) {
                return ResponseEntity.status(400).body(ApiResponse.error(400, "Email không hợp lệ", "Bad Request"));
            }
            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(email.trim());
            userDTO.setPassword(password.trim());
            userDTO.setFullName(fullName.trim());
            userService.register(userDTO);
            return ResponseEntity.status(201).body(ApiResponse.success(201, "", userDTO));
        }catch (Exception e){
            return ResponseEntity.status(400).body(ApiResponse.error(400, e.getMessage(), "Bad Request"));
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

    @GetMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Token không hợp lệ", "Bad Request"));
        }
        String token = authHeader.substring(7);
        userService.logout(token);
        return ResponseEntity.status(200).body(ApiResponse.success(200, "Đăng xuất thành công", null));
    }
}
