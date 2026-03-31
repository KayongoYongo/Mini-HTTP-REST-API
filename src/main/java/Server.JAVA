import com.sun.net.httpserver.HttpServer;
import handler.TaskHandler;
import repository.TaskRepository;
import service.TaskService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Server {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {

        // Wire up the layers
        TaskRepository repository = new TaskRepository();
        TaskService service = new TaskService(repository);
        TaskHandler handler = new TaskHandler(service);

        // Create and configure the server
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", handler);
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("Server started on http://localhost:" + PORT);
    }
}