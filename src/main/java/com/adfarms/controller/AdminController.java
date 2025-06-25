package com.adfarms.controller;

import com.adfarms.dto.EmployeeDto;
import com.adfarms.entity.BranchEntity;
import com.adfarms.entity.EmployeeEntity;
import com.adfarms.enums.Role;
import com.adfarms.service.BranchService;
import com.adfarms.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
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
        model.addAttribute("managers", employeeService.findEmployeeByRole(Role.MANAGER));
        return "admin/admin-dashboard";
    }
    @GetMapping("/managers/new")
    public String showManagerForm(Model model) {
        model.addAttribute("employeeDto", new EmployeeDto());
        if(branchService.findAll().isEmpty()){
            return "redirect:/admin/branches/new";
        }
        model.addAttribute("branches", branchService.findAll());
        return "admin/manager-form";

    }

    @PostMapping("/managers")
    public String createManager(EmployeeDto employeeDto) {
        BranchEntity branch = branchService.findById(employeeDto.getBranchId());
        EmployeeEntity empEntity = new EmployeeEntity();
        empEntity.setFirstName(employeeDto.getFirstName());
        empEntity.setLastName(employeeDto.getLastName());
        empEntity.setEmail(employeeDto.getEmail());
        empEntity.setPassword(passwordEncoder.encode(employeeDto.getPassword()));
        empEntity.setRole(Role.MANAGER);
        empEntity.setBranch(branch);

        employeeService.save(empEntity);

        return "redirect:/admin/branches";
    }


    @GetMapping("/managers/{id}/edit")
    public String editManager(@PathVariable("id") Long id, Model model) {
        EmployeeEntity manager = employeeService.findById(id);

        List<BranchEntity> branches = branchService.findAll();
        model.addAttribute("manager", manager);
        model.addAttribute("branches", branches);
        return "admin/edit-manager";
    }

    @PostMapping("/managers/{id}/edit")
    public String updateManager(@PathVariable("id") Long id, @ModelAttribute EmployeeEntity employeeEntity) {
        EmployeeEntity manager = employeeService.findById(id);
        manager.setFirstName(employeeEntity.getFirstName());
        manager.setLastName(employeeEntity.getLastName());
        manager.setEmail(employeeEntity.getEmail());
        manager.setPassword(employeeEntity.getPassword());
        manager.setRole(Role.MANAGER);
        manager.setBranch(employeeEntity.getBranch());
        employeeService.save(manager);
        return "redirect:/admin/";
    }

    @GetMapping("/managers/{id}/delete")
    public String deleteManager(@PathVariable("id") Long id) {
        employeeService.deleteById(id);
        return "redirect:/admin/";
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

    @GetMapping("/branches/{id}/edit")
    public String showBranchEditForm(Model model, @PathVariable Long id) {
        model.addAttribute("branch", branchService.findById(id));
        return "admin/edit-branch";
    }

    @PostMapping("/branches/{id}/edit")
    public String updateBranch(BranchEntity branch, @PathVariable Long id) {
        branch.setId(id);
        branchService.save(branch);
        return "redirect:/admin/";
    }




}
