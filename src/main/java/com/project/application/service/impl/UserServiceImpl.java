package com.project.application.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.application.dto.UploadResponse;
import com.project.application.dto.UserDTO;
import com.project.application.mapper.UserMapper;
import com.project.application.models.User;
import com.project.application.repository.UserRepository;
import com.project.application.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.github.javafaker.Faker;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    @Override
    public byte[] generateUsersJson(int count) {
        JSONArray usersArray = new JSONArray();
        Faker faker = new Faker();

        for (int i = 0; i < count; i++) {
            JSONObject userObject = new JSONObject();
            userObject.put("firstname", faker.name().firstName());
            userObject.put("lastname", faker.name().lastName());
            userObject.put("city", faker.address().city());
            userObject.put("country", faker.address().countryCode());
            userObject.put("avatar", faker.internet().avatar());
            userObject.put("company", faker.company().name());
            userObject.put("jobPosition", faker.job().position());
            userObject.put("mobile", faker.phoneNumber().cellPhone());
            userObject.put("username", faker.name().username());
            userObject.put("email", faker.internet().emailAddress());
            userObject.put("password", faker.internet().password(6, 10));
            userObject.put("role", faker.bool().bool() ? "admin" : "user");

            usersArray.put(userObject);
        }

        return usersArray.toString().getBytes();
    }

    @Override
    public UploadResponse importUsers(MultipartFile file) throws IOException {
        int totalRecords = 0;
        int importedRecords = 0;
        int failedRecords = 0;

        List<UserDTO> users = new ObjectMapper().readValue(file.getInputStream(), new TypeReference<List<UserDTO>>() {});

        for (UserDTO user : users) {
            totalRecords++;
            if (userRepository.existsByEmail(user.getEmail()) || userRepository.existsByUsername(user.getUsername())) {
                failedRecords++;
            } else {
                // Encodage du mot de passe avant de l'enregistrer en base de données
                String encodedPassword = encodePassword(user.getPassword());
                user.setPassword(encodedPassword);

                userRepository.save(userMapper.toUser(user));
                importedRecords++;
            }
        }

        return new UploadResponse(totalRecords, importedRecords, failedRecords);
    }

    private String encodePassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(userMapper::toUserDTO).collect(Collectors.toList());
    }

    @Override
    public User findByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional.orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User findByUsernameOrEmail(String usernameOrEmail) {
        User user = userRepository.findByUsername(usernameOrEmail).orElse(null);
        if (user == null) {
            user = userRepository.findByEmail(usernameOrEmail).orElse(null);
        }
        if (user == null) {
            throw new UsernameNotFoundException("User not found"); // Handle missing user
        }
        return user;
    }

    @Override
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    public User findById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        return optionalUser.orElse(null);
    }


}
