package repository;

import model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskRepository {

    private final ConcurrentHashMap<Integer, Task> store = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    // Save a new task
    public Task save(String title, String description) {
        int id = idCounter.getAndIncrement();
        Task task = new Task(id, title, description);
        store.put(id, task);
        return task;
    }

    // Get a single task by ID
    public Task findById(int id) {
        return store.get(id);
    }

    // Get all tasks
    public List<Task> findAll() {
        return new ArrayList<>(store.values());
    }

    // Update an existing task
    public Task update(int id, String title, String description, Task.Status status) {
        Task task = store.get(id);
        if (task == null) return null;

        if (title != null) task.setTitle(title);
        if (description != null) task.setDescription(description);
        if (status != null) task.setStatus(status);

        return task;
    }

    // Delete a task
    public boolean delete(int id) {
        return store.remove(id) != null;
    }
}