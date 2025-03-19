package com.project.task_management_app.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.task_management_app.models.Task;
import com.project.task_management_app.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

    @JsonIgnore
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(UUID id, String userImgUrl, String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities,
                           List<Task> tasks, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userImgUrl = userImgUrl;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.tasks = tasks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
                user.getTasks(),
                user.getCreatedAt(),
                user.getUpdatedAt()
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