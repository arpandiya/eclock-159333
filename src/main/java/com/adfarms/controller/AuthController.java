package com.adfarms.controller;


import com.adfarms.dto.ChangePasswordDto;
import com.adfarms.entity.EmployeeEntity;
import com.adfarms.service.AuthService;
import com.adfarms.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.logging.Logger;

@Controller
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String login(
            @RequestParam(value="error", required = false) String error,
            @RequestParam(value="logout", required = false) String logout,
            @RequestParam(value="register", required = false) String register,
            Model model
    ) {
        String errMessage = null;
        if(error != null) {
            errMessage = "Invalid username or password";
        } else if(logout != null) {
            errMessage = "You have been logged out successfully";
        } else if(register != null) {
            errMessage = "You have been registered successfully";
        }

        model.addAttribute("errMessage", errMessage);
        return "login";
    }


    @GetMapping("/")
    public String dashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warning("Authentication is null or user is not authenticated");
            return "redirect:/login";
        }

        if (authentication.getAuthorities().isEmpty()) {
            logger.warning("No authorities found for authenticated user: " + authentication.getName());
            return "redirect:/login";
        }

        String role = authentication.getAuthorities().iterator().next().getAuthority();
        logger.info("User: " + authentication.getName() + ", Role: " + role);

        switch (role) {
            case "ROLE_ADMIN":
                return "redirect:/admin/";
            case "ROLE_MANAGER":
                return "redirect:/managers/";
            case "ROLE_EMPLOYEE":
                return "redirect:/employee/";
            default:
                logger.warning("Unknown role: " + role);
                return "redirect:/login";
        }
    }

    @GetMapping("/change-password")
    public String changePassword(Model model, Authentication authentication) {
       if(authentication == null || authentication.getPrincipal() == null || !(authentication.getPrincipal() instanceof EmployeeEntity) ) {
       return "redirect:/login";
       }

        model.addAttribute("changePasswordForm", new ChangePasswordDto());

        return "change-password";

    }

    @PostMapping("/change-password")
    public String changePasswordSubmit(@Valid @ModelAttribute("changePasswordDto") ChangePasswordDto changePasswordDto,
                                       BindingResult bindingResult,
                                       Model model,
                                       Authentication authentication) {



        if(bindingResult.hasErrors()) {
            return "change-password";
        }

        authService.changePassword(changePasswordDto);

        return "redirect:/login";

    }
}
