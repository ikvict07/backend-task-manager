package com.backend.taskmanager.repository;

import com.backend.taskmanager.model.Task;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query(value = "SELECT * FROM tasks WHERE user_id = :userId LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Task> findTasksByUserIdWithLimitAndOffset(@Param("userId") Long userId, @Param("limit") int limit, @Param("offset") int offset);

    void deleteById(@NonNull Long id);
}
