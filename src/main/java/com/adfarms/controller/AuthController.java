package com.adfarms.controller;


import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.logging.Logger;

@Controller
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());

    @GetMapping("/login")
    public String login() {
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
            case "Role_ADMIN":
                return "redirect:/admin/";
            case "Role_MANAGER":
                return "redirect:/managers/";
            case "Role_EMPLOYEE":
                return "redirect:/employee/";
            default:
                logger.warning("Unknown role: " + role);
                return "redirect:/login";
        }
    }
}
