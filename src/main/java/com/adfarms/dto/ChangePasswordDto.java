package com.adfarms.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDto {

    @NotBlank(message = "Old password cannot be empty.")
    private String oldPassword;
    @NotBlank(message = "New password cannot be empty.")
    private String newPassword;
    @NotBlank(message = "Confirm password cannot be empty.")
    private String confirmPassword;
}
