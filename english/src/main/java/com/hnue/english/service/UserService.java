package com.hnue.english.service;

import com.hnue.english.component.JwtTokenUtil;
import com.hnue.english.dto.UserDTO;
import com.hnue.english.exception.DataNotFoundException;
import com.hnue.english.model.User;
import com.hnue.english.reponsitory.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.util.List;
import java.util.Optional;

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
        user.setRole("ROLE_"+userDTO.getRole());
        String pass = passwordEncoder.encode(user.getPassword());
        user.setPassword(pass);
        return userRepository.save(user);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User getUser(int id){
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy user với email: " + email));
    }

    public User updateUser(int id, UserDTO userDTO){
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));

        user.setPassword(userDTO.getPassword());
        user.setFullName(userDTO.getFullName());
        user.setSubscriptionPlan(userDTO.getSubscriptionPlan());
        user.setSubscriptionStartDate(userDTO.getSubscriptionStartDate());
        user.setSubscriptionEndDate(userDTO.getSubscriptionEndDate());
        user.setRole("ROLE_"+userDTO.getRole());
        user.setPaid(userDTO.getPaid());

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

    public void register(UserDTO userDTO) throws DataNotFoundException{
        String email = userDTO.getEmail();
        if (userRepository.existsByEmail(email)){
            throw new DataIntegrityViolationException("Đã tồn tại email");
        }
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setFullName(userDTO.getFullName());
        user.setRole("ROLE_user");

        String pass = passwordEncoder.encode(user.getPassword());
        user.setPassword(pass);

        userRepository.save(user);
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
}
