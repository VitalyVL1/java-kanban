package manager;

import exception.ManagerSaveException;
import model.*;

import java.io.*;
import java.nio.file.Files;
import java.time.*;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final String HEADER = "id,type,name,status,description,epic,startTime,duration";

    public FileBackedTaskManager(HistoryManager historyManager, String pathName) {
        super(historyManager);
        this.file = new File(pathName);
    }

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

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        save();
        return id;
    }

    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);
        save();
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int id = super.addSubtask(subtask);
        save();
        return id;
    }

    @Override
    public Epic removeEpic(int id) {
        Epic epic = super.removeEpic(id);
        save();
        return epic;
    }

    @Override
    public Task removeTask(int id) {
        Task task = super.removeTask(id);
        save();
        return task;
    }

    @Override
    public Subtask removeSubtask(int id) {
        Subtask subtask = super.removeSubtask(id);
        save();
        return subtask;
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

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
