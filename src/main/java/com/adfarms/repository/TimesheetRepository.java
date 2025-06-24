package com.adfarms.repository;

import com.adfarms.entity.BranchEntity;
import com.adfarms.entity.EmployeeEntity;
import com.adfarms.entity.TimesheetEntity;
import com.adfarms.enums.TimesheetStatus;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public interface TimesheetRepository extends JpaRepository<TimesheetEntity, Long> {
    List<TimesheetEntity> findByEmployeeBranchAndStatus(BranchEntity branch, TimesheetStatus status);

    List<TimesheetEntity> findByEmployeeBranch(BranchEntity branch);

    List<TimesheetEntity> findTimesheetEntityByEmployeeId(Long employeeId);

    List<TimesheetEntity> findTimesheetEntityByDateGreaterThanEqualAndDateLessThanEqual(LocalDate dateAfter, LocalDate dateBefore);

    List<TimesheetEntity> findTimesheetEntityByEmployeeIdAndDateAfterAndDateBefore(Long employeeId, LocalDate dateAfter, LocalDate dateBefore);

    List<TimesheetEntity> findTimesheetsEntityByEmployee(EmployeeEntity employee);




    @Query("SELECT t FROM TimesheetEntity t WHERE t.employee.id = :employeeId AND t.date >= :startDate AND t.date <= :endDate")
    List<TimesheetEntity> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
}
