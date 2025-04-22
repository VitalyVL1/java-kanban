package http_api;

import exception.NotFoundException;
import http_api.type_token.EpicListTypeToken;
import http_api.type_token.SubtaskListTypeToken;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static manager.TaskManagerTest.equalTasks;
import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerEpicTest extends HttpTaskManagerTest {
    private final static String BASIC_URL = "http://localhost:8080/epics";

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic1", "Description Epic1");

        String taskJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Возвращен некорректный код ответа для добавляемого эпика");

        List<Epic> epicsFromManager = manager.getEpics();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Epic1", epicsFromManager.getFirst().getTitle(), "Некорректное имя эпика");
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {
        fillTasks(manager);

        Epic updatedEpic = manager.getEpics().getFirst();
        int actualSize = manager.getEpics().size();
        String newTitle = "Updated Title";
        String newDescription = "Updated Description";

        updatedEpic.setTitle(newTitle);
        updatedEpic.setDescription(newDescription);

        //Обычное обновление
        String taskJson = gson.toJson(updatedEpic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL + "/" + updatedEpic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(actualSize, epicsFromManager.size(), "Некорректное количество эпиков");
        assertTrue(equalTasks(updatedEpic, epicsFromManager.getFirst()), "Эпик не обновился");

        // проверка попытки обновить отсутствующую задачу
        Epic notFoundEpic = manager.getEpics().getFirst();
        notFoundEpic.setId(0);
        taskJson = gson.toJson(notFoundEpic);

        url = URI.create(BASIC_URL + "/" + notFoundEpic.getId());
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Возвращен некорректный код ответа для не найденного эпика");

        epicsFromManager = manager.getEpics();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(actualSize, epicsFromManager.size(), "Некорректное количество эпиков");
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        fillTasks(manager);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код успешного ответа");

        List<Epic> epicsFromManager = manager.getEpics();
        List<Epic> epicsFromHttp = gson.fromJson(response.body(), new EpicListTypeToken().getType());

        for (int i = 0; i < epicsFromManager.size(); i++) {
            assertTrue(equalTasks(epicsFromManager.get(i), epicsFromHttp.get(i)), "Полученные эпики не равны");
        }

        assertEquals(epicsFromManager.size(), epicsFromHttp.size(), "Размер полученного и хранимого списков не равны");
    }

    @Test
    public void testGetEpicFromEmptyList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный код успешного ответа");

        List<Epic> epicsFromManager = manager.getEpics();
        List<Epic> epicsFromHttp = gson.fromJson(response.body(), new EpicListTypeToken().getType());

        assertEquals(0, epicsFromHttp.size(), "Полученный ответ не пустой");
        assertEquals(epicsFromManager.size(), epicsFromHttp.size(), "Размер полученного и хранимого списков не равны");
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        final int id = 4;

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

        Epic expectedEpic = manager.getEpic(id);
        Epic actualEpic = gson.fromJson(response.body(), Epic.class);

        assertTrue(equalTasks(expectedEpic, actualEpic), "Хранимый и полученный эпики не равны");
    }

    @Test
    public void testGetSubtasksByEpic() throws IOException, InterruptedException {
        fillTasks(manager);
        Epic epic = manager.getEpics().get(1);
        List<Subtask> expectedSubtasks = manager.getAllSubtasksByEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL + "/" + epic.getId() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<Subtask> actualSubtasks = gson.fromJson(response.body(), new SubtaskListTypeToken().getType());

        assertEquals(expectedSubtasks.size(), actualSubtasks.size(), "Размеры списков полученных подзадач не равны");

        for (int i = 0; i < expectedSubtasks.size(); i++) {
            assertTrue(equalTasks(expectedSubtasks.get(i), actualSubtasks.get(i)), "Полученные подзадачи не равны");
        }
    }

    @Test
    public void testDeleteEpics() throws IOException, InterruptedException {
        fillTasks(manager);

        final int beforeDeleteSize = manager.getEpics().size();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertNotEquals(0, beforeDeleteSize, "В менеджере отсутствуют эпики");

        final int afterDeleteSize = manager.getEpics().size();

        assertEquals(0, afterDeleteSize, "Эпики не удалены");

        List<Subtask> subtasks = manager.getSubtasks();

        assertEquals(0, subtasks.size(), "Подзадачи не удалены после удаления эпиков");
    }

    @Test
    public void testDeleteEpicById() throws IOException, InterruptedException {
        fillTasks(manager);

        final int beforeDeleteSize = manager.getEpics().size();
        final int epicId = manager.getEpics().getFirst().getId();
        final Set<Integer> subtasksId = manager.getEpic(epicId).getSubtasksId();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASIC_URL + "/" + epicId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertNotEquals(0, beforeDeleteSize, "В менеджере отсутствуют эпики");

        final int afterDeleteSize = manager.getEpics().size();

        assertEquals(beforeDeleteSize - 1, afterDeleteSize, "Эпик не удален");

        List<Integer> subtasksIdManager = manager.getSubtasks().stream()
                .map(Task::getId)
                .toList();

        assertTrue(Collections.disjoint(subtasksIdManager, subtasksId), "Подзадачи не были удалены после удаления эпика");
        assertThrows(NotFoundException.class, () -> manager.getSubtask(epicId), "Подзадача не была удалена");
    }
}
