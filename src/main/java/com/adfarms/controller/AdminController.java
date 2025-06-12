package com.adfarms.controller;

import com.adfarms.dto.EmployeeForm;
import com.adfarms.entity.BranchEntity;
import com.adfarms.entity.EmployeeEntity;
import com.adfarms.enums.Role;
import com.adfarms.service.BranchService;
import com.adfarms.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private BranchService branchService;
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/")
    public String showAdminDashboard(Model model) {
        if(branchService.findAll().isEmpty()){
            return "redirect:/admin/branches";
        }

        model.addAttribute("branches", branchService.findAll());
        model.addAttribute("managers", employeeService.findAll());
        return "admin/admin-dashboard";
    }
    @GetMapping("/managers/new")
    public String showManagerForm(Model model) {
        model.addAttribute("employeeForm", new EmployeeForm());
        if(branchService.findAll().isEmpty()){
            return "redirect:/admin/branches/new";
        }
        model.addAttribute("branches", branchService.findAll());
        return "admin/manager-form";

    }

    @PostMapping("/managers")
    public String createManger(EmployeeForm employeeForm) {
        BranchEntity branch = branchService.findById(employeeForm.getBranchId());
        EmployeeEntity empEntity = new EmployeeEntity();
        empEntity.setFirstName(employeeForm.getFirstName());
        empEntity.setLastName(employeeForm.getLastName());
        empEntity.setEmail(employeeForm.getEmail());
        empEntity.setPassword(passwordEncoder.encode(employeeForm.getPassword()));
        empEntity.setRole(Role.MANAGER);
        empEntity.setBranch(branch);

        employeeService.save(empEntity);

        return "redirect:/admin/branches";
    }


    @GetMapping("/branches")
    public String listBranches(Model model) {
        model.addAttribute("branches", branchService.findAll());
        return "admin/branch-list";
    }


    @GetMapping("/branches/new")
    public String showBranchForm(Model model) {
        model.addAttribute("branch", new BranchEntity());
        return "admin/branch-form";
    }
    @PostMapping("/branches")
    public String createBranch(BranchEntity branch) {
        branchService.save(branch);
        return "redirect:/admin/branches";
    }



}
