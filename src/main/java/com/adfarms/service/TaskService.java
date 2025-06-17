package com.adfarms.service;

import com.adfarms.entity.EmployeeEntity;
import com.adfarms.entity.TaskEntity;
import com.adfarms.enums.TaskStatus;
import com.adfarms.repository.TaskRepository;
import jakarta.transaction.Transactional;
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

    @Transactional
    public void save(TaskEntity task) {
         taskRepository.save(task);
    }

    public void updateTaskStatus(Long id, TaskStatus status) {
        taskRepository.findById(id).ifPresent(task -> task.setStatus(status));
        taskRepository.save(taskRepository.findById(id).get());
    }

    public TaskEntity findById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public String deleteTaskEntityById(Long id) {
        TaskEntity task = taskRepository.findById(id).orElse(null);
        if(task != null) {
            taskRepository.deleteTaskEntityById(id);
            return "Task deleted successfully";
        }
        return "Task not found ! ";
    }




}
