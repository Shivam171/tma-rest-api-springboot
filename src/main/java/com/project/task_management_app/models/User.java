package com.project.task_management_app.models;

import com.project.task_management_app.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String userImgUrl;

    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    @BatchSize(size = 10)
    private List<Task> createdTasks;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Workspace> ownedWorkspaces = new ArrayList<>();

    @ManyToMany(mappedBy = "members")
    private Set<Workspace> memberWorkspaces = new HashSet<>();

    @ManyToMany(mappedBy = "assignees")
    private Set<Task> assignedTasks = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_login_history", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "login_time")
    private List<LocalDateTime> loginHistory;
}
