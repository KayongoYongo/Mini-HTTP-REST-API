package service;

import model.Task;
import repository.TaskRepository;

import java.util.List;

public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    // Get all tasks
    public List<Task> getAllTasks() {
        return repository.findAll();
    }

    // Get a single task — throws if not found
    public Task getTaskById(int id) {
        Task task = repository.findById(id);
        if (task == null) {
            throw new IllegalArgumentException("Task with id " + id + " not found");
        }
        return task;
    }

    // Create a new task — validates required fields
    public Task createTask(String title, String description) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        return repository.save(title.trim(), description);
    }

    // Update an existing task
    public Task updateTask(int id, String title, String description, Task.Status status) {
        Task task = repository.update(id, title, description, status);
        if (task == null) {
            throw new IllegalArgumentException("Task with id " + id + " not found");
        }
        return task;
    }

    // Delete a task
    public void deleteTask(int id) {
        boolean deleted = repository.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Task with id " + id + " not found");
        }
    }
}