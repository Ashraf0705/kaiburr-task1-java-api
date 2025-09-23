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
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                processBuilder.command("cmd.exe", "/c", task.getCommand());
            } else {
                processBuilder.command("sh", "-c", task.getCommand());
            }

            Process process = processBuilder.start();

            java.util.Scanner stdInputScanner = new java.util.Scanner(process.getInputStream()).useDelimiter("\\A");
            String stdOutput = stdInputScanner.hasNext() ? stdInputScanner.next() : "";

            java.util.Scanner stdErrorScanner = new java.util.Scanner(process.getErrorStream()).useDelimiter("\\A");
            String stdError = stdErrorScanner.hasNext() ? stdErrorScanner.next() : "";
            
            int exitCode = process.waitFor();
            execution.setEndTime(new java.util.Date());

            // Combine both outputs. The error stream often contains useful info even on success.
            StringBuilder combinedOutput = new StringBuilder();
            if (!stdOutput.isEmpty()) {
                combinedOutput.append("--- Standard Output ---\n");
                combinedOutput.append(stdOutput);
            }
            if (!stdError.isEmpty()) {
                if (combinedOutput.length() > 0) combinedOutput.append("\n\n");
                combinedOutput.append("--- Standard Error ---\n");
                combinedOutput.append(stdError);
            }
            
            // Add a final status message
            combinedOutput.append("\n\n--- Status ---\n");
            combinedOutput.append("Command finished with exit code: ").append(exitCode);
            
            execution.setOutput(combinedOutput.toString());
            
            // --- END OF CORRECTION ---

            if (task.getTaskExecutions() == null) {
                task.setTaskExecutions(new java.util.ArrayList<>());
            }
            task.getTaskExecutions().add(execution);

            taskRepository.save(task);

            return new ResponseEntity<>(task, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}