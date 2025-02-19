package service;

import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private static TaskManager taskManager;
    private static Task task;
    private static Epic epic;
    private static Subtask subtask1;
    private static Subtask subtask2;

    @BeforeAll
    static void setUpBeforeClass() {
        task = new Task("Task1", "Description Task1");
    }

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();

        epic = new Epic("Epic1", "Description Epic1");
        subtask1 = new Subtask("Subtask1", "Description Subtask1", epic);
        subtask2 = new Subtask("Subtask1", "Description Subtask1", epic);
    }

    @Test
    void testAddGetTask_ShouldAddAndReturnTask() {
        final int taskId = taskManager.addTask(task);
        final Task savedTask = taskManager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void testAddGetEpic_ShouldAddAndReturnEpic() {
        final int epicId = taskManager.addEpic(epic);
        final Epic savedEpic = taskManager.getEpic(epicId);

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.getFirst(), "Эпики не совпадают.");
    }

    @Test
    void testAddGetSubtask_ShouldAddAndReturnSubtask() {
        final int subtaskId = taskManager.addSubtask(subtask1);
        final Subtask savedSubtask = taskManager.getSubtask(subtaskId);

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask1, savedSubtask, "Подзадачи не совпадают.");

        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask1, subtasks.getFirst(), "Подзадачи не совпадают.");
    }

    @Test
    void testAddTheSameTaskSubtaskEpic_ShouldNotAddTheSameTaskSubtaskEpic() {
        taskManager.addTask(task);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);

        int duplicatedTaskId = taskManager.addTask(task); //should return -1
        int duplicatedEpicId = taskManager.addEpic(epic); //should return -1
        int duplicatedSubtaskId = taskManager.addSubtask(subtask1); //should return -1

        assertEquals(-1, duplicatedTaskId, "Одна и та же задача добавлена");
        assertEquals(-1, duplicatedEpicId, "Один и тот же эпик добавлен");
        assertEquals(-1, duplicatedSubtaskId, "Одна и та же подзадача добавлена");
    }

    @Test
    void testRemoveTask_ShouldRemoveTask() {
        taskManager.addTask(task);
        taskManager.removeTask(task.getId());

        final List<Task> tasks = taskManager.getTasks();

        assertEquals(0, tasks.size(), "В списке остался Task");
    }

    @Test
    void testRemoveEpic_ShouldRemoveEpic() {
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);

        taskManager.removeEpic(epic.getId());

        final List<Epic> epics = taskManager.getEpics();
        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertEquals(0, epics.size(), "В списке остался Epic");
        assertEquals(0, subtasks.size(), "В списке остался Subtask относящийся к удаленному Epic");
    }

    @Test
    void testRemoveSubtask_ShouldRemoveSubtask() {
        taskManager.addSubtask(subtask1);
        taskManager.addEpic(epic);

        taskManager.removeSubtask(subtask1.getId());

        final List<Subtask> subtasks = taskManager.getSubtasks();
        final List<Subtask> subtasksInEpic = taskManager.getAllSubtasksByEpic(epic);

        assertEquals(0, subtasks.size(), "В списке остался Subtask");
        assertEquals(0, subtasksInEpic.size(), "В Epic остался Subtask");
    }

    @Test
    void testGetAllSubtasksByEpic_ShouldReturnAllSubtasks() {
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addEpic(epic);

        final List<Subtask> subtasks = taskManager.getAllSubtasksByEpic(epic);

        assertEquals(2, subtasks.size(), "Выводится не корректный список Subtask");
        assertTrue(subtasks.contains(subtask1), "Выводится не корректный список Subtask");
        assertTrue(subtasks.contains(subtask2), "Выводится не корректный список Subtask");
    }

    @Test
    void testClearTask_ShouldClearTask() {
        taskManager.addTask(task);
        taskManager.addTask(new Task("TaskTest", "Description TaskTest"));
        taskManager.addTask(new Task("TaskTest", "Description TaskTest"));

        taskManager.clearTasks();
        final List<Task> tasks = taskManager.getTasks();

        assertEquals(0, tasks.size(), "Удалены не все задачи");
    }

    @Test
    void testClearEpic_ShouldClearEpic() {
        taskManager.addSubtask(subtask1);
        taskManager.addEpic(epic);
        taskManager.addEpic(new Epic("Epic2", "Description Epic2"));

        taskManager.clearEpics();

        final List<Epic> epics = taskManager.getEpics();
        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertEquals(0, epics.size(), "Удалены не все эпики");
        assertEquals(0, subtasks.size(), "Удалены не все подзадачи связанные с удаленными эпиками");
    }

    @Test
    void testClearSubtask_ShouldClearSubtask() {
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addEpic(epic);

        taskManager.clearSubtasks();

        final List<Subtask> subtasks = taskManager.getSubtasks();
        final List<Subtask> subtasksInEpic = taskManager.getAllSubtasksByEpic(epic);

        assertEquals(0, subtasks.size(), "Удалены не все подзадачи");
        assertEquals(0, subtasksInEpic.size(), "Удалены не все удаленные подзадачи и эпика");
    }

    @Test
    void testUpdateTask_ShouldUpdateTask() {
        taskManager.addTask(task);

        String titleAfterUpdate = "Task Title";
        String descriptionAfterUpdate = "Task Description";
        TaskStatus taskStatusAfterUpdate = TaskStatus.IN_PROGRESS;

        task.setTitle(titleAfterUpdate);
        task.setDescription(descriptionAfterUpdate);
        task.setStatus(taskStatusAfterUpdate);

        taskManager.updateTask(task);

        Task taskAfterUpdate = taskManager.getTask(task.getId());

        assertEquals(titleAfterUpdate, taskAfterUpdate.getTitle(), "Title не обновился");
        assertEquals(descriptionAfterUpdate, taskAfterUpdate.getDescription(), "Description не обновилось");
        assertEquals(taskStatusAfterUpdate, taskAfterUpdate.getStatus(), "Status не обновился");
    }

    @Test
    void testUpdateEpic_ShouldUpdateEpic() {
        String titleAfterUpdate = "Epic Title";
        String descriptionAfterUpdate = "Epic Description";
        TaskStatus taskStatusAfterUpdate = TaskStatus.DONE; //Принудительно не установится такой статус

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addEpic(epic);

        subtask1.setStatus(TaskStatus.DONE);

        epic.setTitle(titleAfterUpdate);
        epic.setDescription(descriptionAfterUpdate);
        epic.setStatus(taskStatusAfterUpdate);

        taskManager.updateEpic(epic);

        Epic epicAfterUpdate = taskManager.getEpic(epic.getId());

        assertEquals(titleAfterUpdate, epicAfterUpdate.getTitle(), "Title не обновился");
        assertEquals(descriptionAfterUpdate, epicAfterUpdate.getDescription(), "Description не обновилось");
        assertEquals(TaskStatus.IN_PROGRESS, epicAfterUpdate.getStatus(), "Status обновился некорректно");
    }

    @Test
    void testUpdateSubtask_ShouldUpdateSubtask() {
        taskManager.addSubtask(subtask1);

        String titleAfterUpdate = "Subtask Title";
        String descriptionAfterUpdate = "Subtask Description";
        TaskStatus taskStatusAfterUpdate = TaskStatus.DONE;

        subtask1.setTitle(titleAfterUpdate);
        subtask1.setDescription(descriptionAfterUpdate);
        subtask1.setStatus(taskStatusAfterUpdate);

        taskManager.updateSubtask(subtask1);

        Subtask subtaskAfterUpdate = taskManager.getSubtask(subtask1.getId());

        assertEquals(titleAfterUpdate, subtaskAfterUpdate.getTitle(), "Title не обновился");
        assertEquals(descriptionAfterUpdate, subtaskAfterUpdate.getDescription(), "Description не обновилось");
        assertEquals(taskStatusAfterUpdate, subtaskAfterUpdate.getStatus(), "Status не обновился");
    }

    @Test
    void testEpicStatusCheck_ShouldReturnCorrectEpicStatus() {
        taskManager.addSubtask(subtask1);
        taskManager.addEpic(epic);

        final TaskStatus epicNewWithoutSubtasks = taskManager.getEpic(epic.getId()).getStatus();

        subtask1.setStatus(TaskStatus.DONE);

        taskManager.updateSubtask(subtask1);

        final TaskStatus epicDoneAllSubtasksDone = taskManager.getEpic(epic.getId()).getStatus();

        taskManager.addSubtask(subtask2);

        final TaskStatus epicInProgress = taskManager.getEpic(epic.getId()).getStatus();

        taskManager.removeSubtask(subtask2.getId());

        final TaskStatus epicDoneAfterRemoveSubtask = taskManager.getEpic(epic.getId()).getStatus();

        taskManager.clearSubtasks();

        final TaskStatus epicNewAfterClearSubtask = taskManager.getEpic(epic.getId()).getStatus();

        assertEquals(TaskStatus.NEW, epicNewWithoutSubtasks, "Не верный статус Эпика без Subtask");
        assertEquals(TaskStatus.DONE, epicDoneAllSubtasksDone, "Не верный статус Эпика с Subtasks DONE");
        assertEquals(TaskStatus.IN_PROGRESS, epicInProgress, "Не верный статус Эпика со смешанным статусом Subtask");
        assertEquals(TaskStatus.DONE, epicDoneAfterRemoveSubtask, "Не верный статус Эпика после удаления Subtask");
        assertEquals(TaskStatus.NEW, epicNewAfterClearSubtask, "Не верный статус Эпика после удаления всех Subtask");
    }

    @Test
    void testAddTaskSubtaskEpicWithPresetId_ShouldNotReplaceExistingObjects() {
        taskManager.addTask(task);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);

        Task presetIdTask = new Task("Title", "Description");
        Epic presetIdEpic = new Epic("Title", "Description");
        Subtask presetIdSubtask = new Subtask("Title", "Description", presetIdEpic);

        presetIdTask.setId(task.getId());
        presetIdEpic.setId(epic.getId());
        presetIdSubtask.setId(subtask1.getId());

        int idTask = taskManager.addTask(presetIdTask);
        int idEpic = taskManager.addEpic(presetIdEpic);
        int idSubtask = taskManager.addSubtask(presetIdSubtask);

        assertEquals(-1, idTask, "Задача заменила имеющуюся");
        assertEquals(-1, idEpic, "Эпик заменил имеющийся");
        assertEquals(-1, idSubtask, "Подзадача заменила имеющуюся");

        presetIdTask.setId(2);
        presetIdEpic.setId(2);
        presetIdSubtask.setId(2);

        taskManager.addTask(presetIdTask);
        taskManager.addEpic(presetIdEpic);
        taskManager.addSubtask(presetIdSubtask);

        taskManager.addTask(new Task("Title", "Description"));
        taskManager.addEpic(new Epic("Title", "Description"));
        taskManager.addSubtask(new Subtask("Title", "Description", epic));

        int taskSize = taskManager.getTasks().size();
        int epicSize = taskManager.getEpics().size();
        int subtaskSize = taskManager.getSubtasks().size();

        assertEquals(3,taskSize,"Генерация Task id происходит некорректно");
        assertEquals(3,epicSize,"Генерация Epic id происходит некорректно");
        assertEquals(3,subtaskSize,"Генерация Subtask id происходит некорректно");
    }

    @Test
    void testGetHistory_ShouldReturnHistory() {
        taskManager.addTask(task);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(4, history.size(),"Получена некорректная история");
        assertTrue(history.contains(task),"История не сохранила Task");
        assertTrue(history.contains(epic),"История не сохранила Epic");
        assertTrue(history.contains(subtask1),"История не сохранила Subtask");
        assertTrue(history.contains(subtask2),"История не сохранила Subtask");
    }

}