package com.project.application.service;

import com.project.application.dto.UploadResponse;
import com.project.application.dto.UserDTO;
import com.project.application.models.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserService {
    public byte[] generateUsersJson(int count);
    UploadResponse importUsers(MultipartFile file) throws IOException;

    List<UserDTO> getAllUsers();
    User findByUsername(String username);
    User findByEmail(String email);
    User findByUsernameOrEmail(String usernameOrEmail);
    User getCurrentUser(String username);
    User findById(Long id);

}

