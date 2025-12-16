package com.example.task_manager_service.repository;

import com.example.task_manager_service.enums.Priority;
import com.example.task_manager_service.enums.Status;
import com.example.task_manager_service.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    List<Task> findByStatus(Status status);

    List<Task> findByPriority(Priority priority);

    List<Task> findByStatusAndPriority(Status status, Priority priority);

    long countByStatus(Status status);

    long countByPriority(Priority priority);

    @Query("SELECT COUNT(t) FROM task t WHERE t.status != 'DONE' AND t.dueDate < CURRENT_DATE")
    long countOverdueTasks();

    @Query("SELECT t FROM task t WHERE t.status != 'DONE' ORDER BY t.dueDate ASC LIMIT 1")
    Task findNextDueTask();

    @Query("SELECT t FROM task t ORDER BY " +
            "CASE t.priority WHEN 'HIGH' THEN 3 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 1 END DESC, " +
            "t.dueDate ASC, " +
            "LENGTH(t.description) DESC")
    List<Task> findRecommendedTasks();

    List<Task> findByStatusNot(Status status);
}
