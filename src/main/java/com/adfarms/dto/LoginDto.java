package com.adfarms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class LoginDto {

    @NotBlank
    @Email(message = "Email should be valid")
    private String email;
    @NotBlank
    @Min(value = 6, message = "Password must be at least 6 characters long")
    private String password;
}
