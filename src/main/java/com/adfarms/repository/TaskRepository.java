package com.adfarms.repository;

import com.adfarms.entity.EmployeeEntity;
import com.adfarms.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findByAssignee(EmployeeEntity assignee);
    List<TaskEntity> findByAssigner(EmployeeEntity assigner);
    void deleteTaskEntityById(Long id);


}
