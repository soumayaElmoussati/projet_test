package com.project.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;
@Data
@Builder
@AllArgsConstructor
public class LoginRequest {
    @NotNull()
    private String username;
    @NotNull()
    private String password;
}
