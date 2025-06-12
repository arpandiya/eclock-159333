package com.adfarms.entity;

import com.adfarms.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name="tasks")
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @ManyToOne
    @JoinColumn(name = "assigner_id")
    private EmployeeEntity assigner;


    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private EmployeeEntity assignee;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private LocalDate deadline;


}
