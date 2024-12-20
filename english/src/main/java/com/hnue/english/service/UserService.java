package com.hnue.english.service;

import com.hnue.english.component.JwtTokenUtil;
import com.hnue.english.dto.UserDTO;
import com.hnue.english.exception.DataNotFoundException;
import com.hnue.english.model.User;
import com.hnue.english.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
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
        user.setSubscriptionPlan(userDTO.getSubscriptionPlan());
        if (!userDTO.getSubscriptionPlan().equals("none")){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            if (userDTO.getSubscriptionPlan().equals("6_months")){
                calendar.add(Calendar.MONTH, 6);
                user.setSubscriptionStartDate(new Date());
                user.setSubscriptionEndDate(calendar.getTime());
            } else if (userDTO.getSubscriptionPlan().equals("1_year")) {
                calendar.add(Calendar.YEAR, 1);
                user.setSubscriptionStartDate(new Date());
                user.setSubscriptionEndDate(calendar.getTime());
            }else {
                calendar.add(Calendar.YEAR, 3);
                user.setSubscriptionStartDate(new Date());
                user.setSubscriptionEndDate(calendar.getTime());
            }
        }
        user.setRole(userDTO.getRole());
        String pass = passwordEncoder.encode(user.getPassword());
        user.setPassword(pass);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        return userRepository.save(user);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User getUser(int id){
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));
    }

    public Page<User> getUsers(int page, int size, String email, String fullName, String role, String subscriptionPlan, String sort){
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            var predicates= criteriaBuilder.conjunction();

            if (email != null && !email.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("email"), "%" + email + "%"));
            }

            if (fullName != null && !fullName.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("fullName"), "%" + fullName + "%"));
            }

            if (role != null && !role.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("role"), "%" + role + "%"));
            }

            if (subscriptionPlan != null && !subscriptionPlan.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("subscriptionPlan"), "%" + subscriptionPlan + "%"));
            }

            if (sort != null && !sort.trim().isEmpty()) {
                switch (sort) {
                    case "email":
                        query.orderBy(criteriaBuilder.asc(root.get("email")));
                        break;
                    case "-email":
                        query.orderBy(criteriaBuilder.desc(root.get("email")));
                        break;
                    case "fullName":
                        query.orderBy(criteriaBuilder.asc(root.get("fullName")));
                        break;
                    case "-fullName":
                        query.orderBy(criteriaBuilder.desc(root.get("fullName")));
                        break;
                    case "updatedAt":
                        query.orderBy(criteriaBuilder.asc(root.get("updatedAt")));
                        break;
                    case "-updatedAt":
                        query.orderBy(criteriaBuilder.desc(root.get("updatedAt")));
                        break;
                    case "subscriptionStartDate":
                        predicates = criteriaBuilder.and(predicates, criteriaBuilder.isNotNull(root.get("subscriptionStartDate")));
                        query.orderBy(criteriaBuilder.asc(root.get("subscriptionStartDate")));
                        break;
                    case "-subscriptionStartDate":
                        predicates = criteriaBuilder.and(predicates, criteriaBuilder.isNotNull(root.get("subscriptionStartDate")));
                        query.orderBy(criteriaBuilder.desc(root.get("subscriptionStartDate")));
                        break;
                    case "subscriptionEndDate":
                        predicates = criteriaBuilder.and(predicates, criteriaBuilder.isNotNull(root.get("subscriptionEndDate")));
                        query.orderBy(criteriaBuilder.asc(root.get("subscriptionEndDate")));
                        break;
                    case "-subscriptionEndDate":
                        predicates = criteriaBuilder.and(predicates, criteriaBuilder.isNotNull(root.get("subscriptionEndDate")));
                        query.orderBy(criteriaBuilder.desc(root.get("subscriptionEndDate")));
                        break;
                    default:
                        break;
                }
            }

            return predicates;
        };
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(spec,pageable);
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy user với email: " + email));
    }

    public User updateUser(int id, UserDTO userDTO){
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));
        user.setFullName(userDTO.getFullName());
        if (!user.getSubscriptionPlan().equals(userDTO.getSubscriptionPlan())){
            user.setSubscriptionPlan(userDTO.getSubscriptionPlan());
            if (user.getSubscriptionEndDate() == null){
                if (!userDTO.getSubscriptionPlan().equals("none")){
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    if (userDTO.getSubscriptionPlan().equals("6_months")){
                        calendar.add(Calendar.MONTH, 6);
                        user.setSubscriptionStartDate(new Date());
                        user.setSubscriptionEndDate(calendar.getTime());
                    } else if (userDTO.getSubscriptionPlan().equals("1_year")) {
                        calendar.add(Calendar.YEAR, 1);
                        user.setSubscriptionStartDate(new Date());
                        user.setSubscriptionEndDate(calendar.getTime());
                    }else {
                        calendar.add(Calendar.YEAR, 3);
                        user.setSubscriptionStartDate(new Date());
                        user.setSubscriptionEndDate(calendar.getTime());
                    }
                }
            }else{
                if (user.getSubscriptionEndDate().after(new Date())){
                    throw new RuntimeException("User vẫn còn thời hạn của gói đăng kí");
                }else{
                    if (!userDTO.getSubscriptionPlan().equals("none")){
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date());
                        if (userDTO.getSubscriptionPlan().equals("6_months")){
                            calendar.add(Calendar.MONTH, 6);
                            user.setSubscriptionStartDate(new Date());
                            user.setSubscriptionEndDate(calendar.getTime());
                        } else if (userDTO.getSubscriptionPlan().equals("1_year")) {
                            calendar.add(Calendar.YEAR, 1);
                            user.setSubscriptionStartDate(new Date());
                            user.setSubscriptionEndDate(calendar.getTime());
                        }else {
                            calendar.add(Calendar.YEAR, 3);
                            user.setSubscriptionStartDate(new Date());
                            user.setSubscriptionEndDate(calendar.getTime());
                        }
                    }
                }
            }
        }
