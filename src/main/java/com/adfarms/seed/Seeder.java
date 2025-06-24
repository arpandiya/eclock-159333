package com.adfarms.seed;

import com.adfarms.entity.BranchEntity;
import com.adfarms.entity.EmployeeEntity;
import com.adfarms.entity.TaskEntity;
import com.adfarms.entity.TimesheetEntity;
import com.adfarms.enums.Role;
import com.adfarms.repository.EmployeeRepository;
import com.adfarms.service.BranchService;
import com.adfarms.service.EmployeeService;
import com.adfarms.service.TaskService;
import com.adfarms.service.TimesheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Component
public class Seeder implements CommandLineRunner {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private BranchService branchService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TimesheetService timesheetService;

    @Override
    public void run(String... args) throws Exception {

            EmployeeEntity admin = new EmployeeEntity();
            admin.setFirstName("Admin");
            admin.setLastName("Subedi");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            employeeService.save(admin);

        BranchEntity dunollieBranch = new BranchEntity();
        dunollieBranch.setName("Dunollie");
        dunollieBranch.setLocation("Marton");
        dunollieBranch.setEmployees(List.of());
        branchService.save(dunollieBranch);

        BranchEntity palmyBranch = new BranchEntity();
        palmyBranch.setName("Palmy Branch");
        palmyBranch.setLocation("Manawatu");
        palmyBranch.setEmployees(List.of());
        branchService.save(palmyBranch);

        EmployeeEntity manager = new EmployeeEntity();
        manager.setFirstName("Duno");
        manager.setLastName("Manager");
        manager.setEmail("dunomanager@gmail.com");
        manager.setPassword(passwordEncoder.encode("manager123"));
        manager.setRole(Role.MANAGER);
        manager.setBranch(branchService.findById(1L));
        employeeService.save(manager);

        EmployeeEntity palmyManager = new EmployeeEntity();
        palmyManager.setFirstName("Palmy");
        palmyManager.setLastName("Manager");
        palmyManager.setEmail("palmymanager@gmail.com");
        palmyManager.setPassword(passwordEncoder.encode("manager123"));
        palmyManager.setRole(Role.MANAGER);
        palmyManager.setBranch(branchService.findById(2L));
        employeeService.save(palmyManager);



        EmployeeEntity dunoEmp = new EmployeeEntity();
        dunoEmp.setFirstName("Dunollie");
        dunoEmp.setLastName("Employee");
        dunoEmp.setEmail("dunoemployee@gmail.com");
        dunoEmp.setPassword(passwordEncoder.encode("employee123"));
        dunoEmp.setRole(Role.EMPLOYEE);
        dunoEmp.setBranch(branchService.findById(1L));
        employeeService.save(dunoEmp);

        EmployeeEntity dunoEmp2 = new EmployeeEntity();
        dunoEmp2.setFirstName("Dunollie");
        dunoEmp2.setLastName("Employee");
        dunoEmp2.setEmail("dunoemployee2@gmail.com");
        dunoEmp2.setPassword(passwordEncoder.encode("employee123"));
        dunoEmp2.setRole(Role.EMPLOYEE);
        dunoEmp2.setBranch(branchService.findById(1L));
        employeeService.save(dunoEmp2);




        EmployeeEntity palmyEmp = new EmployeeEntity();
        palmyEmp.setFirstName("Palmy");
        palmyEmp.setLastName("Employee");
        palmyEmp.setEmail("palmyemployee@gmail.com");
        palmyEmp.setPassword(passwordEncoder.encode("employee123"));
        palmyEmp.setRole(Role.EMPLOYEE);
        palmyEmp.setBranch(branchService.findById(2L));
        employeeService.save(palmyEmp);

        EmployeeEntity palmyEmp2 = new EmployeeEntity();
        palmyEmp2.setFirstName("Palmy");
        palmyEmp2.setLastName("Employee");
        palmyEmp2.setEmail("palmyemployee2@gmail.com");
        palmyEmp2.setPassword(passwordEncoder.encode("employee123"));
        palmyEmp2.setRole(Role.EMPLOYEE);
        palmyEmp2.setBranch(branchService.findById(2L));
        employeeService.save(palmyEmp2);



        TaskEntity task = new TaskEntity();
        task.setAssignee(dunoEmp);
        task.setAssigner(manager);
        task.setDescription("Clean the kitchen");
        task.setStatus(com.adfarms.enums.TaskStatus.NOT_STARTED);
        task.setDeadline(LocalDate.now().plusDays(1));
        taskService.save(task);

        TaskEntity palmyTask = new TaskEntity();
        palmyTask.setAssignee(palmyEmp);
        palmyTask.setAssigner(palmyManager);
        palmyTask.setDescription("Finish the Project Web");
        palmyTask.setStatus(com.adfarms.enums.TaskStatus.NOT_STARTED);
        palmyTask.setDeadline(LocalDate.now().plusDays(1));
        taskService.save(palmyTask);


        TimesheetEntity timesheet = new TimesheetEntity();

        timesheet.setEmployee(dunoEmp);
        timesheet.setDate(LocalDate.now());
        timesheet.setClockIn(LocalTime.now().minusHours(1));
        timesheet.setClockOut(LocalTime.now());
        timesheet.setNote("Good work");
        timesheet.setStatus(com.adfarms.enums.TimesheetStatus.PENDING);
        timesheetService.saveTimesheet(timesheet);

        TimesheetEntity palmyTimesheet = new TimesheetEntity();

        palmyTimesheet.setEmployee(palmyEmp);
        palmyTimesheet.setDate(LocalDate.now());
        palmyTimesheet.setClockIn(LocalTime.now().minusHours(4).minusMinutes(30));
        palmyTimesheet.setClockOut(LocalTime.now());
        palmyTimesheet.setNote("Good work palmy team");
        palmyTimesheet.setStatus(com.adfarms.enums.TimesheetStatus.PENDING);
        timesheetService.saveTimesheet(palmyTimesheet);




    }
}
