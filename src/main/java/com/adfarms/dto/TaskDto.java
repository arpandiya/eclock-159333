package com.adfarms.dto;



import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class TaskDto
{

    @NotBlank
    private String description;

    @NotBlank
    private Long assignerId;

    @NotBlank
    private Long assigneeId;

    @NotBlank
    @Future
    private LocalDate deadline;
}