//        if (user.getRole().equals("ROLE_ADMIN") && userDTO.getRole().equals("ROLE_USER")){
//            throw new RuntimeException("Bạn không thể cập nhật quyền xuống người dùng");
//        }
        user.setRole(userDTO.getRole());
        user.setUpdatedAt(new Date());
        return userRepository.save(user);
    }

    public User payment(User user){
        Calendar calendar = Calendar.getInstance();
        user.setSubscriptionStartDate(new Date());
        if (user.getSubscriptionEndDate() == null){
            calendar.setTime(new Date());
        }else {
            if (user.getSubscriptionEndDate().after(new Date())){
                calendar.setTime(user.getSubscriptionEndDate());
            }else {
                calendar.setTime(new Date());
            }
        }
        if (user.getSubscriptionPlan().equals("6_months")){
            calendar.add(Calendar.MONTH, 6);
        } else if (user.getSubscriptionPlan().equals("1_year")) {
            calendar.add(Calendar.YEAR, 1);
        }else {
            calendar.add(Calendar.YEAR, 3);
        }
        user.setSubscriptionEndDate(calendar.getTime());
        user.setPaid(1);
        user.setUpdatedAt(new Date());
        return userRepository.save(user);
    }

    public void deleteSubscription(int id){
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));
        user.setSubscriptionPlan("none");
        user.setSubscriptionEndDate(new Date());
        userRepository.save(user);
    }

    public void deleteUser(int id){
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));
        if (user.getRole().equals("ROLE_ADMIN")){
            throw new RuntimeException("Không thể xóa quản trị viên");
        }
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
        user.setSubscriptionPlan("none");
        user.setCreatedAt(new Date());
        user.setRole("ROLE_USER");
        user.setUpdatedAt(new Date());

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
        String pass = passwordEncoder.encode(userDTO.getPassword());
        user.setPassword(pass);
        user.setSubscriptionPlan("none");
        user.setRole("ROLE_USER");
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        return user;
    }

    public List<User> getUsersCreatedBetween(Date start, Date end){
        return userRepository.findUsersCreatedBetween(start, end);
    }

    public Map<String, Long> getUserSegments() {
        List<Object[]> results = userRepository.findSubscriptionPlanCounts();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1],
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    public Map<Integer, Long> countUsersByMonthCurrentYear() {
        List<Object[]> results = userRepository.countUsersByMonthCurrentYear();
        Map<Integer, Long> monthUserCount = results.stream()
                .collect(Collectors.toMap(
                        result -> (Integer) result[0],
                        result -> (Long) result[1]
                ));

        for (int month = 1; month <= 12; month++) {
            monthUserCount.putIfAbsent(month, 0L);
        }

        return monthUserCount;
    }

    public List<User> getUsersWithExpiringSubscriptions(){
        return userRepository.findUsersWithExpiringSubscriptions();
    }

    public User changePassword(User user){
        String pass = passwordEncoder.encode(user.getPassword());
        user.setPassword(pass);
        return userRepository.save(user);
    }

    @Scheduled(fixedDelay = 5000)
    public void getAllUser(){
        List<User> users = userRepository.getAllUser();
        for (User u : users){
            if (u.getSubscriptionEndDate().before(new Date())){
                u.setSubscriptionPlan("none");
                userRepository.save(u);
            }
        }
    }
}
