package com.adfarms.controller;

import com.adfarms.dto.TimesheetDto;
import com.adfarms.entity.EmployeeEntity;
import com.adfarms.entity.TaskEntity;
import com.adfarms.entity.TimesheetEntity;
import com.adfarms.enums.TaskStatus;
import com.adfarms.enums.TimesheetStatus;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/employee")
@PreAuthorize("hasRole('ROLE_EMPLOYEE')")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    @Autowired
    private TimesheetService timesheetService;
    @Autowired
    private TaskService taskService;

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/")
    public String showTimesheetList(Model model, Authentication authentication) {
        EmployeeEntity employee = (EmployeeEntity) authentication.getPrincipal();
        model.addAttribute("tasks", taskService.findByAssignee(employee));
        model.addAttribute("timesheets", timesheetService.findTimesheetsByEmployee(employee.getId()));
        model.addAttribute("approved", "APPROVED");
        return "employee/employee-dashboard";
    }

    @GetMapping("/timesheet/new")
    public String showTimesheetForm(Model model) {
        model.addAttribute("timesheet", new TimesheetDto());


        return "employee/timesheet-form";
    }

    @PostMapping("/timesheet")
    public String submitTimesheet(@ModelAttribute("timesheet") TimesheetDto timesheet,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes,
                                  BindingResult bindingResult) {

//        if(bindingResult.hasErrors()) {
//            return "employee/timesheet-form";
//        }
//
//        if (timesheet.getClockIn() != null && timesheet.getClockOut() != null &&
//                timesheet.getClockOut().isBefore(timesheet.getClockIn())) {
//            bindingResult.rejectValue("clockOut", "time.order", "Clock Out time must be after Clock In time.");
//            return "employee/timesheet-form";
//        }
//
//        if (timesheet.getBreakIn() != null && timesheet.getBreakOut() != null &&
//                timesheet.getBreakOut().isBefore(timesheet.getBreakIn())) {
//            bindingResult.rejectValue("breakOut", "time.order", "Break Out time must be after Break In time.");
//            return "employee/timesheet-form";
//        }

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


//        double totalMinutes = Duration.between(newTimesheet.getClockIn(), newTimesheet.getClockOut()).toMinutes();
//        double totalHours = totalMinutes / 60.0;
//        BigDecimal totalHoursBD = new BigDecimal(totalHours).setScale(2, RoundingMode.HALF_UP);
//        newTimesheet.setHoursWorked(totalHoursBD.doubleValue());
//
//// Check if break times exist
//        if (newTimesheet.getBreakIn() != null && newTimesheet.getBreakOut() != null) {
//            // Calculate break duration in hours
//            double breakMinutes = Duration.between(newTimesheet.getBreakIn(), newTimesheet.getBreakOut()).toMinutes();
//            double breakHours = breakMinutes / 60.0;
//
//
//            BigDecimal breakHoursBD = new BigDecimal(Double.toString(breakHours))
//                    .setScale(2, RoundingMode.HALF_UP);
//            newTimesheet.setTotalBreakHour(breakHoursBD.doubleValue()); // Set the total break hour field
//
//            // Deduct break hours from total hours worked
//            // Ensure hoursWorked is not negative after deduction
//
//            newTimesheet.setHoursWorked(Math.max(0, newTimesheet.getHoursWorked() - breakHours));
//        } else {
//            // If no break, ensure totalBreakHour is explicitly set to 0 or handled appropriately
//            // This prevents null or unexpected values if it's not set elsewhere.
//            newTimesheet.setTotalBreakHour(0D);
//        }

        timesheetService.saveTimesheet(newTimesheet);
        redirectAttributes.addFlashAttribute("successMessage", "Timesheet submitted successfully!");
        return "redirect:/employee/";
    }


    @GetMapping("/timesheet/{id}/edit")
    public String showEditTimesheetForm(@PathVariable("id") Long id, Model model) {
        TimesheetEntity timesheet = timesheetService.findById(id);
        model.addAttribute("timesheet", timesheet);
        return "employee/edit-timesheet";
    }

    @PostMapping("/timesheet/{id}/edit")
    public String updateTimesheet(@PathVariable("id") Long id, @ModelAttribute("timesheet") TimesheetEntity timesheetEntity, Model model) {

        // Calculate initial hours worked in minutes, then convert to double hours
        double minutesWorked = Duration.between(timesheetEntity.getClockIn(), timesheetEntity.getClockOut()).toMinutes();
        timesheetEntity.setHoursWorked(minutesWorked / 60.0); // Use 60.0 to force double division

        // Check if break times exist
        if (timesheetEntity.getBreakIn() != null && timesheetEntity.getBreakOut() != null) {
            // Calculate break duration in minutes, then convert to double hours
            double breakMinutes = Duration.between(timesheetEntity.getBreakIn(), timesheetEntity.getBreakOut()).toMinutes();
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

        return "redirect:/employee/";
    }



    @Transactional
    @GetMapping("/timesheet/{id}/delete")
    public String deleteTimesheet(@PathVariable("id") Long id, Authentication authentication) {
        timesheetService.deleteById(id);
        return "redirect:/employee/";

    }




    @GetMapping("/timesheet/filter")
    public String listFilteredTimesheets(Model model,

                                         @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                         @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                         Authentication authentication) {
        EmployeeEntity user = (EmployeeEntity) authentication.getPrincipal();

        EmployeeEntity employee = employeeService.findById(user.getId());
        List<TimesheetEntity> timesheets = new ArrayList<>();

        if(startDate != null && endDate != null) {
            timesheets = timesheetService.findTimesheetsByDateRange(startDate, endDate);
            if(employee != null) {
                timesheets = timesheets.stream().filter(ts -> ts.getEmployee().getId().equals(employee.getId())).toList();
            }
        }

        if(employee != null && startDate == null && endDate == null) {
            timesheets = timesheetService.findTimesheetsByEmployee(employee.getId());
        }
        model.addAttribute("grandTotal", timesheetService.calculateTotalHoursWorked(timesheets));
        model.addAttribute("employee", employee);
        model.addAttribute("timesheets", timesheets);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "employee/timesheet-filter";
    }

    @PostMapping("/timesheet/filter")
    public String filterTimesheets(Authentication authentication,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        EmployeeEntity employee = (EmployeeEntity) authentication.getPrincipal();

        // Validate date range
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            model.addAttribute("error", "End date must be after start date.");
            model.addAttribute("timesheets", timesheetService.findTimesheetsByEmployee(employee.getId()));
        } else {
            // Fetch filtered timesheets
            List<TimesheetEntity> timesheets = timesheetService.findByEmployeeIdAndDateRange(employee.getId(), startDate, endDate);
            model.addAttribute("timesheets", timesheets);
        }

        // Add employees for dropdown and retain form inputs

        model.addAttribute("employeeId", employee.getId());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "redirect:/employee/timesheet/filter";
    }

    @GetMapping("/timesheet/filter/reset")
    public void resetTimesheetFilter(Model model) {
        model.addAttribute("employeeId", null);
        model.addAttribute("startDate", null);
        model.addAttribute("endDate", null);
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






    @GetMapping("/tasks")
    public String listTasks(Model model, Authentication authentication) {
        EmployeeEntity employee = (EmployeeEntity) authentication.getPrincipal();
        model.addAttribute("tasks", taskService.findByAssignee(employee));
        return "employee/task-list";
    }



    @PostMapping("/tasks/{id}/update")
    public String updateTaskStatus(@ModelAttribute("task") TaskEntity taskEntity, @PathVariable("id") Long id, @RequestParam TaskStatus status, Authentication authentication) {
        EmployeeEntity employee = (EmployeeEntity) authentication.getPrincipal();
        TaskEntity task = taskService.findById(id);

            taskService.updateTaskStatus(id, status);


        return "redirect:/employee/";
    }







}
