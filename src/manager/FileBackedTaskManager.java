package manager;

import exception.ManagerSaveException;
import model.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(HistoryManager historyManager, String pathName) {
        super(historyManager);
        this.file = new File(pathName);
    }

    private static String toStringLine(Task task) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%d,%s,%s,%s,%s,", task.getId(),
                task.getType(),
                task.getTitle(),
                task.getStatus(),
                task.getDescription()));

        if (task instanceof Subtask) {
            sb.append(((Subtask) task).getEpic().getId());
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
        Set<Task> allTasks = new HashSet<>();
        allTasks.addAll(this.getTasks());
        allTasks.addAll(this.getSubtasks());
        allTasks.addAll(this.getEpics());

        try (FileWriter writer = new FileWriter(file)) {
            if (!file.exists()) {
                file.createNewFile();
            }

            writer.write("id,type,name,status,description,epic\n");

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

                String[] split = line.split("\s*,\s*", 6);

                id = Integer.parseInt(split[0]);
                type = TaskType.valueOf(split[1]);
                title = split[2];
                status = TaskStatus.valueOf(split[3]);
                description = split[4];
                epicId = !split[5].isBlank() ? Integer.parseInt(split[5]) : null;

                switch (type) {
                    case TASK -> taskManager.addTask(new Task(id, title, description, status, type));
                    case SUBTASK -> {
                        taskManager.addSubtask(new Subtask(id, title, description, status, type, new Epic("", "")));
                        epicIdBySubtaskId.put(id, epicId);
                    }
                    case EPIC -> taskManager.addEpic(new Epic(id, title, description, status, type, new HashMap<>()));
                }
            }

            for (Map.Entry<Integer, Integer> entry : epicIdBySubtaskId.entrySet()) {
                Subtask subtask = taskManager.getSubtask(entry.getKey());
                Epic epic = taskManager.getEpic(entry.getValue());

                subtask.setEpic(epic);
                epic.addOrUpdateSubtask(subtask);

                taskManager.updateEpic(epic);
                taskManager.updateSubtask(subtask);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения из файла");
        }
        return taskManager;
    }
}
