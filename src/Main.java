import manager.FileBackedTaskManager;
import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

//Класс не будет использоваться в проекте, это для собственной проверки

public class Main {
    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault();
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(30);

        Task task1 = new Task("First Task", "My first task", now, duration.plusMinutes(10));
        Task task2 = new Task("Second Task", "My second task", now.plusHours(1), duration.plusMinutes(20));
        Task task3 = new Task("Third Task", "My third task", now.plusHours(2), duration.plusMinutes(20));
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
        Subtask subtask3 = new Subtask("Third Subtask", "My third subtask", epic2, now.minusHours(3), duration.plusMinutes(20));
        Subtask subtask4 = new Subtask("Fourth Subtask", "My fourth subtask", epic3, now.minusHours(4), duration.plusMinutes(25));
        Subtask subtask5 = new Subtask("Fifth Subtask", "My fifth subtask", epic3, now.minusHours(5), duration.plusMinutes(15));
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
        System.out.println("manager.getPrioritizedTasks() = " + manager.getPrioritizedTasks());

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
        task2 = new Task("Second Task", "My second task", now.plusHours(1), duration.plusMinutes(10));
        task3 = new Task("Third Task", "My third task", now.plusHours(2), duration.plusMinutes(20));

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
        subtask3 = new Subtask("Third Subtask", "My third subtask", epic2, now.minusHours(3), duration.plusMinutes(25));
        subtask4 = new Subtask("Fourth Subtask", "My fourth subtask", epic3, now.minusHours(4), duration.plusMinutes(15));
        subtask5 = new Subtask("Fifth Subtask", "My fifth subtask", epic3, now.minusHours(5), duration.plusMinutes(10));

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
