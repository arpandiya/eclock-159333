package com.adfarms.service;

import com.adfarms.entity.BranchEntity;
import com.adfarms.entity.TimesheetEntity;
import com.adfarms.enums.TimesheetStatus;
import com.adfarms.repository.TimesheetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class TimesheetService {

    private static final Logger logger = LoggerFactory.getLogger(TimesheetService.class);

    @Autowired
    private TimesheetRepository timesheetRepository;

    public List<TimesheetEntity> findByEmployeeBranchAndStatus(BranchEntity branch, TimesheetStatus status) {
        return timesheetRepository.findByEmployeeBranchAndStatus(branch, status);
    }

    public List<TimesheetEntity> findByEmployeeBranch(BranchEntity branch) {
        return timesheetRepository.findByEmployeeBranch(branch);
    }

    public TimesheetEntity findById(Long id) {
        return timesheetRepository.findById(id).orElse(null);
    }

    public TimesheetEntity saveTimesheet(TimesheetEntity newTimesheet) {
        logger.debug("Saving timesheet for employee ID: {}", newTimesheet.getEmployee().getId());

        // Validate clock-in and clock-out
        if (newTimesheet.getClockIn() != null && newTimesheet.getClockOut() != null) {
            if (newTimesheet.getClockOut().isBefore(newTimesheet.getClockIn())) {
                throw new IllegalArgumentException("Clock-out time must be after clock-in time");
            }
            // Calculate hours worked as a double to include partial hours
            Duration workDuration = Duration.between(newTimesheet.getClockIn(), newTimesheet.getClockOut());
            newTimesheet.setHoursWorked(workDuration.toMinutes() / 60.0); // Convert minutes to hours
        }

        // Validate break-in and break-out
        if (newTimesheet.getBreakIn() != null && newTimesheet.getBreakOut() != null) {
            if (newTimesheet.getBreakOut().isBefore(newTimesheet.getBreakIn())) {
                throw new IllegalArgumentException("Break-out time must be after break-in time");
            }
            // Calculate break hours as a double
            Duration breakDuration = Duration.between(newTimesheet.getBreakIn(), newTimesheet.getBreakOut());
            newTimesheet.setTotalBreakHour(breakDuration.toMinutes() / 60.0); // Convert minutes to hours
        }

        return timesheetRepository.save(newTimesheet);
    }


}
