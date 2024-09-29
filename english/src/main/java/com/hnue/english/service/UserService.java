package com.hnue.english.service;

import com.hnue.english.component.JwtTokenUtil;
import com.hnue.english.dto.UserDTO;
import com.hnue.english.exception.DataNotFoundException;
import com.hnue.english.model.User;
import com.hnue.english.reponsitory.UserReponsitory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserReponsitory userReponsitory;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    public void createUser(UserDTO userDTO) throws DataNotFoundException{
        String email = userDTO.getEmail();
        if (userReponsitory.existsByEmail(email)){
            throw  new DataIntegrityViolationException("Đã tồn tại email");
        }
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setRole("ROLE_user");

        String pass = passwordEncoder.encode(user.getPassword());
        user.setPassword(pass);

        userReponsitory.save(user);
    }

    public String login(String email, String password) throws Exception{
        Optional<User> user = userReponsitory.findByEmail(email);
        if (user.isEmpty()){
            throw new DataNotFoundException("Tài khoản hoặc mật khâu sai");
        }
        //return user.get();

        User existingUser = user.get();
        if (!passwordEncoder.matches(password, existingUser.getPassword())){
            throw new BadCredentialsException("Tài khoản hoặc mật khâu không đúng");
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                email, password, existingUser.getAuthorities()
        );
        authenticationManager.authenticate(authenticationToken);

        return jwtTokenUtil.generateToken(existingUser);
    }

    public String refresh(String token) throws Exception{
        String email = jwtTokenUtil.extractEmail(token);
        Optional<User> user = userReponsitory.findByEmail(email);
        if (user.isEmpty()){
            throw new RuntimeException("Không thể tạo lại token");
        }else {
            User u = new User();
            u.setEmail(email);
            return jwtTokenUtil.generateToken(u);
        }
    }
}
