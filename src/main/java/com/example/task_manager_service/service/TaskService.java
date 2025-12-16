package com.example.task_manager_service.service;

import com.example.task_manager_service.enums.Priority;
import com.example.task_manager_service.enums.Status;
import com.example.task_manager_service.models.Task;
import com.example.task_manager_service.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.example.task_manager_service.constant.LoggerConstants.*;

@Slf4j
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;


    public Task createTask(Task task) {
        log.info(ENTER_INTO_METHOD, "TaskService|createTask");
        return taskRepository.save(task);
    }

    public Task getTaskById(Integer id) {
        log.info(ENTER_INTO_METHOD, "TaskService|getTaskById with id: {}", id);
        return taskRepository.findById(id).orElse(null);
    }

    public Task updateTask(Integer id, Task task) {
        log.info(ENTER_INTO_METHOD, "TaskService|updatedTask");
        Task existing = getTaskById(id);
        if (existing != null) {
            existing.setTitle(task.getTitle());
            existing.setDescription(task.getDescription());
            existing.setPriority(task.getPriority());
            existing.setStatus(task.getStatus());
            existing.setTags(task.getTags());
            existing.setDueDate(task.getDueDate());
            existing.setUpdatedAt(LocalDateTime.now());
            log.info(EXIT_METHOD_WITH_SUCCESS, "TaskService|updatedTask");
            return taskRepository.save(existing);
        }
        log.info(EXIT_METHOD_WITH_ERROR, "TaskService|updatedTask");
        return new Task();
    }

    public void deleteTask(Integer id) {
        log.info(ENTER_INTO_METHOD, "TaskService|deleteTask with task id: {}", id);
        taskRepository.deleteById(id);
    }

    public List<Task> listTasks(Status status, Priority priority) {
        log.info(ENTER_INTO_METHOD, "TaskService|listTask");
        if (status != null && priority != null) {
            log.info(EXIT_METHOD_WITH_SUCCESS, "TaskService|listTask with priority:{} and status:{}", priority, status);
            return taskRepository.findByStatusAndPriority(status, priority);
        } else if (status != null) {
            log.info(EXIT_METHOD_WITH_SUCCESS, "TaskService|listTask with status: {}", status);
            return taskRepository.findByStatus(status);
        } else if (priority != null) {
            log.info(EXIT_METHOD_WITH_SUCCESS, "TaskService|listTask with priority:{}", priority);
            return taskRepository.findByPriority(priority);
        }
        log.info(EXIT_METHOD_WITH_SUCCESS, "TaskService|listTask");
        return taskRepository.findAll();
    }

    public List<Task> searchTask(String keyword, String tag, LocalDate after, LocalDate before, String sortBy) {
        log.info(ENTER_INTO_METHOD, "TaskService|searchTask");
        List<Task> allTasks = taskRepository.findAll();
        List<Task> matchedTasks = new ArrayList<>();

        for (Task task : allTasks) {
            boolean isMatched = true;
            if (keyword != null && !keyword.isBlank()) {
                String lowerKeyword = keyword.toLowerCase();
                boolean inTitle = task.getTitle() != null && task.getTitle().toLowerCase().contains(lowerKeyword);
                boolean inDesc = task.getDescription() != null && task.getDescription().toLowerCase().contains(lowerKeyword);
                if (!(inTitle || inDesc)) {
                    isMatched = false;
                }
            }
            if (tag != null && !tag.isBlank()) {
                if (task.getTags() == null || !task.getTags().contains(tag)) {
                    isMatched = false;
                }
            }
            if (after != null && (task.getDueDate() == null || task.getDueDate().isBefore(after))) {
                isMatched = false;
            }
            if (before != null && (task.getDueDate() == null || !task.getDueDate().isBefore(before))) {
                isMatched = false;
            }
            if (isMatched) {
                matchedTasks.add(task);
            }
        }
        if (sortBy != null) {
            if (sortBy.equalsIgnoreCase("dueDate")) {
                matchedTasks.sort(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
            } else if (sortBy.equalsIgnoreCase("priority")) {
                matchedTasks.sort(Comparator.comparing(task -> {
                    if (task.getPriority() == null) return 0;
                    return switch (task.getPriority()) {
                        case HIGH -> 3;
                        case MEDIUM -> 2;
                        case LOW -> 1;
                    };
                }, Comparator.reverseOrder()));
            }
        }
        return matchedTasks;
    }

    public Map<String, Object> getSummary() {
        log.info(ENTER_INTO_METHOD, "TaskService|getSummary");
        long total = taskRepository.count();
        long completed = taskRepository.countByStatus(Status.DONE);
        long pending = total - completed;
        long high = taskRepository.countByPriority(Priority.HIGH);
        long medium = taskRepository.countByPriority(Priority.MEDIUM);
        long low = taskRepository.countByPriority(Priority.LOW);
        long overdue = taskRepository.countOverdueTasks();
        Task nextDue = taskRepository.findNextDueTask();

        return Map.of(
                "total", total,
                "completed", completed,
                "pending", pending,
                "byPriority", Map.of("HIGH", high, "MEDIUM", medium, "LOW", low),
                "overdue", overdue,
                "nextDueTask", nextDue
        );
    }

    public List<Task> getRecommended() {
        log.info(ENTER_INTO_METHOD, "TaskService|getRecommended");
        return taskRepository.findRecommendedTasks();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void pendingTasks() {
        List<Task> pending = taskRepository.findByStatusNot(Status.DONE);
        log.info("Pending tasks: {}", pending.size());
        for (Task task : pending) {
            log.info("Task ID: {}, Title: {}", task.getId(), task.getTitle());
        }
    }
}
