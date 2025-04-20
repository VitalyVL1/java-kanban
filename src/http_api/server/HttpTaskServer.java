package http_api.server;


import com.sun.net.httpserver.HttpServer;
import http_api.handler.*;
import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final Integer PORT = 8080;
    private static final Integer DEFAULT_DELAY = 1;

    private final HttpServer server;
    private final TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        TaskHttpHandler taskHandler = new TaskHttpHandler(taskManager);

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
        fillTasks(manager);//для проверки
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(DEFAULT_DELAY);
    }

    //для проверки изначальное заполнение
    private static void fillTasks(TaskManager taskManager) {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(15);

        Task task1 = new Task("First Task", "My first task", startTime, duration.plusMinutes(10));
        Task task2 = new Task("Second Task", "My second task", startTime.plusHours(1), duration.plusMinutes(20));
        Task task3 = new Task("Third Task", "My third task", startTime.plusHours(2), duration.plusMinutes(20));
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);

        Epic epic1 = new Epic("Fist Epic", "My first epic");
        Epic epic2 = new Epic("Second Epic", "My second epic");
        Epic epic3 = new Epic("Third Epic", "My third epic");
        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        taskManager.addEpic(epic3);

        Subtask subtask1 = new Subtask("Fist Subtask", "My first subtask", epic1, startTime.minusHours(1), duration.plusMinutes(10));
        Subtask subtask2 = new Subtask("Second Subtask", "My second subtask", epic2, startTime.minusHours(2), duration.plusMinutes(20));
        Subtask subtask3 = new Subtask("Third Subtask", "My third subtask", epic2, startTime.minusHours(3), duration.plusMinutes(20));
        Subtask subtask4 = new Subtask("Fourth Subtask", "My fourth subtask", epic3, startTime.minusHours(4), duration.plusMinutes(25));
        Subtask subtask5 = new Subtask("Fifth Subtask", "My fifth subtask", epic3, startTime.minusHours(5), duration.plusMinutes(15));
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);
        taskManager.addSubtask(subtask4);
        taskManager.addSubtask(subtask5);

        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        taskManager.getTask(task3.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());
        taskManager.getSubtask(subtask3.getId());
        taskManager.getEpic(epic1.getId());
        taskManager.getEpic(epic2.getId());
        taskManager.getEpic(epic3.getId());
    }
}
