package com.project.application.dto;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String city;
    private String country;
    private  String company;
    private String jobPosition;
    private String mobile;
    private String username;
    private String avatar;
    private String email;
    private String password;
    private String role;
}
