package com.adfarms.dto;

import com.adfarms.entity.EmployeeEntity;
import com.adfarms.enums.TimesheetStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter
public class TimesheetForm {

    private LocalDate date;

    private LocalTime clockIn;

    private LocalTime clockOut;

    private LocalTime breakIn;

    private LocalTime breakOut;

    private String note;

}
