package com.project.task_management_app.repositories;

import com.project.task_management_app.enums.TaskPriority;
import com.project.task_management_app.enums.TaskStatus;
import com.project.task_management_app.models.Task;
import com.project.task_management_app.models.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    Optional<Task> findByTitle(String title);

    Optional<Task> findByTitleAndWorkspace(String title, Workspace workspace);

    Optional<Task> findByIdAndWorkspace(UUID id, Workspace workspace);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByWorkspace(Workspace workspace);

    List<Task> findByWorkspaceAndStatus(Workspace workspace, TaskStatus status);

    List<Task> findByPriority(TaskPriority priority);

    List<Task> findByWorkspaceAndPriority(Workspace workspace, TaskPriority priority);

    List<Task> findByCategory(String category);

    List<Task> findByWorkspaceAndCategory(Workspace workspace, String category);

    Page<Task> findByWorkspace(Workspace workspace, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE " +
            "t.workspace = :workspace AND " +
            "(:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:category IS NULL OR LOWER(t.category) = LOWER(:category)) AND " +
            "(:fromDate IS NULL OR t.dueDate >= :fromDate) AND " +
            "(:toDate IS NULL OR t.dueDate <= :toDate)")
    Page<Task> searchTasksByWorkspace(
            @Param("workspace") Workspace workspace,
            @Param("title") String title,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("category") String category,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    @Query(value = "SELECT * FROM tasks WHERE workspace_id = :workspaceId ORDER BY created_at DESC LIMIT 5", nativeQuery = true)
    List<Task> findRecentTasksByWorkspace(@Param("workspaceId") UUID workspaceId);
    @Query(value = "SELECT * FROM tasks WHERE workspace_id = :workspace_id ORDER BY created_at DESC LIMIT 5", nativeQuery = true)
    List<Task> findRecentTasksByWorkspace(@Param("workspace_id") Workspace workspace);
}