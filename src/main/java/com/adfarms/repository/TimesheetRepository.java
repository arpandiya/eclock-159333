package com.adfarms.repository;

import com.adfarms.entity.BranchEntity;
import com.adfarms.entity.TimesheetEntity;
import com.adfarms.enums.TimesheetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimesheetRepository extends JpaRepository<TimesheetEntity, Long> {
    List<TimesheetEntity> findByEmployeeBranchAndStatus(BranchEntity branch, TimesheetStatus status);

    List<TimesheetEntity> findByEmployeeBranch(BranchEntity branch);
}
