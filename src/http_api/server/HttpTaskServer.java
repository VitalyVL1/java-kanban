package http_api.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import http_api.adapter.DurationAdapter;
import http_api.adapter.LocalDateTimeAdapter;
import http_api.handler.*;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final Integer PORT = 8080;
    private static final Integer DEFAULT_DELAY = 1;

    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        this.gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        TaskHttpHandler taskHandler = new TaskHttpHandler(taskManager, gson);

        server.createContext("/tasks", taskHandler);
        server.createContext("/tasks/id", taskHandler);
        server.createContext("/subtasks", taskHandler);
        server.createContext("/subtasks/id", taskHandler);
        server.createContext("/epics", taskHandler);
        server.createContext("/epics/id", taskHandler);
        server.createContext("/epics/id/subtasks", taskHandler);
        server.createContext("/history", taskHandler);
        server.createContext("/prioritized", taskHandler);

    }

    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(DEFAULT_DELAY);
    }

    public Gson getGson() {
        return gson;
    }
}
