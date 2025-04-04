package manager;

import exception.ManagerSaveException;
import model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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
                task.getStartTime() == LocalDateTime.MIN ? 0 :
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
        Set<Task> allTasks = new HashSet<>();
        allTasks.addAll(this.getTasks());
        allTasks.addAll(this.getSubtasks());
        allTasks.addAll(this.getEpics());

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
                startTime = Long.parseLong(split[5]) == 0 ? LocalDateTime.MIN :
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(split[5])), ZoneOffset.UTC);
                duration = Duration.ofMillis(Long.parseLong(split[6]));
                epicId = !split[7].isBlank() ? Integer.parseInt(split[7]) : null;

                switch (type) {
                    case TASK ->
                            taskManager.addTask(new Task(id, title, description, status, type, startTime, duration));
                    case SUBTASK -> {
                        taskManager.addSubtask(new Subtask(id, title, description, status, type, new Epic("", ""), startTime, duration));
                        epicIdBySubtaskId.put(id, epicId);
                    }
                    case EPIC ->
                            taskManager.addEpic(new Epic(id, title, description, status, type, new HashSet<>(), startTime, duration));
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

    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault();
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(90);

        Task task1 = new Task("First Task", "My first task", now, duration.plusMinutes(15));
        Task task2 = new Task("Second Task", "My second task", now.plusHours(1), duration.plusMinutes(50));
        Task task3 = new Task("Third Task", "My third task", now.plusHours(2), duration.plusMinutes(50));
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        Epic epic1 = new Epic("Fist Epic", "My first epic");
        Epic epic2 = new Epic("Second Epic", "My second epic");
        Epic epic3 = new Epic("Third Epic", "My third epic");
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.addEpic(epic3);

        Subtask subtask1 = new Subtask("Fist Subtask", "My first subtask", epic1, now.minusHours(1), duration.plusMinutes(10));
        Subtask subtask2 = new Subtask("Second Subtask", "My second subtask", epic2, now.minusHours(2), duration.plusMinutes(20));
        Subtask subtask3 = new Subtask("Third Subtask", "My third subtask", epic2, now.minusHours(3), duration.plusMinutes(30));
        Subtask subtask4 = new Subtask("Fourth Subtask", "My fourth subtask", epic3, now.minusHours(4), duration.plusMinutes(40));
        Subtask subtask5 = new Subtask("Fifth Subtask", "My fifth subtask", epic3, now.minusHours(5), duration.plusMinutes(50));
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.addSubtask(subtask3);
        manager.addSubtask(subtask4);
        manager.addSubtask(subtask5);

        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getTask(task3.getId());
        manager.getSubtask(subtask1.getId());
        manager.getSubtask(subtask2.getId());
        manager.getSubtask(subtask3.getId());
        manager.getEpic(epic1.getId());
        manager.getEpic(epic2.getId());
        manager.getEpic(epic3.getId());

        System.out.println("manager.getTasks() = " + manager.getTasks());
        System.out.println("manager.getEpics() = " + manager.getEpics());
        System.out.println("manager.getSubtasks() = " + manager.getSubtasks());
        System.out.println("-".repeat(20));
        System.out.println("Subtask by Epic: ");
        System.out.println("manager.getAllSubtasksByEpic(epic1) = " + manager.getAllSubtasksByEpic(epic1));
        System.out.println("manager.getAllSubtasksByEpic(epic2) = " + manager.getAllSubtasksByEpic(epic2));
        System.out.println("manager.getAllSubtasksByEpic(epic3) = " + manager.getAllSubtasksByEpic(epic3));
        System.out.println("manager.getHistory() = " + manager.getHistory());

        task1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task1);
        manager.getTask(task1.getId());

        task2.setStatus(TaskStatus.DONE);
        manager.updateTask(task2);
        manager.getTask(task2.getId());

        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);
        manager.getSubtask(subtask1.getId());

        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask2);
        manager.getSubtask(subtask2.getId());

        epic3.setStatus(TaskStatus.DONE);//Проверяем что нельзя установить принудительно статус в Эпике
        manager.updateEpic(epic3);
        manager.getEpic(epic3.getId());

        manager.getEpic(epic1.getId());
        manager.getEpic(epic2.getId());

        System.out.println("-".repeat(20));
        System.out.println("After status changing");
        System.out.println("manager.getTasks() = " + manager.getTasks());
        System.out.println("manager.getEpics() = " + manager.getEpics());
        System.out.println("manager.getSubtasks() = " + manager.getSubtasks());
        System.out.println("manager.getAllSubtasksByEpic(epic1) = " + manager.getAllSubtasksByEpic(epic1));
        System.out.println("manager.getAllSubtasksByEpic(epic2) = " + manager.getAllSubtasksByEpic(epic2));
        System.out.println("manager.getAllSubtasksByEpic(epic3) = " + manager.getAllSubtasksByEpic(epic3));

        manager.removeTask(task3.getId());
        manager.removeSubtask(subtask3.getId());
        manager.removeEpic(epic3.getId());

        System.out.println("-".repeat(20));
        System.out.println("After removing");
        System.out.println("manager.getTasks() = " + manager.getTasks());
        System.out.println("manager.getEpics() = " + manager.getEpics());
        System.out.println("manager.getSubtasks() = " + manager.getSubtasks());
        System.out.println("manager.getAllSubtasksByEpic(epic1) = " + manager.getAllSubtasksByEpic(epic1));
        System.out.println("manager.getAllSubtasksByEpic(epic2) = " + manager.getAllSubtasksByEpic(epic2));
        System.out.println("manager.getAllSubtasksByEpic(epic3) = " + manager.getAllSubtasksByEpic(epic3));
        System.out.println("manager.getHistory() = " + manager.getHistory());

        manager.clearSubtasks();
        manager.clearTasks();

        System.out.println("-".repeat(20));
        System.out.println("After clearing subtasks, and task");
        System.out.println("manager.getTasks() = " + manager.getTasks());
        System.out.println("manager.getEpics() = " + manager.getEpics());
        System.out.println("manager.getSubtasks() = " + manager.getSubtasks());
        System.out.println("manager.getAllSubtasksByEpic(epic1) = " + manager.getAllSubtasksByEpic(epic1));
        System.out.println("manager.getAllSubtasksByEpic(epic2) = " + manager.getAllSubtasksByEpic(epic2));
        System.out.println("manager.getAllSubtasksByEpic(epic3) = " + manager.getAllSubtasksByEpic(epic3));
        System.out.println("manager.getHistory() = " + manager.getHistory());

        manager.clearEpics();

        System.out.println("-".repeat(20));
        System.out.println("After clearing epics");
        System.out.println("manager.getTasks() = " + manager.getTasks());
        System.out.println("manager.getEpics() = " + manager.getEpics());
        System.out.println("manager.getSubtasks() = " + manager.getSubtasks());
        System.out.println("manager.getHistory() = " + manager.getHistory());

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        Path path = File.createTempFile("taskSave", ".txt").toPath();
        TaskManager fileBackedManager = Managers.getFileBackedTaskManager(path.toString());

        task1 = new Task("First Task", "My first task", now, duration.plusMinutes(15));
        task2 = new Task("Second Task", "My second task", now.plusHours(1), duration.plusMinutes(50));
        task3 = new Task("Third Task", "My third task", now.plusHours(2), duration.plusMinutes(50));

        fileBackedManager.addTask(task1);
        fileBackedManager.addTask(task2);

        epic1 = new Epic("Fist Epic", "My first epic");
        epic2 = new Epic("Second Epic", "My second epic");
        epic3 = new Epic("Third Epic", "My third epic");

        fileBackedManager.addEpic(epic1);
        fileBackedManager.addEpic(epic2);
        fileBackedManager.addEpic(epic3);

        subtask1 = new Subtask("Fist Subtask", "My first subtask", epic1, now.minusHours(1), duration.plusMinutes(10));
        subtask2 = new Subtask("Second Subtask", "My second subtask", epic2, now.minusHours(2), duration.plusMinutes(20));
        subtask3 = new Subtask("Third Subtask", "My third subtask", epic2, now.minusHours(3), duration.plusMinutes(30));
        subtask4 = new Subtask("Fourth Subtask", "My fourth subtask", epic3, now.minusHours(4), duration.plusMinutes(40));
        subtask5 = new Subtask("Fifth Subtask", "My fifth subtask", epic3, now.minusHours(5), duration.plusMinutes(50));

        fileBackedManager.addSubtask(subtask1);
        fileBackedManager.addSubtask(subtask2);
        fileBackedManager.addSubtask(subtask3);
        fileBackedManager.addSubtask(subtask4);
        fileBackedManager.addSubtask(subtask5);

        task1 = fileBackedManager.getTask(task1.getId());
        task1.setStatus(TaskStatus.IN_PROGRESS);
        fileBackedManager.updateTask(task1);

        task2 = fileBackedManager.getTask(task2.getId());
        task2.setStatus(TaskStatus.DONE);
        fileBackedManager.updateTask(task2);

        subtask1 = fileBackedManager.getSubtask(subtask1.getId());
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        fileBackedManager.updateSubtask(subtask1);

        subtask2 = fileBackedManager.getSubtask(subtask2.getId());
        subtask2.setStatus(TaskStatus.DONE);
        fileBackedManager.updateSubtask(subtask2);

        subtask4 = fileBackedManager.getSubtask(subtask4.getId());
        subtask4.setStatus(TaskStatus.DONE);
        fileBackedManager.updateSubtask(subtask4);

        subtask5 = fileBackedManager.getSubtask(subtask5.getId());
        subtask5.setStatus(TaskStatus.IN_PROGRESS);
        fileBackedManager.updateSubtask(subtask5);

        TaskManager restoredFileManager = FileBackedTaskManager.loadFromFile(path.toFile());

        System.out.println("-".repeat(20));
        System.out.println("-".repeat(20));
        System.out.println("Before restoring files");
        System.out.println("-".repeat(20));

        System.out.println("fileBackedManager.getTasks() = " + fileBackedManager.getTasks());
        System.out.println("fileBackedManager.getEpics() = " + fileBackedManager.getEpics());
        System.out.println("fileBackedManager.getSubtasks() = " + fileBackedManager.getSubtasks());
        System.out.println("fileBackedManager.getAllSubtasksByEpic(epic1) = " + fileBackedManager.getAllSubtasksByEpic(epic1));
        System.out.println("fileBackedManager.getAllSubtasksByEpic(epic2) = " + fileBackedManager.getAllSubtasksByEpic(epic2));
        System.out.println("fileBackedManager.getAllSubtasksByEpic(epic3) = " + fileBackedManager.getAllSubtasksByEpic(epic3));

        System.out.println("-".repeat(20));
        System.out.println("After restoring files");
        System.out.println("-".repeat(20));

        System.out.println("restoredFileManager.getTasks() = " + restoredFileManager.getTasks());
        System.out.println("restoredFileManager.getEpics() = " + restoredFileManager.getEpics());
        System.out.println("restoredFileManager.getSubtasks() = " + restoredFileManager.getSubtasks());
        System.out.println("restoredFileManager.getAllSubtasksByEpic(epic1) = " + restoredFileManager.getAllSubtasksByEpic(epic1));
        System.out.println("restoredFileManager.getAllSubtasksByEpic(epic2) = " + restoredFileManager.getAllSubtasksByEpic(epic2));
        System.out.println("restoredFileManager.getAllSubtasksByEpic(epic3) = " + restoredFileManager.getAllSubtasksByEpic(epic3));

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        fileBackedManager.removeSubtask(subtask2.getId());
        fileBackedManager.removeEpic(epic3.getId());
        fileBackedManager.removeTask(task1.getId());

        restoredFileManager = FileBackedTaskManager.loadFromFile(path.toFile());

        System.out.println("-".repeat(20));
        System.out.println("-".repeat(20));
        System.out.println("Before restoring files");
        System.out.println("-".repeat(20));

        System.out.println("fileBackedManager.getTasks() = " + fileBackedManager.getTasks());
        System.out.println("fileBackedManager.getEpics() = " + fileBackedManager.getEpics());
        System.out.println("fileBackedManager.getSubtasks() = " + fileBackedManager.getSubtasks());
        System.out.println("fileBackedManager.getAllSubtasksByEpic(epic1) = " + fileBackedManager.getAllSubtasksByEpic(epic1));
        System.out.println("fileBackedManager.getAllSubtasksByEpic(epic2) = " + fileBackedManager.getAllSubtasksByEpic(epic2));
        System.out.println("fileBackedManager.getAllSubtasksByEpic(epic3) = " + fileBackedManager.getAllSubtasksByEpic(epic3));

        System.out.println("-".repeat(20));
        System.out.println("After restoring files");
        System.out.println("-".repeat(20));

        System.out.println("restoredFileManager.getTasks() = " + restoredFileManager.getTasks());
        System.out.println("restoredFileManager.getEpics() = " + restoredFileManager.getEpics());
        System.out.println("restoredFileManager.getSubtasks() = " + restoredFileManager.getSubtasks());
        System.out.println("restoredFileManager.getAllSubtasksByEpic(epic1) = " + restoredFileManager.getAllSubtasksByEpic(epic1));
        System.out.println("restoredFileManager.getAllSubtasksByEpic(epic2) = " + restoredFileManager.getAllSubtasksByEpic(epic2));
        System.out.println("restoredFileManager.getAllSubtasksByEpic(epic3) = " + restoredFileManager.getAllSubtasksByEpic(epic3));

        fileBackedManager.clearTasks();
        fileBackedManager.clearEpics();

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        restoredFileManager = FileBackedTaskManager.loadFromFile(path.toFile());

        System.out.println("-".repeat(20));
        System.out.println("-".repeat(20));
        System.out.println("Before restoring files");
        System.out.println("-".repeat(20));

        System.out.println("fileBackedManager.getTasks() = " + fileBackedManager.getTasks());
        System.out.println("fileBackedManager.getEpics() = " + fileBackedManager.getEpics());
        System.out.println("fileBackedManager.getSubtasks() = " + fileBackedManager.getSubtasks());
        System.out.println("fileBackedManager.getAllSubtasksByEpic(epic1) = " + fileBackedManager.getAllSubtasksByEpic(epic1));
        System.out.println("fileBackedManager.getAllSubtasksByEpic(epic2) = " + fileBackedManager.getAllSubtasksByEpic(epic2));
        System.out.println("fileBackedManager.getAllSubtasksByEpic(epic3) = " + fileBackedManager.getAllSubtasksByEpic(epic3));

        System.out.println("-".repeat(20));
        System.out.println("After restoring files");
        System.out.println("-".repeat(20));

        System.out.println("restoredFileManager.getTasks() = " + restoredFileManager.getTasks());
        System.out.println("restoredFileManager.getEpics() = " + restoredFileManager.getEpics());
        System.out.println("restoredFileManager.getSubtasks() = " + restoredFileManager.getSubtasks());
        System.out.println("restoredFileManager.getAllSubtasksByEpic(epic1) = " + restoredFileManager.getAllSubtasksByEpic(epic1));
        System.out.println("restoredFileManager.getAllSubtasksByEpic(epic2) = " + restoredFileManager.getAllSubtasksByEpic(epic2));
        System.out.println("restoredFileManager.getAllSubtasksByEpic(epic3) = " + restoredFileManager.getAllSubtasksByEpic(epic3));

        System.out.println("-".repeat(20));
        System.out.println("-".repeat(20));
    }
}
