package com.adfarms.controller;

import com.adfarms.dto.EmployeeForm;
import com.adfarms.dto.TaskForm;

import com.adfarms.entity.BranchEntity;
import com.adfarms.entity.EmployeeEntity;
import com.adfarms.entity.TaskEntity;
import com.adfarms.entity.TimesheetEntity;
import com.adfarms.enums.Role;
import com.adfarms.enums.TaskStatus;
import com.adfarms.enums.TimesheetStatus;
import com.adfarms.service.BranchService;
import com.adfarms.service.EmployeeService;
import com.adfarms.service.TaskService;
import com.adfarms.service.TimesheetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/managers")
public class ManagerController {

    private static final Logger logger = LoggerFactory.getLogger(ManagerController.class);

    @Autowired
    private TimesheetService timesheetService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private BranchService branchService;



    @GetMapping("/")
    public String showManagerDashboard(Model model, Authentication authentication) {
        EmployeeEntity manager = (EmployeeEntity) authentication.getPrincipal();
        model.addAttribute("tasks", taskService.findByAssigner(manager));
        model.addAttribute("timesheets", timesheetService.findByEmployeeBranch(manager.getBranch()));
        return "manager/manager-dashboard";
    }
    @GetMapping("/new")
    public String showEmployeeForm(Model model, Authentication authentication) {
        model.addAttribute("empForm", new EmployeeForm());

        // Check if branches exist
        if (branchService.findAll().isEmpty()) {
            logger.warn("No branches found, redirecting to /admin/branches");
            return "redirect:/admin/branches";
        }

        // Get authenticated user's email
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.debug("Fetching employee for email: {}", email);

        // Fetch EmployeeEntity from repository
        EmployeeEntity manager = employeeService.findByEmail(email);
        if (manager == null) {
            logger.error("Authenticated user not found in database: {}", email);
            throw new IllegalStateException("Authenticated user not found");
        }

        // Assign default branch if none exists
        if (manager.getBranch() == null) {
            BranchEntity defaultBranch = branchService.findAll().get(0);
            logger.debug("Assigning default branch ID {} to manager: {}", defaultBranch.getId(), email);
            manager.setBranch(defaultBranch);
            employeeService.save(manager);
        }

        // Set branchId in model
//        Long branchId = manager.getBranch().getId();
        Long branchId = 1L;
        if (branchId == null) {
            logger.error("Branch ID is null for manager: {}", email);
            throw new IllegalStateException("Branch ID is null");
        }
        logger.debug("Setting branchId: {} in model", branchId);
        model.addAttribute("branchId", branchId);

        return "manager/employee-form";
    }

    @PostMapping("/employees")
    public String createEmployee(EmployeeForm employeeForm, Authentication authentication) {
        EmployeeEntity manager = (EmployeeEntity) authentication.getPrincipal();
        EmployeeEntity empEntity = new EmployeeEntity();
        empEntity.setFirstName(employeeForm.getFirstName());
        empEntity.setLastName(employeeForm.getLastName());
        empEntity.setEmail(employeeForm.getEmail());
        empEntity.setPassword(passwordEncoder.encode(employeeForm.getPassword()));
        empEntity.setRole(Role.EMPLOYEE);
        empEntity.setBranch(branchService.findById(employeeForm.getBranchId()));


        employeeService.save(empEntity);
        return "redirect:/managers/timesheets/pending";
    }




    @GetMapping("/timesheets/pending")
    public String listPendingTimesheets(Model model, Authentication authentication) {
        EmployeeEntity employee = (EmployeeEntity) authentication.getPrincipal();

        model.addAttribute("timesheets", timesheetService.findByEmployeeBranchAndStatus(employee.getBranch(), TimesheetStatus.PENDING));
        return "manager/pending-timesheets";
    }

    @PostMapping("/timesheets/{id}/approve")
    public String approveTimesheet(@PathVariable Long id, Authentication authentication) {
        EmployeeEntity employee = (EmployeeEntity) authentication.getPrincipal();
        TimesheetEntity timesheet = timesheetService.findById(id);

        if(timesheet != null && timesheet.getEmployee().getBranch().equals(employee.getBranch())) {
            timesheet.setStatus(TimesheetStatus.APPROVED);
            timesheet.setApprovedBy(employee);
            timesheetService.saveTimesheet(timesheet);
        }

        return "redirect:/manager/timesheet/pending";
    }

    @GetMapping("/tasks/new")
    public String showTaskForm(Model model, Authentication authentication) {
        EmployeeEntity manager = (EmployeeEntity) authentication.getPrincipal();

        model.addAttribute("task", new TaskForm());

        model.addAttribute("employees", employeeService.findByBranchAndRole(manager.getBranch(), Role.EMPLOYEE));

        return "manager/task-form";
    }


    @PostMapping("/tasks")
    public String assignTask(TaskForm task, Authentication authentication) {
        EmployeeEntity manager = (EmployeeEntity) authentication.getPrincipal();
        TaskEntity newTask = new TaskEntity();
        newTask.setAssigner(manager);
        newTask.setAssignee( employeeService.findById(task.getAssigneeId()));
        newTask.setDeadline(task.getDeadline());
        newTask.setDescription(task.getDescription());
        newTask.setStatus(TaskStatus.NOT_STARTED);

        taskService.save(newTask);
        return "redirect:/manager/timesheets/pending";
    }

}
