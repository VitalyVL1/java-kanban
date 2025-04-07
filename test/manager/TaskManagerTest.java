package manager;

import model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

abstract class TaskManagerTest<T extends TaskManager>{

    protected abstract TaskManager init();

    protected static TaskManager taskManager;
    protected static Task task;
    protected static Epic epic;
    protected static Subtask subtask1;
    protected static Subtask subtask2;
    protected static LocalDateTime startTime;
    protected static Duration duration;

    @BeforeAll
    static void setUpBeforeClass() {
        startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Instant.now().toEpochMilli()), ZoneOffset.UTC); // создается таким образом, т.к. сохранение в файл идет с точностью до Миллисекунд, что бы корректно работал метод сравнения
        duration = Duration.ofMinutes(15);
    }

    @BeforeEach
    void setUp() {
        taskManager = init();
        task = new Task("Task1", "Description Task1", startTime.plusHours(1), duration.plusMinutes(10));
        epic = new Epic("Epic1", "Description Epic1");
        epic.setId(1);
        subtask1 = new Subtask("Subtask1", "Description Subtask1", epic, startTime.plusHours(2), duration.plusMinutes(5));
        subtask2 = new Subtask("Subtask2", "Description Subtask2", epic, startTime.plusHours(3), duration.plusMinutes(20));
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
        taskManager.addEpic(epic);
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
        taskManager.addEpic(epic);
        taskManager.addTask(task);
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
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);

        taskManager.removeSubtask(subtask1.getId());

        final List<Subtask> subtasks = taskManager.getSubtasks();
        final List<Subtask> subtasksInEpic = taskManager.getAllSubtasksByEpic(epic);

        assertEquals(0, subtasks.size(), "В списке остался Subtask");
        assertEquals(0, subtasksInEpic.size(), "В Epic остался Subtask");
    }

    @Test
    void testGetAllSubtasksByEpic_ShouldReturnAllSubtasks() {
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        final List<Subtask> subtasks = taskManager.getAllSubtasksByEpic(epic);

        assertEquals(2, subtasks.size(), "Выводится не корректный список Subtask");
        assertTrue(subtasks.contains(subtask1), "Выводится не корректный список Subtask");
        assertTrue(subtasks.contains(subtask2), "Выводится не корректный список Subtask");
    }

    @Test
    void testClearTask_ShouldClearTask() {
        taskManager.addTask(task);
        taskManager.addTask(new Task("TaskTest", "Description TaskTest", startTime.plusHours(2), duration.plusMinutes(10)));
        taskManager.addTask(new Task("TaskTest", "Description TaskTest", startTime.plusHours(3), duration.plusMinutes(10)));

        taskManager.clearTasks();
        final List<Task> tasks = taskManager.getTasks();

        assertEquals(0, tasks.size(), "Удалены не все задачи");
    }

    @Test
    void testClearEpic_ShouldClearEpic() {
        taskManager.addEpic(epic);
        taskManager.addEpic(new Epic("Epic2", "Description Epic2"));
        taskManager.addSubtask(subtask1);

        taskManager.clearEpics();

        final List<Epic> epics = taskManager.getEpics();
        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertEquals(0, epics.size(), "Удалены не все эпики");
        assertEquals(0, subtasks.size(), "Удалены не все подзадачи связанные с удаленными эпиками");
    }

    @Test
    void testClearSubtask_ShouldClearSubtask() {
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

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

        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

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
        taskManager.addEpic(epic);
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
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);

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
        taskManager.addEpic(epic);
        taskManager.addTask(task);
        taskManager.addSubtask(subtask1);

        Task presetIdTask = new Task("Title", "Description", startTime.minusHours(5), duration.plusMinutes(10));
        Epic presetIdEpic = new Epic("Title", "Description");
        Subtask presetIdSubtask = new Subtask("Title", "Description", presetIdEpic, startTime.minusHours(7), duration.plusMinutes(5));

        presetIdTask.setId(task.getId());
        presetIdEpic.setId(epic.getId());
        presetIdSubtask.setId(subtask1.getId());

        int idTask = taskManager.addTask(presetIdTask);
        int idEpic = taskManager.addEpic(presetIdEpic);
        int idSubtask = taskManager.addSubtask(presetIdSubtask);

        assertEquals(-1, idTask, "Задача заменила имеющуюся");
        assertEquals(-1, idEpic, "Эпик заменил имеющийся");
        assertEquals(-1, idSubtask, "Подзадача заменила имеющуюся");

        presetIdTask.setId(subtask1.getId() + 1);
        presetIdEpic.setId(subtask1.getId() + 2);
        presetIdSubtask.setId(subtask1.getId() + 3);

        taskManager.addTask(presetIdTask);
        taskManager.addEpic(presetIdEpic);
        taskManager.addSubtask(presetIdSubtask);

        taskManager.addTask(new Task("Title", "Description", startTime.minusHours(1), duration.plusMinutes(10)));
        taskManager.addEpic(new Epic("Title", "Description"));
        taskManager.addSubtask(new Subtask("Title", "Description", epic, startTime.minusHours(2), duration.plusMinutes(5)));

        int taskSize = taskManager.getTasks().size();
        int epicSize = taskManager.getEpics().size();
        int subtaskSize = taskManager.getSubtasks().size();

        assertEquals(3, taskSize, "Генерация Task id происходит некорректно");
        assertEquals(3, epicSize, "Генерация Epic id происходит некорректно");
        assertEquals(3, subtaskSize, "Генерация Subtask id происходит некорректно");
    }

    @Test
    void testGetHistory_ShouldReturnHistory() {
        taskManager.addEpic(epic);
        taskManager.addTask(task);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(4, history.size(), "Получена некорректная история");
        assertTrue(history.contains(task), "История не сохранила Task");
        assertTrue(history.contains(epic), "История не сохранила Epic");
        assertTrue(history.contains(subtask1), "История не сохранила Subtask");
        assertTrue(history.contains(subtask2), "История не сохранила Subtask");
    }

    @Test
    void testRemoveTaskSubtaskEpic_ShouldRemoveTaskSubtaskEpicFromHistory() {
        taskManager.addEpic(epic);
        taskManager.addTask(task);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());

        task.setDescription("Description");
        epic.setDescription("Description");
        subtask1.setDescription("Description");
        subtask2.setDescription("Description");

        taskManager.updateTask(task);
        taskManager.updateEpic(epic);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());

        final List<Task> historyBeforeRemove = taskManager.getHistory();

        taskManager.removeTask(task.getId());
        final List<Task> historyAfterRemoveTask = taskManager.getHistory();

        taskManager.removeSubtask(subtask1.getId());
        final List<Task> historyAfterRemoveSubtask1 = taskManager.getHistory();

        taskManager.removeEpic(epic.getId());
        final List<Task> historyAfterRemoveEpic = taskManager.getHistory();

        assertEquals(4, historyBeforeRemove.size(), "В историю попали не все элементы");

        assertFalse(historyAfterRemoveTask.contains(task), "Task не удален из истории");
        assertFalse(historyAfterRemoveSubtask1.contains(subtask1), "Subtask не удален из истории");
        assertFalse(historyAfterRemoveEpic.contains(epic), "Epic не удален из истории");
        assertFalse(historyAfterRemoveEpic.contains(subtask2), "Subtask относящийся к удаленному Epic не удален из истории");

        assertEquals(0, historyAfterRemoveEpic.size(), "В истории удалены не все элементы");
    }


    @Test
    void testClearTaskSubtaskEpic_ShouldRemoveAllTaskSubtaskEpicFromHistory() {
        Subtask subtask3 = new Subtask("Subtask3", "Description Subtask3", epic, startTime.minusHours(3), duration.plusMinutes(5));

        taskManager.addEpic(epic);
        taskManager.addTask(task);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());

        task.setDescription("Description");
        epic.setDescription("Description");
        subtask1.setDescription("Description");
        subtask2.setDescription("Description");

        taskManager.updateTask(task);
        taskManager.updateEpic(epic);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());

        final List<Task> historyBeforeClear = taskManager.getHistory();

        taskManager.clearTasks();
        final List<Task> historyAfterClearTask = taskManager.getHistory();

        taskManager.clearSubtasks();
        final List<Task> historyAfterClearSubtask = taskManager.getHistory();

        taskManager.addSubtask(subtask3);

        taskManager.getSubtask(subtask3.getId());

        taskManager.clearEpics();
        final List<Task> historyAfterClearEpic = taskManager.getHistory();

        assertEquals(4, historyBeforeClear.size(), "В историю попали не все элементы");

        assertFalse(historyAfterClearTask.contains(task), "Task не удален из истории");
        assertFalse(historyAfterClearSubtask.contains(subtask1), "Subtask не удален из истории");
        assertFalse(historyAfterClearSubtask.contains(subtask2), "Subtask не удален из истории");

        assertFalse(historyAfterClearEpic.contains(epic), "Epic не удален из истории");
        assertFalse(historyAfterClearEpic.contains(subtask3), "Subtask относящийся к удаленному Epic не удален из истории");

        assertEquals(0, historyAfterClearEpic.size(), "В истории удалены не все элементы");
    }

    @Test
    void testSetDataInTaskSubtaskEpic_ShouldNotChangeTaskSubtaskEpicFromTaskManager() {
        taskManager.addEpic(epic);
        taskManager.addTask(task);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.getTask(task.getId()).setTitle("CHECK");
        taskManager.getEpic(epic.getId()).setTitle("CHECK");
        taskManager.getSubtask(subtask1.getId()).setTitle("CHECK");
        taskManager.getSubtask(subtask2.getId()).setTitle("CHECK");

        final List<Task> checkGet = new ArrayList<>();

        checkGet.add(taskManager.getTask(task.getId()));
        checkGet.add(taskManager.getEpic(epic.getId()));
        checkGet.add(taskManager.getSubtask(subtask1.getId()));
        checkGet.add(taskManager.getSubtask(subtask2.getId()));

        for (Task t : checkGet) {
            assertNotEquals("CHECK", t.getTitle(), t.getClass()
                    + " попали изменения get object с помощью setter'ов");
        }

        final List<Task> listGetAll = new ArrayList<>();

        listGetAll.addAll(taskManager.getEpics());
        listGetAll.addAll(taskManager.getSubtasks());
        listGetAll.addAll(taskManager.getTasks());

        for (Task task : listGetAll) {
            task.setTitle("CHECK_ALL");
        }

        final List<Task> checklistGetAll = new ArrayList<>();

        checklistGetAll.addAll(taskManager.getEpics());
        checklistGetAll.addAll(taskManager.getSubtasks());
        checklistGetAll.addAll(taskManager.getTasks());

        for (Task t : checklistGetAll) {
            assertNotEquals("CHECK_ALL", t.getTitle(), t.getClass()
                    + " попали изменения getAll object с помощью setter'ов");
        }
    }

    protected static boolean equalTasks(Task expectedTask, Task actualTask) {
        if (expectedTask == null && actualTask == null) {
            return true;
        }

        if ((expectedTask == null && actualTask != null) || (expectedTask != null && actualTask == null)) {
            return false;
        }

        if (!expectedTask.getClass().equals(actualTask.getClass())) {
            return false;
        }

        int expectedId = expectedTask.getId() == null ? -1 : expectedTask.getId();
        String expectedTitle = expectedTask.getTitle();
        String expectedDescription = expectedTask.getDescription();
        TaskStatus expectedStatus = expectedTask.getStatus();
        TaskType expectedType = expectedTask.getType();
        LocalDateTime expectedStartTime = expectedTask.getStartTime();
        Duration expectedDuration = expectedTask.getDuration();


        int actualId = actualTask.getId() == null ? -1 : actualTask.getId();
        String actualTitle = actualTask.getTitle();
        String actualDescription = actualTask.getDescription();
        TaskStatus actualStatus = actualTask.getStatus();
        TaskType actualType = actualTask.getType();
        LocalDateTime actualStartTime = actualTask.getStartTime();
        Duration actualDuration = actualTask.getDuration();

        return expectedId == actualId &&
                expectedTitle.equals(actualTitle) &&
                expectedDescription.equals(actualDescription) &&
                expectedStatus.equals(actualStatus) &&
                expectedType.equals(actualType) &&
                expectedStartTime.equals(actualStartTime) &&
                expectedDuration.equals(actualDuration);
    }
}
