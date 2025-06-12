package com.adfarms.entity;

import com.adfarms.enums.TimesheetStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name="timesheets")
public class TimesheetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private EmployeeEntity employee;

   private LocalDate date;

   private LocalTime clockIn;

   private LocalTime clockOut;

    private LocalTime breakIn;

    private LocalTime breakOut;

   private double hoursWorked;

   private double totalBreakHour;

   @Enumerated(EnumType.STRING)
   private TimesheetStatus status;

   @ManyToOne
   @JoinColumn(name = "approved_by_id")
   private EmployeeEntity approvedBy;

   private String note;

}
