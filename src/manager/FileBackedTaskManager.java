package manager;

import exception.ManagerSaveException;
import model.*;

import java.io.*;
import java.nio.file.Files;
import java.time.*;
import java.util.*;

/**
 * Реализация менеджера задач с автоматическим сохранением состояния в файл.
 *
 * <p>Наследует функциональность {@link InMemoryTaskManager} и добавляет
 * автоматическое сохранение всех изменений в файл. При каждом изменении
 * данных (добавление, обновление, удаление) вызывается метод {@link #save()},
 * который записывает актуальное состояние всех задач в файл.</p>
 *
 * <p>Формат файла:</p>
 * <ul>
 *   <li>CSV файл с заголовком: {@value #HEADER}</li>
 *   <li>Каждая задача записывается с новой строки</li>
 *   <li>Поля разделяются запятыми</li>
 *   <li>Время хранится в миллисекундах (Unix timestamp)</li>
 *   <li>Длительность хранится в миллисекундах</li>
 * </ul>
 *
 * <p>Пример содержимого файла:</p>
 * <pre>
 * id,type,name,status,description,epic,startTime,duration
 * 1,TASK,Задача 1,NEW,Описание задачи,,1640995200000,3600000
 * 2,EPIC,Эпик 1,NEW,Описание эпика,,,
 * 3,SUBTASK,Подзадача 1,NEW,Описание,,1641081600000,1800000,2
 * </pre>
 *
 * <p>При создании через {@link #loadFromFile(File)} автоматически восстанавливает
 * состояние из указанного файла, если он существует.</p>
 *
 * @see InMemoryTaskManager
 * @see ManagerSaveException
 */
