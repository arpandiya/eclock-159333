package com.adfarms.service;

import com.adfarms.dto.ChangePasswordDto;
import com.adfarms.entity.EmployeeEntity;
import com.adfarms.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {


    private final EmployeeRepository employeeRepository;

    @Autowired
    private  PasswordEncoder passwordEncoder;

    public AuthService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;

    }

    public String changePassword(ChangePasswordDto dto) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        EmployeeEntity employee = employeeRepository.findByEmail(email);



        if (!passwordEncoder.matches(dto.getOldPassword(), employee.getPassword())) {
            return "Current password is incorrect";
        }


        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return "New password and confirmation do not match";
        }


        employee.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        employeeRepository.save(employee);
        return "Password changed successfully";
    }
}
