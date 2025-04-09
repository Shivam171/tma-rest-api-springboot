package com.project.task_management_app.repositories;

import com.project.task_management_app.models.User;
import com.project.task_management_app.models.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    Optional<Workspace> findByIdAndEntryCode(UUID id, String entryCode);
    Optional<Workspace> findByName(String name);
    List<Workspace> findByOwner(User owner);
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END " +
            "FROM Workspace w WHERE LOWER(w.name) = LOWER(:name) AND w.owner = :owner")
    boolean existsByNameIgnoreCaseAndOwner(@Param("name") String name, @Param("owner") User owner);
    boolean existsById(UUID id);
}
