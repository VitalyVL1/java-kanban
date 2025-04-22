package api;

import api.type_token.TaskListTypeToken;
import model.Task;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static manager.TaskManagerTest.equalTasks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpPrioritizedTaskTest extends HttpTaskManagerTest {
    private final static String BASIC_URL = "http://localhost:8080/prioritized";

    @Test
    public void testGetPrioritized() throws IOException, InterruptedException {
        Task task1 = new Task("First Task", "My first task", startTime, duration.plusMinutes(10));//id=1
        Task task2 = new Task("Second Task", "My second task", startTime.plusHours(1), duration.plusMinutes(20));//id=2
        Task task3 = new Task("Third Task", "My third task", startTime.minusHours(1), duration.plusMinutes(20));//id=3
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код успешного ответа");

        List<Task> prioritizedFromManager = manager.getPrioritizedTasks();
        List<Task> prioritizedFromHttp = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        assertEquals(prioritizedFromManager.size(), prioritizedFromHttp.size(), "Размер полученного и хранимого списков не равны");

        for (int i = 0; i < prioritizedFromManager.size(); i++) {
            assertTrue(equalTasks(prioritizedFromManager.get(i), prioritizedFromHttp.get(i)), "Полученные задачи не равны");
        }
    }
}
