package com.project.task_management_app.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.task_management_app.models.Task;
import com.project.task_management_app.models.User;
import com.project.task_management_app.models.Workspace;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
public class UserDetailsImpl implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String username;
    private String email;
    private String userImgUrl;
    private List<Task> tasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Workspace> ownedWorkspaces;
    private Set<Workspace> memberWorkspaces;
    private Set<Task> assignedTasks;

    @JsonIgnore
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(UUID id, String userImgUrl, String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities,
                           List<Task> tasks, LocalDateTime createdAt, LocalDateTime updatedAt, List<Workspace> ownedWorkspaces, Set<Workspace> memberWorkspaces, Set<Task> assignedTasks) {
        this.id = id;
        this.userImgUrl = userImgUrl;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.tasks = tasks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.ownedWorkspaces = ownedWorkspaces;
        this.memberWorkspaces = memberWorkspaces;
        this.assignedTasks = assignedTasks;
    }

    public static UserDetailsImpl build(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());

        return new UserDetailsImpl(
                user.getId(),
                user.getUserImgUrl(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                List.of(authority),
                user.getCreatedTasks(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getOwnedWorkspaces(),
                user.getMemberWorkspaces(),
                user.getAssignedTasks()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}