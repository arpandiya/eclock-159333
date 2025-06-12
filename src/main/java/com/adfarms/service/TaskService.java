package com.adfarms.service;

import com.adfarms.entity.EmployeeEntity;
import com.adfarms.entity.TaskEntity;
import com.adfarms.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<TaskEntity> findByAssignee(EmployeeEntity assignee) {
        return taskRepository.findByAssignee(assignee);
    }

    public List<TaskEntity> findByAssigner(EmployeeEntity assigner) {
        return taskRepository.findByAssigner(assigner);
    }

    public TaskEntity save(TaskEntity task) {
        return taskRepository.save(task);
    }

    public TaskEntity findById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }
}
