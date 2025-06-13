package com.adfarms.controller;

import com.adfarms.dto.TimesheetForm;
import com.adfarms.entity.EmployeeEntity;
import com.adfarms.entity.TaskEntity;
import com.adfarms.entity.TimesheetEntity;
import com.adfarms.enums.TaskStatus;
import com.adfarms.enums.TimesheetStatus;
import com.adfarms.service.TaskService;
import com.adfarms.service.TimesheetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    @Autowired
    private TimesheetService timesheetService;
    @Autowired
    private TaskService taskService;

    @GetMapping("/")
    public String showTimesheetList(Model model, Authentication authentication) {
        EmployeeEntity employee = (EmployeeEntity) authentication.getPrincipal();
        model.addAttribute("tasks", taskService.findByAssignee(employee));
        model.addAttribute("timesheets", timesheetService.findByEmployeeBranchAndStatus(employee.getBranch(), TimesheetStatus.PENDING));

        return "employee/employee-dashboard";
    }

    @GetMapping("/timesheets/new")
    public String showTimesheetForm(Model model) {
        model.addAttribute("timesheet", new TimesheetForm());
        return "employee/timesheet-form";
    }

    @PostMapping("/timesheets")
    public String submitTimesheet(TimesheetForm timesheet, Authentication authentication) {
        EmployeeEntity employee = (EmployeeEntity) authentication.getPrincipal();

        TimesheetEntity newTimesheet = new TimesheetEntity();

        newTimesheet.setEmployee(employee);
        newTimesheet.setDate(timesheet.getDate());
        newTimesheet.setClockIn(timesheet.getClockIn());
        newTimesheet.setClockOut(timesheet.getClockOut());
        newTimesheet.setBreakIn(timesheet.getBreakIn());
        newTimesheet.setBreakOut(timesheet.getBreakOut());
        newTimesheet.setNote(timesheet.getNote() == null ? "" : timesheet.getNote());
        newTimesheet.setStatus(TimesheetStatus.PENDING);

        newTimesheet.setHoursWorked(Duration.between(newTimesheet.getClockIn(), newTimesheet.getClockOut()).toHours() );
        newTimesheet.setTotalBreakHour(Duration.between(newTimesheet.getBreakIn(), newTimesheet.getBreakOut()).toHours() );

        timesheetService.saveTimesheet(newTimesheet);
        return "redirect:/employee/timesheets/new";
    }

    @GetMapping("/tasks")
    public String listTasks(Model model, Authentication authentication) {
        EmployeeEntity employee = (EmployeeEntity) authentication.getPrincipal();
        model.addAttribute("tasks", taskService.findByAssignee(employee));
        return "employee/task-list";
    }

    @GetMapping("/tasks/{id}/edit")
    public String showUpdateTaskForm(@PathVariable Long id, Model model, Authentication authentication) {
        EmployeeEntity employee = (EmployeeEntity) authentication.getPrincipal();
        TaskEntity task = taskService.findById(id);
        logger.warn("Task ID: {}, Employee ID: {}", id, employee.getId());

        if (task != null && task.getAssignee().equals(employee)) {
            model.addAttribute("task", task);
            return "employee/edit-task";
        }
        return "redirect:/employee/";
    }

    @PostMapping("/tasks/{id}/update")
    public String updateTaskStatus(@PathVariable Long id, @ModelAttribute("task") TaskStatus status, Authentication authentication) {
        EmployeeEntity employee = (EmployeeEntity) authentication.getPrincipal();
        TaskEntity task = taskService.findById(id);
        if(task != null && task.getAssignee().equals(employee)) {
            taskService.updateTaskStatus(id, status);
        }

        return "redirect:/employee/";
    }


}
