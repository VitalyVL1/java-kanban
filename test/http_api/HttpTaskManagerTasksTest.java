package http_api;

import exception.NotFoundException;
import http_api.type_token.TaskListTypeToken;
import model.Task;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static manager.TaskManagerTest.equalTasks;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerTasksTest extends HttpTaskManagerTest {
    private final static String BASIC_URL = "http://localhost:8080/tasks";

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2", startTime, duration);
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Возвращен некорректный код ответа для добавляемой задачи");

        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.getFirst().getTitle(), "Некорректное имя задачи");

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Возвращен некорректный код ответа для пересекающейся задачи");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        fillTasks(manager);

        Task updatedTask = manager.getTasks().getFirst();
        int actualSize = manager.getTasks().size();
        String newTitle = "Updated Title";
        String newDescription = "Updated Description";
        LocalDateTime newStartTime = updatedTask.getStartTime().minusDays(1);
        Duration newDuration = updatedTask.getDuration().plusMinutes(5);

        updatedTask.setTitle(newTitle);
        updatedTask.setDescription(newDescription);
        updatedTask.setStartTime(newStartTime);
        updatedTask.setDuration(newDuration);

        //Обычное обновление
        String taskJson = gson.toJson(updatedTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL + "/" + updatedTask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(actualSize, tasksFromManager.size(), "Некорректное количество задач");
        assertTrue(equalTasks(updatedTask, tasksFromManager.getFirst()), "Задача не обновилась");

        // проверка пересекающегося времени
        Task intersectionTask = manager.getTasks().get(0);
        LocalDateTime intersectionStartTime = manager.getTasks().get(1).getStartTime();

        intersectionTask.setStartTime(intersectionStartTime);
        taskJson = gson.toJson(intersectionTask);

        url = URI.create(BASIC_URL + "/" + intersectionTask.getId());
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Возвращен некорректный код ответа для пересекающейся задачи");

        tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(actualSize, tasksFromManager.size(), "Некорректное количество задач");
        assertTrue(equalTasks(updatedTask, tasksFromManager.getFirst()), "Задача не должна была обновиться");

        // проверка попытки обновить отсутствующую задачу
        Task notFoundTask = manager.getTasks().getFirst();
        notFoundTask.setId(0);
        taskJson = gson.toJson(notFoundTask);

        url = URI.create(BASIC_URL + "/" + notFoundTask.getId());
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Возвращен некорректный код ответа для не найденной задачи");

        tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(actualSize, tasksFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        fillTasks(manager);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код успешного ответа");

        List<Task> tasksFromManager = manager.getTasks();
        List<Task> taskFromHttp = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        for (int i = 0; i < tasksFromManager.size(); i++) {
            assertTrue(equalTasks(tasksFromManager.get(i), taskFromHttp.get(i)), "Полученные задачи не равны");
        }

        assertEquals(tasksFromManager.size(), taskFromHttp.size(), "Размер полученного и хранимого списков не равны");
    }

    @Test
    public void testGetTasksFromEmptyList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код успешного ответа");

        List<Task> tasksFromManager = manager.getTasks();
        List<Task> taskFromHttp = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        assertEquals(0, taskFromHttp.size(), "Полученный ответ не пустой");
        assertEquals(tasksFromManager.size(), taskFromHttp.size(), "Размер полученного и хранимого списков не равны");
    }

    @Test
    public void testGetTasksById() throws IOException, InterruptedException {
        final int id = 1;

        //проверяем отсутствующий ID
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL + "/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Некорректный код ответа");

        //проверяем действующий ID
        fillTasks(manager);

        url = URI.create(BASIC_URL + "/" + id);
        request = HttpRequest.newBuilder().uri(url).GET().build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код ответа");

        Task expectedTask = manager.getTask(id);
        Task actualTask = gson.fromJson(response.body(), Task.class);

        assertTrue(equalTasks(expectedTask, actualTask), "Хранимая и полученная задачи не равны");
    }

    @Test
    public void testDeleteTasks() throws IOException, InterruptedException {
        fillTasks(manager);

        final int beforeDeleteSize = manager.getTasks().size();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertNotEquals(0, beforeDeleteSize, "В менеджере отсутствуют задачи");

        final int afterDeleteSize = manager.getTasks().size();

        assertEquals(0, afterDeleteSize, "Задачи не удалены");
    }

    @Test
    public void testDeleteTasksById() throws IOException, InterruptedException {
        fillTasks(manager);

        final int beforeDeleteSize = manager.getTasks().size();
        final int id = manager.getTasks().getFirst().getId();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL + "/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertNotEquals(0, beforeDeleteSize, "В менеджере отсутствуют задачи");

        final int afterDeleteSize = manager.getTasks().size();

        assertEquals(beforeDeleteSize - 1, afterDeleteSize, "Задачи не удалены");
        assertThrows(NotFoundException.class, () -> manager.getTask(id), "Задача не была удалена");
    }
}
