package com.hnue.english.service;

import com.hnue.english.component.JwtTokenUtil;
import com.hnue.english.dto.UserDTO;
import com.hnue.english.exception.DataNotFoundException;
import com.hnue.english.model.User;
import com.hnue.english.reponsitory.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    public User createUser(UserDTO userDTO){
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setFullName(userDTO.getFullName());
        user.setSubscriptionPlan(user.getSubscriptionPlan());
        user.setSubscriptionStartDate(userDTO.getSubscriptionStartDate());
        user.setSubscriptionEndDate(userDTO.getSubscriptionEndDate());
        user.setRole("ROLE_"+userDTO.getRole().toUpperCase());
        String pass = passwordEncoder.encode(user.getPassword());
        user.setPassword(pass);
        user.setCreatedAt(new Date());
        return userRepository.save(user);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User getUser(int id){
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));
    }

    public Page<User> getUsers(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy user với email: " + email));
    }

    public User updateUser(int id, UserDTO userDTO){
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));
        if (!(userDTO.getPassword() == null || userDTO.getPassword().isBlank())){
            user.setPassword(userDTO.getPassword());
        }
        user.setFullName(userDTO.getFullName());
        user.setSubscriptionPlan(userDTO.getSubscriptionPlan());
        user.setSubscriptionStartDate(userDTO.getSubscriptionStartDate());
        user.setSubscriptionEndDate(userDTO.getSubscriptionEndDate());
        user.setRole("ROLE_"+userDTO.getRole().toUpperCase());
        user.setPaid(userDTO.getPaid());
        user.setUpdatedAt(new Date());
        return userRepository.save(user);
    }

    public void deleteUser(int id){
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));
        userRepository.delete(user);
    }

    public boolean existsByEmail(String email){
        return userRepository.existsByEmail(email);
    }

    public String login(String email, String password) throws Exception{
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()){
            throw new DataNotFoundException("Thông tin đăng nhập không chính xác");
        }
        //return user.get();

        User existingUser = user.get();
        if (!passwordEncoder.matches(password, existingUser.getPassword())){
            throw new BadCredentialsException("Thông tin đăng nhập không chính xác");
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                email, password, existingUser.getAuthorities()
        );
        authenticationManager.authenticate(authenticationToken);

        return jwtTokenUtil.generateToken(existingUser);
    }

    public User fetch(String token){
        String email = jwtTokenUtil.extractEmail(token);
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy user với email: " + email));
    }

    public User register(UserDTO userDTO) throws DataNotFoundException{
        String email = userDTO.getEmail();
        if (userRepository.existsByEmail(email)){
            throw new DataIntegrityViolationException("Đã tồn tại email");
        }
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setFullName(userDTO.getFullName());
        user.setCreatedAt(new Date());
        user.setRole("ROLE_USER");

        String pass = passwordEncoder.encode(user.getPassword());
        user.setPassword(pass);

        return userRepository.save(user);
    }

    public void logout(String token){
        tokenBlacklistService.blacklistToken(token);
    }

    public String refresh(String token) throws Exception{
        String email = jwtTokenUtil.extractEmail(token);
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()){
            throw new RuntimeException("Không thể tạo lại token");
        }else {
            tokenBlacklistService.blacklistToken(token);
            User u = new User();
            u.setEmail(email);
            return jwtTokenUtil.generateToken(u);
        }
    }

    public List<String> checkExistingEmails(List<UserDTO> userDTOList) {
        return userDTOList.stream()
                .filter(userDTO -> existsByEmail(userDTO.getEmail()))
                .map(UserDTO::getEmail)
                .collect(Collectors.toList());
    }

    public void saveAll(List<UserDTO> userDTOList) {
        List<User> usersToSave = userDTOList.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        userRepository.saveAll(usersToSave);
    }

    private User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setFullName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        return user;
    }
}
