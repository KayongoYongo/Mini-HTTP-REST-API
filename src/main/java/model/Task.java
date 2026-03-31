package model;

import java.time.LocalDateTime;

public class Task {

    private int id;
    private String title;
    private String description;
    private Status status;
    private LocalDateTime createdAt;

    public enum Status {
        PENDING,
        IN_PROGRESS,
        DONE
    }

    // Constructor
    public Task(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = Status.PENDING; // default status
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters (only fields that can be updated)
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(Status status) { this.status = status; }
}