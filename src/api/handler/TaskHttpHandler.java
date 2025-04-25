package api.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.ManagerSaveException;
import exception.NotFoundException;
import exception.TaskOverlappingException;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;

import java.io.IOException;
import java.util.Optional;

public class TaskHttpHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TaskHttpHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Optional<Integer> taskId = getId(exchange);
        Endpoint endpoint = getEndpoint(exchange);
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);

        switch (endpoint) {
            case GET_TASKS -> {
                if (taskId.isPresent()) {
                    try {
                        sendText(exchange, gson.toJson(taskManager.getTask(taskId.get())));
                    } catch (NotFoundException e) {
                        sendNotFound(exchange, e.getMessage());
                    }
                } else {
                    sendText(exchange, gson.toJson(taskManager.getTasks()));
                }
            }
            case GET_EPICS -> {
                if (taskId.isPresent()) {
                    try {
                        sendText(exchange, gson.toJson(taskManager.getEpic(taskId.get())));
                    } catch (NotFoundException e) {
                        sendNotFound(exchange, e.getMessage());
                    }
                } else {
                    sendText(exchange, gson.toJson(taskManager.getEpics()));
                }
            }
            case GET_SUBTASKS -> {
                if (taskId.isPresent()) {
                    try {
                        sendText(exchange, gson.toJson(taskManager.getSubtask(taskId.get())));
                    } catch (NotFoundException e) {
                        sendNotFound(exchange, e.getMessage());
                    }
                } else {
                    sendText(exchange, gson.toJson(taskManager.getSubtasks()));
                }
            }
            case GET_EPIC_SUBTASKS -> {
                if (taskId.isPresent()) {
                    try {
                        Epic epic = taskManager.getEpic(taskId.get());
                        sendText(exchange, gson.toJson(taskManager.getAllSubtasksByEpic(epic)));
                    } catch (NotFoundException e) {
                        sendNotFound(exchange, e.getMessage());
                    }
                } else {
                    writeResponse(exchange, "Некорректный запрос", 400);
                }
            }
            case GET_HISTORY -> sendText(exchange, gson.toJson(taskManager.getHistory()));
            case GET_PRIORITIZED -> sendText(exchange, gson.toJson(taskManager.getPrioritizedTasks()));
            case POST_TASK -> {
                if (taskId.isPresent()) {
                    try {
                        taskManager.getTask(taskId.get()); //для того что бы выбросился NotFoundException, если Task отсутствует
                        Task updatedTask = gson.fromJson(requestBody, Task.class);
                        taskManager.updateTask(updatedTask);
                        writeResponse(exchange, ("Task с id = " + taskId.get() + " успешно обновлен"), 201);
                    } catch (TaskOverlappingException e) {
                        sendHasInteractions(exchange, e.getMessage());
                    } catch (NotFoundException e) {
                        sendNotFound(exchange, e.getMessage());
                    } catch (ManagerSaveException e) {
                        writeResponse(exchange, e.getMessage(), 500);
                    }
                } else {
                    try {
                        Task newTask = gson.fromJson(requestBody, Task.class);
                        writeResponse(exchange,
                                "Task успешно добавлен, присвоенный id = " + taskManager.addTask(newTask),
                                201);
                    } catch (TaskOverlappingException e) {
                        sendHasInteractions(exchange, e.getMessage());
                    } catch (ManagerSaveException e) {
                        writeResponse(exchange, e.getMessage(), 500);
                    }
                }
            }
            case POST_EPIC -> {
                if (taskId.isPresent()) {
                    try {
                        taskManager.getEpic(taskId.get()); //для того что бы выбросился NotFoundException, если Task отсутствует
                        Epic updatedEpic = gson.fromJson(requestBody, Epic.class);
                        taskManager.updateEpic(updatedEpic);
                        writeResponse(exchange, ("Epic с id = " + taskId.get() + " успешно обновлен"), 201);
                    } catch (TaskOverlappingException e) {
                        sendHasInteractions(exchange, e.getMessage());
                    } catch (NotFoundException e) {
                        sendNotFound(exchange, e.getMessage());
                    } catch (ManagerSaveException e) {
                        writeResponse(exchange, e.getMessage(), 500);
                    }
                } else {
                    try {
                        Epic newEpic = gson.fromJson(requestBody, Epic.class);
                        writeResponse(exchange,
                                "Epic успешно добавлен, присвоенный id = " + taskManager.addEpic(newEpic),
                                201);
                    } catch (TaskOverlappingException e) {
                        sendHasInteractions(exchange, e.getMessage());
                    } catch (ManagerSaveException e) {
                        writeResponse(exchange, e.getMessage(), 500);
                    }
                }
            }
            case POST_SUBTASK -> {
                if (taskId.isPresent()) {
                    try {
                        taskManager.getSubtask(taskId.get()); //для того что бы выбросился NotFoundException, если Task отсутствует
                        Subtask updatedSubtask = gson.fromJson(requestBody, Subtask.class);
                        taskManager.updateSubtask(updatedSubtask);
                        writeResponse(exchange, ("Subtask с id = " + taskId.get() + " успешно обновлен"), 201);
                    } catch (TaskOverlappingException e) {
                        sendHasInteractions(exchange, e.getMessage());
                    } catch (NotFoundException e) {
                        sendNotFound(exchange, e.getMessage());
                    } catch (ManagerSaveException e) {
                        writeResponse(exchange, e.getMessage(), 500);
                    }
                } else {
                    try {
                        Subtask newSubtask = gson.fromJson(requestBody, Subtask.class);
                        writeResponse(exchange,
                                "Subtask успешно добавлен, присвоенный id = " + taskManager.addSubtask(newSubtask),
                                201);
                    } catch (TaskOverlappingException e) {
                        sendHasInteractions(exchange, e.getMessage());
                    } catch (NotFoundException e) {
                        sendNotFound(exchange, e.getMessage());
                    } catch (ManagerSaveException e) {
                        writeResponse(exchange, e.getMessage(), 500);
                    }
                }
            }
            case DELETE_TASKS -> {
                if (taskId.isPresent()) {
                    try {
                        taskManager.removeTask(taskId.get());
                        sendText(exchange, "Task с id = " + taskId.get() + " успешно удален");
                    } catch (ManagerSaveException e) {
                        writeResponse(exchange, e.getMessage(), 500);
                    }
                } else {
                    try {
                        taskManager.clearTasks();
                        sendText(exchange, "Все Task были удалены");
                    } catch (ManagerSaveException e) {
                        writeResponse(exchange, e.getMessage(), 500);
                    }
                }
            }
            case DELETE_EPICS -> {
                if (taskId.isPresent()) {
                    try {
                        taskManager.removeEpic(taskId.get());
                        sendText(exchange, "Epic с id = " + taskId.get() + " успешно удален");
                    } catch (ManagerSaveException e) {
                        writeResponse(exchange, e.getMessage(), 500);
                    }
                } else {
                    try {
                        taskManager.clearEpics();
                        sendText(exchange, "Все Epic были удалены");
                    } catch (ManagerSaveException e) {
                        writeResponse(exchange, e.getMessage(), 500);
                    }
                }
            }
            case DELETE_SUBTASKS -> {
                if (taskId.isPresent()) {
                    try {
                        taskManager.removeSubtask(taskId.get());
                        sendText(exchange, "Subtask с id = " + taskId.get() + " успешно удален");
                    } catch (ManagerSaveException e) {
                        writeResponse(exchange, e.getMessage(), 500);
                    }
                } else {
                    try {
                        taskManager.clearSubtasks();
                        sendText(exchange, "Все Subtask были удалены");
                    } catch (ManagerSaveException e) {
                        writeResponse(exchange, e.getMessage(), 500);
                    }
                }
            }
            default -> writeResponse(exchange,
                    "Сервер пока еще не научился обрабатывать такие запросы",
                    501);
        }
    }

    enum Endpoint {
        GET_TASKS,
        GET_SUBTASKS,
        GET_EPICS,
        GET_EPIC_SUBTASKS,
        GET_HISTORY,
        GET_PRIORITIZED,
        POST_TASK,
        POST_SUBTASK,
        POST_EPIC,
        DELETE_TASKS,
        DELETE_SUBTASKS,
        DELETE_EPICS,
        UNKNOWN
    }

    protected Optional<Integer> getId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (IndexOutOfBoundsException | NumberFormatException exception) {
            return Optional.empty();
        }
    }

    protected Endpoint getEndpoint(HttpExchange exchange) {
        String requestPath = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();
        String[] pathParts = requestPath.split("/");

        if (pathParts.length > 1 && pathParts.length < 4) {
            String type = pathParts[1];
            if (requestMethod.equals("GET")) {
                switch (type.toLowerCase()) {
                    case "tasks" -> {
                        return Endpoint.GET_TASKS;
                    }
                    case "epics" -> {
                        return Endpoint.GET_EPICS;
                    }
                    case "subtasks" -> {
                        return Endpoint.GET_SUBTASKS;
                    }
                    case "history" -> {
                        return Endpoint.GET_HISTORY;
                    }
                    case "prioritized" -> {
                        return Endpoint.GET_PRIORITIZED;
                    }
                }
            } else if (requestMethod.equals("POST")) {
                switch (type.toLowerCase()) {
                    case "tasks" -> {
                        return Endpoint.POST_TASK;
                    }
                    case "epics" -> {
                        return Endpoint.POST_EPIC;
                    }
                    case "subtasks" -> {
                        return Endpoint.POST_SUBTASK;
                    }
                }
            } else if (requestMethod.equals("DELETE")) {
                switch (type.toLowerCase()) {
                    case "tasks" -> {
                        return Endpoint.DELETE_TASKS;
                    }
                    case "epics" -> {
                        return Endpoint.DELETE_EPICS;
                    }
                    case "subtasks" -> {
                        return Endpoint.DELETE_SUBTASKS;
                    }
                }
            }
        }
        if (pathParts.length == 4 && pathParts[1].equals("epics") && pathParts[3].equals("subtasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_EPIC_SUBTASKS;
            }
        }
        return Endpoint.UNKNOWN;
    }
}
