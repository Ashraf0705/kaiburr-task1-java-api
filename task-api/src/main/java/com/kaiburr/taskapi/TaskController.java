package com.kaiburr.taskapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks") // Add a base path for all endpoints in this controller
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    // Endpoint 1: PUT a task (Create or Update)
    @PutMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        // Here you would add command validation logic
        Task savedTask = taskRepository.save(task);
        return new ResponseEntity<>(savedTask, HttpStatus.CREATED);
    }

    // Endpoint 2: GET all tasks OR find by name
    @GetMapping
    public List<Task> getTasks(@RequestParam(required = false) String name) {
        if (name != null && !name.isEmpty()) {
            return taskRepository.findByNameContaining(name);
        }
        return taskRepository.findAll();
    }

    // Endpoint 3: GET a single task by ID
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable String id) {
        Optional<Task> taskData = taskRepository.findById(id);

        if (taskData.isPresent()) {
            return new ResponseEntity<>(taskData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Endpoint 4: DELETE a task by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteTask(@PathVariable String id) {
        try {
            taskRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // Add this method inside the TaskController class

    // Endpoint 5: PUT a TaskExecution (Execute a task's command)
    @PostMapping("/{id}/executions")
    public ResponseEntity<Task> executeTask(@PathVariable String id) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Task task = taskOptional.get();
        TaskExecution execution = new TaskExecution();

        try {
            execution.setStartTime(new java.util.Date());

            ProcessBuilder processBuilder = new ProcessBuilder();
            // This is a simple way to handle commands for both Windows and Linux/macOS
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                processBuilder.command("cmd.exe", "/c", task.getCommand());
            } else {
                processBuilder.command("sh", "-c", task.getCommand());
            }

            Process process = processBuilder.start();

            // Capture output
            java.io.InputStream inputStream = process.getInputStream();
            java.util.Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A");
            String output = scanner.hasNext() ? scanner.next() : "";
            
            int exitCode = process.waitFor();
            execution.setEndTime(new java.util.Date());

            if (exitCode == 0) {
                execution.setOutput(output);
            } else {
                // Capture error stream if command fails
                java.io.InputStream errorStream = process.getErrorStream();
                java.util.Scanner errorScanner = new java.util.Scanner(errorStream).useDelimiter("\\A");
                String errorOutput = errorScanner.hasNext() ? errorScanner.next() : "";
                execution.setOutput("Error executing command. Exit code: " + exitCode + "\nOutput:\n" + output + "\nError:\n" + errorOutput);
            }
            
            // Add the new execution to the task's list
            if (task.getTaskExecutions() == null) {
                task.setTaskExecutions(new java.util.ArrayList<>());
            }
            task.getTaskExecutions().add(execution);

            // Save the updated task
            taskRepository.save(task);

            return new ResponseEntity<>(task, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}