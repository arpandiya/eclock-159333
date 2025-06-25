package com.adfarms.controller;

import com.adfarms.dto.EmployeeDto;
import com.adfarms.dto.TaskDto;

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
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/managers")
@PreAuthorize("hasRole('ROLE_MANAGER') and hasRole('ROLE_EMPLOYEE')")
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
        if(authentication == null || authentication.getPrincipal() == null || !(authentication.getPrincipal() instanceof EmployeeEntity) ) {
            return "redirect:/login";
        }

        EmployeeEntity manager = (EmployeeEntity) authentication.getPrincipal();
        model.addAttribute("tasks", taskService.findByAssigner(manager));
        model.addAttribute("timesheets", timesheetService.findByEmployeeBranch(manager.getBranch()));
        model.addAttribute("employees", employeeService.findByBranchAndRole(manager.getBranch(), Role.EMPLOYEE));
        return "manager/manager-dashboard";
    }
    @GetMapping("/new")
    public String showEmployeeForm(Model model, Authentication authentication) {


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
        model.addAttribute("empForm", new EmployeeDto());
        return "manager/employee-form";
    }

    @PostMapping("/employees")
    public String createEmployee(EmployeeDto employeeDto, Authentication authentication) {
        EmployeeEntity manager = (EmployeeEntity) authentication.getPrincipal();
        EmployeeEntity empEntity = new EmployeeEntity();
        empEntity.setFirstName(employeeDto.getFirstName());
        empEntity.setLastName(employeeDto.getLastName());
        empEntity.setEmail(employeeDto.getEmail());
        empEntity.setPassword(passwordEncoder.encode(employeeDto.getPassword()));
        empEntity.setRole(Role.EMPLOYEE);
        empEntity.setBranch(manager.getBranch());
        employeeService.save(empEntity);
        return "redirect:/managers/";
    }

    @GetMapping("/employee/{id}/edit")
    public String editManager(@PathVariable("id") Long id, Model model) {
        EmployeeEntity employee = employeeService.findById(id);

        List<BranchEntity> branches = branchService.findAll();
        model.addAttribute("employee", employee);
        model.addAttribute("branches", branches);
        return "manager/edit-employee";
    }

    @PostMapping("/employee/{id}/edit")
    public String updateManager(@PathVariable("id") Long id, @ModelAttribute EmployeeEntity employeeEntity) {
        EmployeeEntity employee = employeeService.findById(id);
        employee.setFirstName(employeeEntity.getFirstName());
        employee.setLastName(employeeEntity.getLastName());
        employee.setEmail(employeeEntity.getEmail());
        employee.setPassword(employeeEntity.getPassword());
        employee.setRole(Role.EMPLOYEE);
        employee.setBranch(employeeEntity.getBranch());
        employeeService.save(employee);
        return "redirect:/managers/";
    }

    @GetMapping("/employee/{id}/delete")
    public String deleteManager(@PathVariable("id") Long id) {
        employeeService.deleteById(id);
        return "redirect:/managers/";
    }





    @GetMapping("/timesheet/{id}/edit")
    public String showEditTimesheetForm(@PathVariable("id") Long id, Model model) {
        TimesheetEntity timesheet = timesheetService.findById(id);
        model.addAttribute("timesheet", timesheet);
        return "manager/edit-timesheet";
    }

    @PostMapping("/timesheet/{id}/edit")
    public String updateTimesheet(@PathVariable("id") Long id, @ModelAttribute("timesheet") TimesheetEntity timesheetEntity, Model model) {
        // Calculate initial hours worked
        timesheetEntity.setHoursWorked(Duration.between(timesheetEntity.getClockIn(), timesheetEntity.getClockOut()).toHours());

// Calculate initial hours worked in minutes, then convert to double hours
        long minutesWorked = Duration.between(timesheetEntity.getClockIn(), timesheetEntity.getClockOut()).toMinutes();
        timesheetEntity.setHoursWorked(minutesWorked / 60.0); // Use 60.0 to force double division

// Check if break times exist
        if (timesheetEntity.getBreakIn() != null && timesheetEntity.getBreakOut() != null) {
            // Calculate break duration in minutes, then convert to double hours
            long breakMinutes = Duration.between(timesheetEntity.getBreakIn(), timesheetEntity.getBreakOut()).toMinutes();
            double breakHours = breakMinutes / 60.0; // Use 60.0 for double division

            timesheetEntity.setTotalBreakHour(breakHours); // Set the total break hour field

            // Deduct break hours from total hours worked
            // Ensure hoursWorked is not negative after deduction
            timesheetEntity.setHoursWorked(Math.max(0.0, timesheetEntity.getHoursWorked() - breakHours)); // Use 0.0 for double comparison
        } else {
            // If no break, ensure totalBreakHour is explicitly set to 0.0
            timesheetEntity.setTotalBreakHour(0.0);
        }
       timesheetService.updateTimesheet(id, timesheetEntity);

        return "redirect:/managers/";
    }



    @Transactional
    @GetMapping("/timesheet/{id}/delete")
    public String deleteTimesheet(@PathVariable("id") Long id, Authentication authentication) {
        timesheetService.deleteById(id);
        return "redirect:/managers/";

    }


    @GetMapping("/timesheet/pending")
    public String listPendingTimesheets(Model model, Authentication authentication) {
        EmployeeEntity employee = (EmployeeEntity) authentication.getPrincipal();

        model.addAttribute("timesheets", timesheetService.findByEmployeeBranchAndStatus(employee.getBranch(), TimesheetStatus.PENDING));
        return "manager/pending-timesheets";
    }

    @PostMapping("/timesheet/{id}/approve")
    public String approveTimesheet(@PathVariable Long id, Authentication authentication) {
        EmployeeEntity employee = (EmployeeEntity) authentication.getPrincipal();
        TimesheetEntity timesheet = timesheetService.findById(id);

            timesheet.setApprovedBy(employee);
            timesheetService.updateTimesheetStatus(id, TimesheetStatus.APPROVED);


        return "redirect:/managers/";
    }

    @PostMapping("/pending/timesheet/{id}/approve")
    public String approvePendingTimesheet(@PathVariable Long id, Authentication authentication) {
        EmployeeEntity employee = (EmployeeEntity) authentication.getPrincipal();
        TimesheetEntity timesheet = timesheetService.findById(id);

        timesheet.setApprovedBy(employee);
        timesheetService.updateTimesheetStatus(id, TimesheetStatus.APPROVED);


        return "redirect:/managers/timesheet/pending";
    }


    @GetMapping("/timesheet/filter")
    public String listFilteredTimesheets(Model model,
                                         @RequestParam(name = "employeeId", required = false) Long employeeId,
                                         @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                         @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                         Authentication authentication) {
        EmployeeEntity manager = (EmployeeEntity) authentication.getPrincipal();
        List<EmployeeEntity> employees = employeeService.findByBranchAndRole(manager.getBranch(), Role.EMPLOYEE);

        List<TimesheetEntity> timesheets = timesheetService.findByEmployeeBranch(manager.getBranch());

        if(startDate != null && endDate != null) {
            timesheets = timesheetService.findTimesheetsByDateRange(startDate, endDate);
            if(employeeId != null) {
                timesheets = timesheets.stream().filter(ts -> ts.getEmployee().getId().equals(employeeId)).toList();
            }
        }

        if(employeeId != null && startDate == null && endDate == null) {
            timesheets = timesheetService.findTimesheetsByEmployee(employeeId);
        }



        model.addAttribute("grandTotal", timesheetService.calculateTotalHoursWorked(timesheets));
        model.addAttribute("employees", employees);
        model.addAttribute("timesheets", timesheets);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("employeeId", employeeId);

        return "manager/timesheet-filter";
    }

    @PostMapping("/timesheet/filter")
    public String filterTimesheets(
            @RequestParam(name = "employeeId", required = false) Long employeeId,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {



        // Validate date range
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            model.addAttribute("error", "End date must be after start date.");
            model.addAttribute("timesheets", timesheetService.findAll());
        } else {
            // Fetch filtered timesheets
            List<TimesheetEntity> timesheets = timesheetService.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
            model.addAttribute("timesheets", timesheets);
        }



        // Add employees for dropdown and retain form inputs
        model.addAttribute("employees", employeeService.findEmployeesByBranchAndRole(employeeService.findById(employeeId).getBranch(), Role.EMPLOYEE));
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);


        return "redirect:/managers/timesheet/filter";
    }


    @GetMapping("/tasks/new")
    public String showTaskForm(Model model, Authentication authentication) {
        EmployeeEntity manager = (EmployeeEntity) authentication.getPrincipal();

        model.addAttribute("task", new TaskDto());

        model.addAttribute("employees", employeeService.findByBranchAndRole(manager.getBranch(), Role.EMPLOYEE));

        return "manager/task-form";
    }


    @PostMapping("/tasks")
    public String assignTask(TaskDto task, Authentication authentication) {
        EmployeeEntity manager = (EmployeeEntity) authentication.getPrincipal();
        TaskEntity newTask = new TaskEntity();
        newTask.setAssigner(manager);
        newTask.setAssignee( employeeService.findById(task.getAssigneeId()));
        newTask.setDeadline(task.getDeadline());
        newTask.setDescription(task.getDescription());
        newTask.setStatus(TaskStatus.NOT_STARTED);

        taskService.save(newTask);
        return "redirect:/managers/";
    }

    @PostMapping("/tasks/{id}/update")
    public String updateTaskStatus(@ModelAttribute("task") TaskEntity taskEntity, @PathVariable("id") Long id, @RequestParam TaskStatus status, Authentication authentication) {
        TaskEntity task = taskService.findById(id);

        EmployeeEntity user = (EmployeeEntity) authentication.getPrincipal();
        taskService.updateTaskStatus(id, status);
        return "redirect:/managers/";
    }



    @Transactional
    @GetMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable("id") Long id, Authentication authentication) {
        taskService.deleteTaskEntityById(id);
        return "redirect:/managers/";

    }

    @GetMapping("/timesheet/download/csv")
    public ResponseEntity<byte[]> downloadTimesheetInCsv(
            @RequestParam(name = "employeeId", required = false) Long employeeId,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {

        List<TimesheetEntity> timesheets = timesheetService.findByEmployeeIdAndDateRange(employeeId,startDate, endDate);



        byte[] csvBytes = timesheetService.downloadTimesheetInCSV(timesheets);
        String filename = "timesheets_" + LocalDate.now() + "-" + LocalTime.now() + ".csv";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");


        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }
}
