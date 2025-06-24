package com.adfarms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter
public class TimesheetDto {

    @PastOrPresent(message = "Date cannot be in the future.")
    @NotBlank(message = "Date cannot be blank.")
    private LocalDate date;

    @NotBlank(message = "Clock-In cannot be blank.")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime clockIn;

    @NotBlank(message = "Clock-Out cannot be blank.")
    @PastOrPresent
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime clockOut;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime breakIn;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime breakOut;

    private String note;

}
