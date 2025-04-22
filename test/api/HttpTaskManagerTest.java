package api;

import com.google.gson.Gson;
import api.server.HttpTaskServer;
import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public abstract class HttpTaskManagerTest {
    protected TaskManager manager;
    protected HttpTaskServer taskServer;
    protected Gson gson;
    protected final static LocalDateTime startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Instant.now().toEpochMilli()), ZoneOffset.UTC);
    protected final static Duration duration = Duration.ofMinutes(5);

    @BeforeEach
    public void setUp() throws IOException {
        manager = Managers.getFileBackedTaskManager(File.createTempFile("httpTest", ".txt").getAbsolutePath());
        taskServer = new HttpTaskServer(manager);
        gson = taskServer.getGson();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    protected static void fillTasks(TaskManager taskManager) {
        Task task1 = new Task("First Task", "My first task", startTime, duration.plusMinutes(10));//id=1
        Task task2 = new Task("Second Task", "My second task", startTime.plusHours(1), duration.plusMinutes(20));//id=2
        Task task3 = new Task("Third Task", "My third task", startTime.plusHours(2), duration.plusMinutes(20));//id=3
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);

        Epic epic1 = new Epic("Fist Epic", "My first epic");//id=4
        Epic epic2 = new Epic("Second Epic", "My second epic");//id=5
        Epic epic3 = new Epic("Third Epic", "My third epic");//id=6
        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        taskManager.addEpic(epic3);

        Subtask subtask1 = new Subtask("Fist Subtask", "My first subtask", epic1, startTime.minusHours(1), duration.plusMinutes(10));//id=7
        Subtask subtask2 = new Subtask("Second Subtask", "My second subtask", epic2, startTime.minusHours(2), duration.plusMinutes(20));//id=8
        Subtask subtask3 = new Subtask("Third Subtask", "My third subtask", epic2, startTime.minusHours(3), duration.plusMinutes(20));//id=9
        Subtask subtask4 = new Subtask("Fourth Subtask", "My fourth subtask", epic3, startTime.minusHours(4), duration.plusMinutes(25));//id=10
        Subtask subtask5 = new Subtask("Fifth Subtask", "My fifth subtask", epic3, startTime.minusHours(5), duration.plusMinutes(15));//id=11
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
