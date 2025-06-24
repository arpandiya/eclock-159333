package com.adfarms.service;

import com.adfarms.entity.BranchEntity;
import com.adfarms.entity.EmployeeEntity;
import com.adfarms.enums.Role;
import com.adfarms.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService implements UserDetailsService {
   @Autowired
   private EmployeeRepository employeeRepository;
    @Autowired
    private BranchService branchService;



    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        EmployeeEntity employee = employeeRepository.findByEmail(email);

        if (employee == null) throw new UsernameNotFoundException("Employee not found ! ");
        return employee;
    }


    public List<EmployeeEntity> findEmployeesByBranchAndRole(BranchEntity branch, Role role) {
        return employeeRepository.findEmployeeEntitiesByBranchAndRole(branch, role);
    }


    public List<EmployeeEntity> findByBranchAndRole(BranchEntity branch, Role role) {
        return employeeRepository.findByBranchAndRole(branch, role);
    }
   public EmployeeEntity findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }
    public EmployeeEntity findById(Long id) {
        return employeeRepository.findById(id).orElse(null);
    }

    public EmployeeEntity save(EmployeeEntity employee) {

        return employeeRepository.save(employee);
    }

    public List<EmployeeEntity> findAll() {
        return employeeRepository.findAll();
    }

    public void deleteById(Long id) {
        employeeRepository.deleteById(id);
    }

    public boolean changePassword(String email, String oldPassword, String newPassword) {
        EmployeeEntity employee = employeeRepository.findByEmail(email);
        return true;
    }

    public List<EmployeeEntity> findEmployeeByRole(Role role) {
        return employeeRepository.findByRole(role);
    }


}
