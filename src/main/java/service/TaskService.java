package service;

import model.Task;
import repository.TaskRepository;

import java.util.List;

public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    //  READ 

    public List<Task> getAllTasks() {
        return repository.findAll();
    }

    public Task getTaskById(int id) {
        validateId(id);

        Task task = repository.findById(id);
        if (task == null) {
            throw new IllegalArgumentException("Task with id " + id + " not found");
        }

        return task;
    }

    //  CREATE 

    public Task createTask(String title, String description) {
        validateTitle(title);

        String cleanTitle = title.trim();
        String cleanDescription = (description != null) ? description.trim() : null;

        return repository.save(cleanTitle, cleanDescription);
    }

    // UPDATE

    public Task updateTask(int id, String title, String description, Task.Status status) {
        validateId(id);

        // Optional validation (only validate if provided)
        if (title != null && title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        if (status != null && !isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid status value");
        }

        String cleanTitle = (title != null) ? title.trim() : null;
        String cleanDescription = (description != null) ? description.trim() : null;

        Task task = repository.update(id, cleanTitle, cleanDescription, status);

        if (task == null) {
            throw new IllegalArgumentException("Task with id " + id + " not found");
        }

        return task;
    }

    //  DELETE 

    public void deleteTask(int id) {
        validateId(id);

        boolean deleted = repository.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Task with id " + id + " not found");
        }
    }

    // VALIDATION 

    private void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid task id");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
    }

    private boolean isValidStatus(Task.Status status) {
        for (Task.Status s : Task.Status.values()) {
            if (s == status) return true;
        }
        return false;
    }
}