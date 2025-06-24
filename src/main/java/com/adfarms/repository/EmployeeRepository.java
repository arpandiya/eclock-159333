package com.adfarms.repository;

import com.adfarms.entity.BranchEntity;
import com.adfarms.entity.EmployeeEntity;
import com.adfarms.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {
    List<EmployeeEntity> findByBranchAndRole(BranchEntity branch, Role role);
    EmployeeEntity findByEmail(String email);

    List<EmployeeEntity> findEmployeeEntitiesByBranchAndRole(BranchEntity branch, Role role);

    List<EmployeeEntity> findByBranch(BranchEntity branch);
    List<EmployeeEntity> findByRole(Role role);

    String email(String email);
}
