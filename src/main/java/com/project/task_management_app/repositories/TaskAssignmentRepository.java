package com.project.task_management_app.repositories;

import com.project.task_management_app.models.Task;
import com.project.task_management_app.models.TaskAssignment;
import com.project.task_management_app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, UUID> {
    List<TaskAssignment> findByTask(Task task);
    TaskAssignment findByTaskAndAssignee(Task task, User assignee);
    void deleteByTaskAndAssignee(Task task, User assignee);
}
