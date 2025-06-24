package com.adfarms.service;

import com.adfarms.entity.BranchEntity;
import com.adfarms.entity.EmployeeEntity;
import com.adfarms.entity.TimesheetEntity;
import com.adfarms.enums.Role;
import com.adfarms.enums.TimesheetStatus;
import com.adfarms.repository.TimesheetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import com.opencsv.CSVWriter;

@Service
public class TimesheetService {

    private static final Logger logger = LoggerFactory.getLogger(TimesheetService.class);

    @Autowired
    private TimesheetRepository timesheetRepository;

    public List<TimesheetEntity> findAll() {
        return timesheetRepository.findAll();
    }



    public List<TimesheetEntity> findByEmployeeBranchAndStatus(BranchEntity branch, TimesheetStatus status) {
        return timesheetRepository.findByEmployeeBranchAndStatus(branch, status);
    }

    public List<TimesheetEntity> findByEmployeeBranch(BranchEntity branch) {
        return timesheetRepository.findByEmployeeBranch(branch);
    }

    public List<TimesheetEntity> findByEmployeeIdAndDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return timesheetRepository.findTimesheetEntityByEmployeeIdAndDateAfterAndDateBefore(employeeId, startDate, endDate);
    }

    public List<TimesheetEntity> findByEmployeeIdAndDateRangeAndStatus(Long employeeId, LocalDate startDate, LocalDate endDate, TimesheetStatus status) {
        return timesheetRepository.findTimesheetEntityByEmployeeIdAndDateAfterAndDateBefore(employeeId, startDate, endDate);
    }

    public List<TimesheetEntity> findTimesheetsByEmployee(Long employeeId) {
        return timesheetRepository.findTimesheetEntityByEmployeeId(employeeId);
    }

    public TimesheetEntity findById(Long id) {
        return timesheetRepository.findById(id).orElse(null);
    }

    public TimesheetEntity saveTimesheet(TimesheetEntity newTimesheet) {
        logger.debug("Saving timesheet for employee ID: {}", newTimesheet.getEmployee().getId());

        newTimesheet.setStatus(TimesheetStatus.PENDING);
        // Validate clock-in and clock-out
        if (newTimesheet.getClockIn() != null && newTimesheet.getClockOut() != null) {
            if (newTimesheet.getClockOut().isBefore(newTimesheet.getClockIn())) {
                throw new IllegalArgumentException("Clock-out time must be after clock-in time");
            }
            // Calculate hours worked as a double to include partial hours
            double totalMinutes = Duration.between(newTimesheet.getClockIn(), newTimesheet.getClockOut()).toMinutes();
            double totalHours = totalMinutes / 60.0;
            BigDecimal totalHoursBD = new BigDecimal(totalHours).setScale(2, RoundingMode.HALF_UP);
            newTimesheet.setHoursWorked(totalHoursBD.doubleValue());
        }

        // Validate break-in and break-out
        if (newTimesheet.getBreakIn() != null && newTimesheet.getBreakOut() != null) {
            if (newTimesheet.getBreakOut().isBefore(newTimesheet.getBreakIn())) {
                throw new IllegalArgumentException("Break-out time must be after break-in time");
            }
            // Calculate break hours as a double


            double breakMinutes = Duration.between(newTimesheet.getBreakIn(), newTimesheet.getBreakOut()).toMinutes();
            double breakHours = breakMinutes / 60.0;


            BigDecimal breakHoursBD = new BigDecimal(Double.toString(breakHours))
                    .setScale(2, RoundingMode.HALF_UP);
            newTimesheet.setTotalBreakHour(breakHoursBD.doubleValue());
        }

        return timesheetRepository.save(newTimesheet);
    }


    public void updateTimesheetStatus(Long id, TimesheetStatus status) {
        timesheetRepository.findById(id).ifPresent(timesheet -> timesheet.setStatus(status));
        timesheetRepository.save(timesheetRepository.findById(id).get());
    }

    public void updateTimesheet(Long id, TimesheetEntity timesheet) {
        timesheetRepository.findById(id).ifPresent(timesheetEntity -> {
            timesheetEntity.setClockIn(timesheet.getClockIn());
            timesheetEntity.setClockOut(timesheet.getClockOut());
            timesheetEntity.setBreakIn(timesheet.getBreakIn());
            timesheetEntity.setBreakOut(timesheet.getBreakOut());
            timesheetEntity.setNote(timesheet.getNote() == null ? "" : timesheet.getNote());
            timesheetEntity.setStatus(timesheet.getStatus());
            timesheetEntity.setHoursWorked(timesheet.getHoursWorked());
            timesheetEntity.setTotalBreakHour(timesheet.getTotalBreakHour());
            timesheetEntity.setStatus(TimesheetStatus.PENDING);
        });
        timesheetRepository.save(timesheetRepository.findById(id).get());
    }

    public void deleteById(Long id) {
        timesheetRepository.deleteById(id);
    }


    public List<TimesheetEntity> findTimesheetsByDateRange(LocalDate startDate, LocalDate endDate) {
        return timesheetRepository.findTimesheetEntityByDateGreaterThanEqualAndDateLessThanEqual(startDate, endDate);
    }

    public List<TimesheetEntity> findTimesheetsByBranchAndRole(Long branchId, Role role) {
        List<TimesheetEntity> timesheets = findAll();
        for(TimesheetEntity timesheet : timesheets) {
            if(timesheet.getEmployee().getBranch().getId().equals(branchId) && timesheet.getEmployee().getRole().equals(role)) {
                return timesheets;
            }
        }
        return null;

    }


    public double calculateTotalHoursWorked(List<TimesheetEntity> timesheets) {
        double totalHours = 0.0;

        for (TimesheetEntity timesheet : timesheets) {
            totalHours += timesheet.getHoursWorked();
        }

        return totalHours;
    }




    public byte[] downloadTimesheetInCSV(List<TimesheetEntity> timesheets) throws IOException {
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            CSVWriter csvWriter = new CSVWriter(writer)
            ){
            String[] header = {"Employee Name", "Date", "Clock-In", "Clock-Out", "Break-In", "Break-Out", "Total Hours Worked", "Total Break Hours", "Note"};
            csvWriter.writeNext(header);

            for(TimesheetEntity timesheet : timesheets) {
                String employeeName = timesheet.getEmployee() != null ? timesheet.getEmployee().getFirstName() + " " + timesheet.getEmployee().getLastName() : "N/A";

                String[] rows = {
                        String.valueOf(employeeName),
                        timesheet.getDate().toString(),
                        timesheet.getClockIn() != null ? timesheet.getClockIn().toString() : "N/A",
                        timesheet.getClockOut() != null ? timesheet.getClockOut().toString() : "N/A",
                        timesheet.getBreakIn() != null ? timesheet.getBreakIn().toString() : "N/A",
                        timesheet.getBreakOut() != null ? timesheet.getBreakOut().toString() : "N/A",
                        String.valueOf(timesheet.getHoursWorked()),
                        String.valueOf(timesheet.getTotalBreakHour()),
                        timesheet.getNote() == null ? "" : timesheet.getNote()
                };

                csvWriter.writeNext(rows);
            }

            csvWriter.flush();
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error while generating timesheet CSV", e);
        }
    }

}
