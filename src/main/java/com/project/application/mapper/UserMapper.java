package com.project.application.mapper;

import com.project.application.dto.UserDTO;
import com.project.application.models.User;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Component
public class UserMapper {
    private final ModelMapper modelMapper = new ModelMapper();

    public UserDTO toUserDTO(User user){
        return modelMapper.map(user, UserDTO.class);
    }

    public User toUser(UserDTO userDTO){
        return modelMapper.map(userDTO, User.class);
    }

    public List
            <User> toUsers(List<UserDTO> userDTOs) {
        return userDTOs.stream()
                .map(this::toUser)
                .collect(Collectors.toList());
    }
}
