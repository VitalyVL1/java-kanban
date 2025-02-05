import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.TaskManager;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        Task task1 = new Task("First Task", "My first task");
        Task task2 = new Task("Second Task", "My second task");
        Task task3 = new Task("Third Task", "My third task");
        manager.addTask(task1, task2, task3);

        Epic epic1 = new Epic("Fist Epic", "My first epic");
        Epic epic2 = new Epic("Second Epic", "My second epic");
        Epic epic3 = new Epic("Third Epic", "My third epic");
        manager.addEpic(epic1, epic2, epic3);

        Subtask subtask1 = new Subtask("Fist Subtask", "My first subtask", epic1);
        Subtask subtask2 = new Subtask("Second Subtask", "My second subtask", epic2);
        Subtask subtask3 = new Subtask("Third Subtask", "My third subtask", epic2);
        Subtask subtask4 = new Subtask("Fourth Subtask", "My fourth subtask", epic3);
        Subtask subtask5 = new Subtask("Fifth Subtask", "My fifth subtask", epic3);
        manager.addSubtask(subtask1, subtask2, subtask3, subtask4, subtask5);

        System.out.println("manager.getTasks() = " + manager.getTasks());
        System.out.println("manager.getEpics() = " + manager.getEpics());
        System.out.println("manager.getSubtasks() = " + manager.getSubtasks());
        System.out.println("-".repeat(20));
        System.out.println("Subtask by Epic: ");
        System.out.println("manager.getAllSubtasksByEpic(epic1) = " + manager.getAllSubtasksByEpic(epic1));
        System.out.println("manager.getAllSubtasksByEpic(epic2) = " + manager.getAllSubtasksByEpic(epic2));
        System.out.println("manager.getAllSubtasksByEpic(epic3) = " + manager.getAllSubtasksByEpic(epic3));

        task1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task1);

        task2.setStatus(TaskStatus.DONE);
        manager.updateTask(task2);

        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);

        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask2);

        epic3.setStatus(TaskStatus.DONE);//Проверяем что нельзя установить принудительно статус в Эпике
        manager.updateEpic(epic3);

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

        manager.clearEpics();

        System.out.println("-".repeat(20));
        System.out.println("After clearing epics");
        System.out.println("manager.getTasks() = " + manager.getTasks());
        System.out.println("manager.getEpics() = " + manager.getEpics());
        System.out.println("manager.getSubtasks() = " + manager.getSubtasks());
    }
}