public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    /** Заголовок CSV файла с описанием колонок */
    private static final String HEADER = "id,type,name,status,description,epic,startTime,duration";

    /**
     * Создает менеджер с файловым сохранением.
     *
     * @param historyManager менеджер истории просмотров
     * @param pathName путь к файлу для сохранения данных
     */
    public FileBackedTaskManager(HistoryManager historyManager, String pathName) {
        super(historyManager);
        this.file = new File(pathName);
    }

    /**
     * Преобразует задачу в строку CSV формата.
     *
     * <p>Для подзадач дополнительно добавляется ID эпика в конце строки.
     * Пустые значения времени заменяются на 0.</p>
     *
     * @param task задача для преобразования
     * @return строка в CSV формате
     */
    private static String toStringLine(Task task) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%d,%s,%s,%s,%s,%d,%d,", task.getId(),
                task.getType(),
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                task.getStartTime() == null ? 0 :
                        task.getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli(),
                task.getDuration().toMillis()));

        if (task instanceof Subtask) {
            sb.append(((Subtask) task).getEpicId());
        }

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     *
     * <p>После добавления эпика автоматически сохраняет состояние в файл.</p>
     */
    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        save();
        return id;
    }

    /**
     * {@inheritDoc}
     *
     * <p>После добавления задачи автоматически сохраняет состояние в файл.</p>
     */
    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);
        save();
        return id;
    }

    /**
     * {@inheritDoc}
     *
     * <p>После добавления подзадачи автоматически сохраняет состояние в файл.</p>
     */
    @Override
    public int addSubtask(Subtask subtask) {
        int id = super.addSubtask(subtask);
        save();
        return id;
    }

    /**
     * {@inheritDoc}
     *
     * <p>После удаления эпика автоматически сохраняет состояние в файл.</p>
     */
    @Override
    public Epic removeEpic(int id) {
        Epic epic = super.removeEpic(id);
        save();
        return epic;
    }

    /**
     * {@inheritDoc}
     *
     * <p>После удаления задачи автоматически сохраняет состояние в файл.</p>
     */
    @Override
    public Task removeTask(int id) {
        Task task = super.removeTask(id);
        save();
        return task;
    }

    /**
     * {@inheritDoc}
     *
     * <p>После удаления подзадачи автоматически сохраняет состояние в файл.</p>
     */
    @Override
    public Subtask removeSubtask(int id) {
        Subtask subtask = super.removeSubtask(id);
        save();
        return subtask;
    }

    /**
     * {@inheritDoc}
     *
     * <p>После очистки эпиков автоматически сохраняет состояние в файл.</p>
     */
    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    /**
     * {@inheritDoc}
     *
     * <p>После очистки задач автоматически сохраняет состояние в файл.</p>
     */
    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    /**
     * {@inheritDoc}
     *
     * <p>После очистки подзадач автоматически сохраняет состояние в файл.</p>
     */
    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    /**
     * {@inheritDoc}
     *
     * <p>После обновления эпика автоматически сохраняет состояние в файл.</p>
     */
    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    /**
     * {@inheritDoc}
     *
     * <p>После обновления задачи автоматически сохраняет состояние в файл.</p>
     */
    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    /**
     * {@inheritDoc}
     *
     * <p>После обновления подзадачи автоматически сохраняет состояние в файл.</p>
     */
    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    /**
     * Сохраняет текущее состояние всех задач в файл.
     *
     * <p>Собирает все задачи (Task, Epic, Subtask) из соответствующих коллекций,
     * объединяет их и записывает в файл в CSV формате. При возникновении ошибок
     * ввода-вывода выбрасывает {@link ManagerSaveException}.</p>
     *
     * @throws ManagerSaveException если произошла ошибка при записи в файл
     */
    public void save() {
        Set<Task> allTasks = new LinkedHashSet<>();
        allTasks.addAll(this.getTasks());
        allTasks.addAll(this.getEpics());
        allTasks.addAll(this.getSubtasks());

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(HEADER + "\n");

            for (Task task : allTasks) {
                writer.write(toStringLine(task) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл");
        }
    }

    /**
     * Загружает состояние менеджера из файла.
     *
     * <p>Создает новый экземпляр {@link FileBackedTaskManager} и восстанавливает
     * его состояние из указанного файла. Если файл не существует, создает пустой файл.</p>
     *
     * <p>Процесс загрузки:</p>
     * <ol>
     *   <li>Чтение всех строк из файла</li>
     *   <li>Пропуск заголовка</li>
     *   <li>Парсинг каждой строки в соответствующий тип задачи</li>
     *   <li>Добавление задач в менеджер</li>
     *   <li>Восстановление связей между эпиками и подзадачами</li>
     * </ol>
     *
     * @param file файл для загрузки данных
     * @return восстановленный менеджер задач
     * @throws ManagerSaveException если произошла ошибка при чтении из файла
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager taskManager = new FileBackedTaskManager(new InMemoryHistoryManager(), file.getPath());

        Integer id;
        TaskType type;
        String title;
        TaskStatus status;
        String description;
        Integer epicId;
        LocalDateTime startTime;
        Duration duration;

        try {
            if (!file.exists()) {
                System.out.println("По указанному пути файл отсутствует!");
                file.createNewFile();
                return taskManager;
            }

            List<String> lines = Files.readAllLines(file.toPath());

            Map<Integer, Integer> epicIdBySubtaskId = new HashMap<>();

            for (String line : lines) {
                if (line.isBlank()) break;
                if (line.startsWith("id")) continue;

                String[] split = line.split("\s*,\s*", 8);

                id = Integer.parseInt(split[0]);
                type = TaskType.valueOf(split[1]);
                title = split[2];
                status = TaskStatus.valueOf(split[3]);
                description = split[4];
                startTime = Long.parseLong(split[5]) == 0 ? null :
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(split[5])), ZoneOffset.UTC);
                duration = Duration.ofMillis(Long.parseLong(split[6]));
                epicId = !split[7].isBlank() ? Integer.parseInt(split[7]) : null;

                switch (type) {
                    case TASK ->
                            taskManager.addTask(new Task(id, title, description, status, type, startTime, duration));
                    case SUBTASK -> {
                        if (epicId != null) {
                            taskManager.addSubtask(new Subtask(id, title, description, status, type, taskManager.getEpic(epicId), startTime, duration));
                            epicIdBySubtaskId.put(id, epicId);
                        } // Subtask должен быть всегда привязан к Epic, проверку поставил что бы избежать NullPointerException, некорректные Subtask будут пропущены
                    }
                    case EPIC ->
                            taskManager.addEpic(new Epic(id, title, description, status, type, new HashSet<>(), startTime, duration));
                }
            }

            for (Map.Entry<Integer, Integer> entry : epicIdBySubtaskId.entrySet()) {
                Subtask subtask = taskManager.getSubtask(entry.getKey());
                Epic epic = taskManager.getEpic(entry.getValue());
                epic.addOrUpdateSubtask(subtask);
                taskManager.updateEpic(epic);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения из файла");
        }
        return taskManager;
    }
}