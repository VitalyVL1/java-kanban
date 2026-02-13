package api.server;

import api.adapter.DurationAdapter;
import api.adapter.LocalDateTimeAdapter;
import api.handler.TaskHttpHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * HTTP сервер для управления задачами через REST API.
 *
 * <p>Запускает сервер на порту 8080 и предоставляет доступ к задачам (Task),
 * эпикам (Epic) и подзадачам (Subtask) через HTTP запросы.</p>
 *
 * <p>Доступные контексты:</p>
 * <ul>
 *   <li><b>/tasks</b> - работа с обычными задачами</li>
 *   <li><b>/tasks/id</b> - работа с задачей по идентификатору</li>
 *   <li><b>/subtasks</b> - работа с подзадачами</li>
 *   <li><b>/subtasks/id</b> - работа с подзадачей по идентификатору</li>
 *   <li><b>/epics</b> - работа с эпиками</li>
 *   <li><b>/epics/id</b> - работа с эпиком по идентификатору</li>
 *   <li><b>/epics/id/subtasks</b> - получение подзадач эпика</li>
 *   <li><b>/history</b> - получение истории просмотров</li>
 *   <li><b>/prioritized</b> - получение задач по приоритету</li>
 * </ul>
 *
 * <p>Все запросы обрабатываются через {@link TaskHttpHandler}. Для корректной
 * сериализации/десериализации используются адаптеры:</p>
 * <ul>
 *   <li>{@link DurationAdapter} - преобразование Duration в минуты</li>
 *   <li>{@link LocalDateTimeAdapter} - преобразование даты в формат "dd-MM-yyyy HH:mm:ss.SSS"</li>
 * </ul>
 *
 * <p>Пример запуска:</p>
 * <pre>
 * TaskManager manager = Managers.getDefault();
 * HttpTaskServer server = new HttpTaskServer(manager);
 * server.start();
 * // ... работа с сервером
 * server.stop();
 * </pre>
 *
 * @see TaskHttpHandler
 * @see api.adapter.DurationAdapter
 * @see api.adapter.LocalDateTimeAdapter
 * @see manager.TaskManager
 */
public class HttpTaskServer {

    /** Порт по умолчанию для HTTP сервера */
    private static final Integer PORT = 8080;

    /** Задержка при остановке сервера (в секундах) */
    private static final Integer DEFAULT_DELAY = 1;

    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;

    /**
     * Создает и настраивает HTTP сервер.
     *
     * <p>Инициализирует сервер на порту 8080, настраивает Gson с необходимыми
     * адаптерами и регистрирует все обработчики контекстов.</p>
     *
     * @param taskManager менеджер задач для обработки запросов
     * @throws IOException если не удается создать сервер
     */
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

    /**
     * Точка входа для запуска сервера.
     *
     * <p>Создает экземпляр менеджера через {@link Managers#getDefault()},
     * инициализирует и запускает HTTP сервер.</p>
     *
     * @param args аргументы командной строки (не используются)
     * @throws IOException если не удается создать сервер
     */
    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();
    }

    /**
     * Запускает HTTP сервер.
     */
    public void start() {
        server.start();
    }

    /**
     * Останавливает HTTP сервер.
     *
     * <p>Использует задержку в {@value #DEFAULT_DELAY} секунду для завершения
     * текущих запросов.</p>
     */
    public void stop() {
        server.stop(DEFAULT_DELAY);
    }

    /**
     * Возвращает настроенный экземпляр Gson.
     *
     * @return Gson с зарегистрированными адаптерами для Duration и LocalDateTime
     */
    public Gson getGson() {
        return gson;
    }
}