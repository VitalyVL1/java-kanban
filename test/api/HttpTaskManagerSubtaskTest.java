package api;

import exception.NotFoundException;
import api.type_token.SubtaskListTypeToken;
import model.Epic;
import model.Subtask;
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

public class HttpTaskManagerSubtaskTest extends HttpTaskManagerTest {
    private final static String BASIC_URL = "http://localhost:8080/subtasks";

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic1", "Description Epic1");
        epic.setId(1);

        Subtask subtask = new Subtask("Subtask1", "Description Subtask1", epic, startTime.plusHours(2), duration.plusMinutes(5));
        manager.addEpic(epic);

        String taskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Возвращен некорректный код ответа для добавляемой подзадачи");

        List<Subtask> subtasksFromManager = manager.getSubtasks();

        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Subtask1", subtasksFromManager.getFirst().getTitle(), "Некорректное имя подзадачи");

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Возвращен некорректный код ответа для пересекающейся подзадачи");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        fillTasks(manager);

        Subtask updatedSubtask = manager.getSubtasks().getFirst();
        int actualSize = manager.getSubtasks().size();
        String newTitle = "Updated Title";
        String newDescription = "Updated Description";
        LocalDateTime newStartTime = updatedSubtask.getStartTime().minusDays(1);
        Duration newDuration = updatedSubtask.getDuration().plusMinutes(5);

        updatedSubtask.setTitle(newTitle);
        updatedSubtask.setDescription(newDescription);
        updatedSubtask.setStartTime(newStartTime);
        updatedSubtask.setDuration(newDuration);

        //Обычное обновление
        String taskJson = gson.toJson(updatedSubtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL + "/" + updatedSubtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getSubtasks();

        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(actualSize, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertTrue(equalTasks(updatedSubtask, subtasksFromManager.getFirst()), "Подзадача не обновилась");

        // проверка пересекающегося времени
        Subtask intersectionSubtask = manager.getSubtasks().get(0);
        LocalDateTime intersectionStartTime = manager.getSubtasks().get(1).getStartTime();

        intersectionSubtask.setStartTime(intersectionStartTime);
        taskJson = gson.toJson(intersectionSubtask);

        url = URI.create(BASIC_URL + "/" + intersectionSubtask.getId());
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Возвращен некорректный код ответа для пересекающейся задачи");

        subtasksFromManager = manager.getSubtasks();

        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(actualSize, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertTrue(equalTasks(updatedSubtask, subtasksFromManager.getFirst()), "Подзадача не должна была обновиться");

        // проверка попытки обновить отсутствующую задачу
        Subtask notFoundSubtask = manager.getSubtasks().getFirst();
        notFoundSubtask.setId(0);
        taskJson = gson.toJson(notFoundSubtask);


        url = URI.create(BASIC_URL + "/" + notFoundSubtask.getId());
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Возвращен некорректный код ответа для не найденной подзадачи");

        subtasksFromManager = manager.getSubtasks();

        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(actualSize, subtasksFromManager.size(), "Некорректное количество подзадач");
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        fillTasks(manager);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код успешного ответа");

        List<Subtask> subtasksFromManager = manager.getSubtasks();
        List<Subtask> subtasksFromHttp = gson.fromJson(response.body(), new SubtaskListTypeToken().getType());

        for (int i = 0; i < subtasksFromManager.size(); i++) {
            assertTrue(equalTasks(subtasksFromManager.get(i), subtasksFromHttp.get(i)), "Полученные подзадачи не равны");
        }

        assertEquals(subtasksFromManager.size(), subtasksFromHttp.size(), "Размер полученного и хранимого списков не равны");
    }

    @Test
    public void testGetSubtasksFromEmptyList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код успешного ответа");

        List<Subtask> subtasksFromManager = manager.getSubtasks();
        List<Subtask> subtaskFromHttp = gson.fromJson(response.body(), new SubtaskListTypeToken().getType());

        assertEquals(0, subtaskFromHttp.size(), "Полученный ответ не пустой");
        assertEquals(subtasksFromManager.size(), subtaskFromHttp.size(), "Размер полученного и хранимого списков не равны");
    }

    @Test
    public void testGetSubtasksById() throws IOException, InterruptedException {
        final int id = 7;

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

        Subtask expectedSubtask = manager.getSubtask(id);
        Subtask actualSubtask = gson.fromJson(response.body(), Subtask.class);

        assertTrue(equalTasks(expectedSubtask, actualSubtask), "Хранимая и полученная подзадачи не равны");
    }

    @Test
    public void testDeleteSubtasks() throws IOException, InterruptedException {
        fillTasks(manager);

        final int beforeDeleteSize = manager.getSubtasks().size();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertNotEquals(0, beforeDeleteSize, "В менеджере отсутствуют подзадачи");

        final int afterDeleteSize = manager.getSubtasks().size();

        assertEquals(0, afterDeleteSize, "Подзадачи не удалены");

        List<Epic> epics = manager.getEpics();

        for (Epic epic : epics) {
            assertEquals(0, epic.getSubtasksId().size(), "Подзадачи не удалены из эпика");
        }
    }

    @Test
    public void testDeleteSubtasksById() throws IOException, InterruptedException {
        fillTasks(manager);

        final int beforeDeleteSize = manager.getSubtasks().size();
        final int subtaskId = manager.getSubtasks().getFirst().getId();
        final int epicId = manager.getSubtask(subtaskId).getEpicId();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL + "/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertNotEquals(0, beforeDeleteSize, "В менеджере отсутствуют подзадачи");

        final int afterDeleteSize = manager.getSubtasks().size();

        assertEquals(beforeDeleteSize - 1, afterDeleteSize, "Подзадачи не удалены");
        assertFalse(manager.getEpic(epicId).getSubtasksId().contains(subtaskId), "Подзадача не была удалена из эпика");
        assertThrows(NotFoundException.class, () -> manager.getSubtask(subtaskId), "Подзадача не была удалена");
    }
}
