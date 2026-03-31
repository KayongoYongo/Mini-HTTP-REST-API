package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import model.Task;
import service.TaskService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler implements HttpHandler {

    private final TaskService taskService;
    private final Gson gson = new Gson();

    public TaskHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (path.equals("/tasks")) {
                switch (method) {
                    case "GET":
                        handleGetAll(exchange);
                        break;
                    case "POST":
                        handleCreate(exchange);
                        break;
                    default:
                        sendError(exchange, 405, "Method not allowed");
                }

            } else if (path.matches("/tasks/\\d+")) {
                int id = extractId(path);

                switch (method) {
                    case "GET":
                        handleGetOne(exchange, id);
                        break;
                    case "PUT":
                        handleUpdate(exchange, id);
                        break;
                    case "DELETE":
                        handleDelete(exchange, id);
                        break;
                    default:
                        sendError(exchange, 405, "Method not allowed");
                }

            } else {
                sendError(exchange, 404, "Route not found");
            }

        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, e.getMessage());

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 VERY IMPORTANT FOR DEBUGGING
            sendError(exchange, 500, "Internal server error");
        }
    }

    // ------------------ HANDLERS ------------------

    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskService.getAllTasks();
        sendJson(exchange, 200, tasks);
    }

    private void handleGetOne(HttpExchange exchange, int id) throws IOException {
        Task task = taskService.getTaskById(id);
        if (task == null) {
            throw new IllegalArgumentException("Task not found");
        }
        sendJson(exchange, 200, task);
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        JsonObject body = parseBody(exchange);

        String title = getAsString(body, "title");
        String description = getAsString(body, "description");

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        Task task = taskService.createTask(title, description);
        sendJson(exchange, 201, task);
    }

    private void handleUpdate(HttpExchange exchange, int id) throws IOException {
        JsonObject body = parseBody(exchange);

        String title = getAsString(body, "title");
        String description = getAsString(body, "description");
        Task.Status status = null;

        if (body.has("status")) {
            try {
                status = Task.Status.valueOf(body.get("status").getAsString().toUpperCase());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid status value");
            }
        }

        Task task = taskService.updateTask(id, title, description, status);
        sendJson(exchange, 200, task);
    }

    private void handleDelete(HttpExchange exchange, int id) throws IOException {
        taskService.deleteTask(id);
        exchange.sendResponseHeaders(204, -1);
    }

    // ------------------ HELPERS ------------------

    private JsonObject parseBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Request body is empty");
        }

        try {
            return JsonParser.parseString(body).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid JSON format");
        }
    }

    private String getAsString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull()
                ? obj.get(key).getAsString()
                : null;
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String response = gson.toJson(data);
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);

        byte[] bytes = gson.toJson(error).getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}