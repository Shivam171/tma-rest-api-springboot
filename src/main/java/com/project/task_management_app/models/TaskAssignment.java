package com.project.task_management_app.models;

import com.project.task_management_app.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "task_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User assignee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.PENDING;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
}
