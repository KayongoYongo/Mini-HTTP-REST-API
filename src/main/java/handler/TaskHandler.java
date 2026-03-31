package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
        String path = exchange.getRequestURI().getPath(); // e.g. /tasks or /tasks/1

        try {
            if (path.equals("/tasks")) {
                if (method.equals("GET")) handleGetAll(exchange);
                else if (method.equals("POST")) handleCreate(exchange);
                else sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");

            } else if (path.matches("/tasks/\\d+")) {
                int id = extractId(path);
                if (method.equals("GET")) handleGetOne(exchange, id);
                else if (method.equals("PUT")) handleUpdate(exchange, id);
                else if (method.equals("DELETE")) handleDelete(exchange, id);
                else sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");

            } else {
                sendResponse(exchange, 404, "{\"error\":\"Route not found\"}");
            }

        } catch (IllegalArgumentException e) {
            // Validation or not-found errors from the service layer
            String body = "{\"error\":\"" + e.getMessage() + "\"}";
            int status = e.getMessage().contains("not found") ? 404 : 400;
            sendResponse(exchange, status, body);

        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    // GET /tasks
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskService.getAllTasks();
        sendResponse(exchange, 200, gson.toJson(tasks));
    }

    // GET /tasks/{id}
    private void handleGetOne(HttpExchange exchange, int id) throws IOException {
        Task task = taskService.getTaskById(id);
        sendResponse(exchange, 200, gson.toJson(task));
    }

    // POST /tasks
    private void handleCreate(HttpExchange exchange) throws IOException {
        JsonObject body = parseBody(exchange);
        String title = body.has("title") ? body.get("title").getAsString() : null;
        String description = body.has("description") ? body.get("description").getAsString() : null;

        Task task = taskService.createTask(title, description);
        sendResponse(exchange, 201, gson.toJson(task));
    }

    // PUT /tasks/{id}
    private void handleUpdate(HttpExchange exchange, int id) throws IOException {
        JsonObject body = parseBody(exchange);
        String title = body.has("title") ? body.get("title").getAsString() : null;
        String description = body.has("description") ? body.get("description").getAsString() : null;
        Task.Status status = null;

        if (body.has("status")) {
            try {
                status = Task.Status.valueOf(body.get("status").getAsString().toUpperCase());
            } catch (IllegalArgumentException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid status value\"}");
                return;
            }
        }

        Task task = taskService.updateTask(id, title, description, status);
        sendResponse(exchange, 200, gson.toJson(task));
    }

    // DELETE /tasks/{id}
    private void handleDelete(HttpExchange exchange, int id) throws IOException {
        taskService.deleteTask(id);
        sendResponse(exchange, 204, "");
    }

    // --- Helpers ---

    private JsonObject parseBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        return JsonParser.parseString(body).getAsJsonObject();
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, statusCode == 204 ? -1 : bytes.length);

        if (statusCode != 204) {
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.getResponseBody().close();
        }
    }
}