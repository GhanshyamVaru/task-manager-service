package com.example.task_manager_service.controller;

import com.example.task_manager_service.enums.Priority;
import com.example.task_manager_service.enums.Status;
import com.example.task_manager_service.models.Task;
import com.example.task_manager_service.service.TaskService;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@RequestBody @Valid Task task) {
        return taskService.createTask(task);
    }

    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Integer id) {
        return taskService.getTaskById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Integer id, @RequestBody @Valid Task task) {
        Task updatedtask = taskService.updateTask(id, task);
        if (updatedtask.equals(new Task())) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.status(200).body(updatedtask);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Integer id) {
        taskService.deleteTask(id);
    }

    @GetMapping
    public List<Task> listTasks(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority) {
        return taskService.listTasks(status, priority);
    }

    @GetMapping("/search")
    public List<Task> searchTasksSimple(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate after,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate before,
            @RequestParam(required = false) String sortBy) {
        return taskService.searchTask(keyword, tag, after, before, sortBy);
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        return taskService.getSummary();
    }

    @GetMapping("/recommended")
    public List<Task> getRecommended() {
        return taskService.getRecommended();
    }

    @GetMapping("/export")
    public void exportTaskCSV(HttpServletResponse response) throws Exception {
        String filename = "task_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".csv";
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        List<Task> tasks = taskService.listTasks(null, null);

        StatefulBeanToCsv<Task> writer = new StatefulBeanToCsvBuilder<Task>(response.getWriter())
                .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                .build();
        writer.write(tasks);
    }
}
