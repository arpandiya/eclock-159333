package com.adfarms.dto;



import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class TaskForm
{

    private String description;

    private Long assignerId;

    private Long assigneeId;

    private LocalDate deadline;
}
