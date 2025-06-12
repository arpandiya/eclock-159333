package com.adfarms.seed;

import com.adfarms.entity.BranchEntity;
import com.adfarms.entity.EmployeeEntity;
import com.adfarms.entity.TaskEntity;
import com.adfarms.enums.Role;
import com.adfarms.repository.EmployeeRepository;
import com.adfarms.service.BranchService;
import com.adfarms.service.EmployeeService;
import com.adfarms.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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

    @Override
    public void run(String... args) throws Exception {

            EmployeeEntity admin = new EmployeeEntity();
            admin.setFirstName("Admin");
            admin.setLastName("SUbedi");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            employeeService.save(admin);

        BranchEntity branch = new BranchEntity();
        branch.setName("Dunollie");
        branch.setLocation("Marton");
        branch.setEmployees(List.of());
        branchService.save(branch);

        EmployeeEntity manager = new EmployeeEntity();
        manager.setFirstName("Manager");
        manager.setLastName("Man");
        manager.setEmail("manager@gmail.com");
        manager.setPassword(passwordEncoder.encode("manager123"));
        manager.setRole(Role.MANAGER);
        manager.setBranch(branchService.findById(1L));
        employeeService.save(manager);



        EmployeeEntity employee = new EmployeeEntity();
        employee.setFirstName("Employee");
        employee.setLastName("Man");
        employee.setEmail("employee@gmail.com");
        employee.setPassword(passwordEncoder.encode("employee123"));
        employee.setRole(Role.EMPLOYEE);
        employee.setBranch(branchService.findById(1L));
        employeeService.save(employee);

        TaskEntity task = new TaskEntity();
        task.setAssignee(employee);
        task.setAssigner(manager);
        task.setDescription("Clean the kitchen");
        task.setStatus(com.adfarms.enums.TaskStatus.NOT_STARTED);
        task.setDeadline(LocalDate.now().plusDays(1));
        taskService.save(task);






    }
}
