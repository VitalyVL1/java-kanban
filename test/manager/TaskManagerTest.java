package manager;

import exception.TaskOverlappingException;
import model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

abstract class TaskManagerTest<T extends TaskManager> {

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
        LocalDateTime startDateTimeAfterUpdate = startTime.plusHours(10);
        Duration durationAfterUpdate = duration.plusMinutes(12);

        task.setTitle(titleAfterUpdate);
        task.setDescription(descriptionAfterUpdate);
        task.setStatus(taskStatusAfterUpdate);
        task.setStartTime(startDateTimeAfterUpdate);
        task.setDuration(durationAfterUpdate);

        taskManager.updateTask(task);

        Task taskAfterUpdate = taskManager.getTask(task.getId());

        assertEquals(titleAfterUpdate, taskAfterUpdate.getTitle(), "Task Title не обновился");
        assertEquals(descriptionAfterUpdate, taskAfterUpdate.getDescription(), "Task Description не обновилось");
        assertEquals(taskStatusAfterUpdate, taskAfterUpdate.getStatus(), "Task Status не обновился");
        assertEquals(startDateTimeAfterUpdate, taskAfterUpdate.getStartTime(), "Task StartTime не обновилось");
        assertEquals(durationAfterUpdate, taskAfterUpdate.getDuration(), "Task Duration не обновилась");
    }

    @Test
    void testUpdateEpic_ShouldUpdateEpic() {
        String titleAfterUpdate = "Epic Title";
        String descriptionAfterUpdate = "Epic Description";
        LocalDateTime startDateTimeAfterUpdate = startTime.plusHours(10);
        Duration durationAfterUpdate = duration.plusMinutes(12);

        TaskStatus taskStatusAfterUpdate = TaskStatus.DONE; //Принудительно не установится такой статус

        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        subtask1.setStatus(TaskStatus.DONE);

        epic.setTitle(titleAfterUpdate);
        epic.setDescription(descriptionAfterUpdate);
        epic.setStatus(taskStatusAfterUpdate);
        epic.setStartTime(startDateTimeAfterUpdate);
        epic.setDuration(durationAfterUpdate);

        taskManager.updateEpic(epic);

        LocalDateTime correctEpicStartDateTimeAfterUpdate = subtask1.getStartTime().isBefore(subtask2.getStartTime()) ?
                subtask1.getStartTime() : subtask2.getStartTime();
        Duration correctDurationAfterUpdate = subtask1.getDuration().plus(subtask2.getDuration());

        Epic epicAfterUpdate = taskManager.getEpic(epic.getId());

        assertEquals(titleAfterUpdate, epicAfterUpdate.getTitle(), "Epic Title не обновился");
        assertEquals(descriptionAfterUpdate, epicAfterUpdate.getDescription(), "Epic Description не обновилось");
        assertEquals(TaskStatus.IN_PROGRESS, epicAfterUpdate.getStatus(), "Epic Status обновился некорректно");
        assertEquals(correctEpicStartDateTimeAfterUpdate, epicAfterUpdate.getStartTime(), "Epic StartTime обновилось некорректно");
        assertEquals(correctDurationAfterUpdate, epicAfterUpdate.getDuration(), "Epic Duration обновилось некорректно");
    }

    @Test
    void testUpdateSubtask_ShouldUpdateSubtask() {
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);

        String titleAfterUpdate = "Subtask Title";
        String descriptionAfterUpdate = "Subtask Description";
        TaskStatus taskStatusAfterUpdate = TaskStatus.DONE;
        LocalDateTime startDateTimeAfterUpdate = startTime.plusHours(10);
        Duration durationAfterUpdate = duration.plusMinutes(12);

        subtask1.setTitle(titleAfterUpdate);
        subtask1.setDescription(descriptionAfterUpdate);
        subtask1.setStatus(taskStatusAfterUpdate);
        subtask1.setStartTime(startDateTimeAfterUpdate);
        subtask1.setDuration(durationAfterUpdate);

        taskManager.updateSubtask(subtask1);

        Subtask subtaskAfterUpdate = taskManager.getSubtask(subtask1.getId());

        assertEquals(titleAfterUpdate, subtaskAfterUpdate.getTitle(), "Subtask Title не обновился");
        assertEquals(descriptionAfterUpdate, subtaskAfterUpdate.getDescription(), "Subtask Description не обновилось");
        assertEquals(taskStatusAfterUpdate, subtaskAfterUpdate.getStatus(), "Subtask Status не обновился");
        assertEquals(startDateTimeAfterUpdate, subtaskAfterUpdate.getStartTime(), "Subtask StartTime не обновилось");
        assertEquals(durationAfterUpdate, subtaskAfterUpdate.getDuration(), "Subtask Duration не обновилась");
    }

    @Test
    void testEpicStatusCheck_ShouldReturnCorrectEpicStatus() {
        taskManager.addEpic(epic);

        final TaskStatus epicNewWithoutSubtasks = taskManager.getEpic(epic.getId()).getStatus();

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        final TaskStatus epicNewWithAllNewSubtasks = taskManager.getEpic(epic.getId()).getStatus();

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);

        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        final TaskStatus epicDoneAllSubtasksDone = taskManager.getEpic(epic.getId()).getStatus();

        subtask1.setStatus(TaskStatus.NEW);
        taskManager.updateSubtask(subtask1);

        final TaskStatus epicInProgressWithDoneAndNewSubtask = taskManager.getEpic(epic.getId()).getStatus();

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        subtask2.setStatus(TaskStatus.IN_PROGRESS);

        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        final TaskStatus epicInProgressWithAllInProgressSubtasks = taskManager.getEpic(epic.getId()).getStatus();

        subtask1.setStatus(TaskStatus.DONE);

        taskManager.updateSubtask(subtask1);

        final TaskStatus epicInProgressWithDoneAndInProgressSubtasks = taskManager.getEpic(epic.getId()).getStatus();

        taskManager.removeSubtask(subtask2.getId());

        final TaskStatus epicDoneAfterRemoveSubtask = taskManager.getEpic(epic.getId()).getStatus();

        taskManager.clearSubtasks();

        final TaskStatus epicNewAfterClearSubtask = taskManager.getEpic(epic.getId()).getStatus();

        assertEquals(TaskStatus.NEW, epicNewWithoutSubtasks, "Не верный статус Эпика без Subtask");
        assertEquals(TaskStatus.NEW, epicNewWithAllNewSubtasks, "Не верный статус Эпика с NEW Subtasks");
        assertEquals(TaskStatus.DONE, epicDoneAllSubtasksDone, "Не верный статус Эпика с Subtasks DONE");
        assertEquals(TaskStatus.IN_PROGRESS, epicInProgressWithDoneAndNewSubtask, "Не верный статус Эпика с DONE, NEW Subtask");
        assertEquals(TaskStatus.IN_PROGRESS, epicInProgressWithAllInProgressSubtasks, "Не верный статус Эпика с IN_PROGRESS Subtask");
        assertEquals(TaskStatus.IN_PROGRESS, epicInProgressWithDoneAndInProgressSubtasks, "Не верный статус Эпика с DONE, IN_PROGRESS Subtask");
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
        Subtask presetIdSubtask = new Subtask("Title", "Description", epic, startTime.minusHours(7), duration.plusMinutes(5));

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

    @Test
    void testGetPrioritizedTasks() {
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addTask(task);

        final Task[] prioritizedTasksArray = taskManager.getPrioritizedTasks().toArray(Task[]::new);
        final List<Task> sortedByStartTimeTask = new ArrayList<>();
        sortedByStartTimeTask.addAll(taskManager.getSubtasks());
        sortedByStartTimeTask.addAll(taskManager.getTasks());

        sortedByStartTimeTask.sort(Comparator.comparing(Task::getStartTime));

        final Task[] sortedByStartTimeTaskArray = sortedByStartTimeTask.toArray(Task[]::new);

        assertArrayEquals(prioritizedTasksArray, sortedByStartTimeTaskArray, "Не правильно сформирован приоритизированный список задач");
    }

    @Test
    void epicStartTimeAndDurationTest_ShouldReturnCalculatedStartTimeAndDuration() {
        taskManager.addEpic(epic);

        final LocalDateTime startTimeBeforeAddSubtask = epic.getStartTime(); // null
        final LocalDateTime endTimeBeforeAddSubtask = epic.getEndTime(); // null
        final Duration durationBeforeAddSubtask = epic.getDuration(); // Duration.ZERO

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        final LocalDateTime startTimeAfterAddSubtask = epic.getStartTime();
        final LocalDateTime endTimeAfterAddSubtask = epic.getEndTime();
        final Duration durationAfterAddSubtask = epic.getDuration();

        final LocalDateTime correctStartTime = subtask1.getStartTime().isBefore(subtask2.getStartTime()) ?
                subtask1.getStartTime() : subtask2.getStartTime();
        final LocalDateTime correctEndTime = subtask1.getEndTime().isAfter(subtask2.getEndTime()) ?
                subtask1.getEndTime() : subtask2.getEndTime();
        final Duration correctDuration = subtask1.getDuration().plus(subtask2.getDuration());

        assertNull(startTimeBeforeAddSubtask, "Не верно установлено StartTime у нового Epic");
        assertNull(endTimeBeforeAddSubtask, "Не верное EndTime у нового Epic");
        assertEquals(Duration.ZERO, durationBeforeAddSubtask, "Не верно установлена Duration у нового Epic");

        assertEquals(correctStartTime, startTimeAfterAddSubtask, "Не верно рассчитано StartTime у Epic");
        assertEquals(correctEndTime, endTimeAfterAddSubtask, "Не верно рассчитана Duration у Epic");
        assertEquals(correctDuration, durationAfterAddSubtask, "Не верно рассчитано EndTime Epic");
    }

    @Test
    void testAddOverlappingTask_ShouldThrowException() {
        taskManager.addTask(task);
        Task copyOfTask = new Task(task);
        copyOfTask.setId(11);

        assertThrows(TaskOverlappingException.class, () -> taskManager.addTask(copyOfTask), "Exception не выбросился при добавлении пересекающейся задачи");
    }

    @Test
    void testAddOverlappingSubTask_ShouldThrowException() {
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);
        Subtask copyOfSubtask = new Subtask(subtask1);
        copyOfSubtask.setId(11);

        assertThrows(TaskOverlappingException.class, () -> taskManager.addTask(copyOfSubtask), "Exception не выбросился при добавлении пересекающейся подзадачи");
    }

    @Test
    void testUpdateOverlappingTask_ShouldThrowException() {
        taskManager.addEpic(epic);
        int taskId = taskManager.addTask(task);
        int subtaskId = taskManager.addSubtask(subtask1);

        LocalDateTime overlappingStartTime = taskManager.getSubtask(subtaskId).getStartTime();

        Task taskToUpdate = taskManager.getTask(taskId);
        taskToUpdate.setStartTime(overlappingStartTime);

        assertThrows(TaskOverlappingException.class, () -> taskManager.updateTask(taskToUpdate), "Exception не выбросился при добавлении пересекающейся задачи");
    }

    @Test
    void testUpdateOverlappingSubTask_ShouldThrowException() {
        taskManager.addEpic(epic);
        int taskId = taskManager.addTask(task);
        int subtaskId = taskManager.addSubtask(subtask1);

        LocalDateTime overlappingStartTime = taskManager.getTask(taskId).getStartTime();

        Subtask subtaskToUpdate = taskManager.getSubtask(subtaskId);
        subtaskToUpdate.setStartTime(overlappingStartTime);

        assertThrows(TaskOverlappingException.class, () -> taskManager.updateSubtask(subtaskToUpdate), "Exception не выбросился при добавлении пересекающейся задачи");
    }

    @Test
    void testUpdateTaskSubtaskWithDeletingStartTime_ShouldDeleteTaskSubtaskFromPrioritizedList() {
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Subtask subtask1ToUpdate = taskManager.getSubtask(subtask1.getId());
        Subtask subtask2ToUpdate = taskManager.getSubtask(subtask2.getId());

        subtask1ToUpdate.setStartTime(null);
        subtask1ToUpdate.setDuration(Duration.ZERO);

        taskManager.updateSubtask(subtask1ToUpdate);

        final List<Task> prioritizedListAfterUpdatingSubtask1 = taskManager.getPrioritizedTasks();

        assertEquals(subtask2ToUpdate.getStartTime(), taskManager.getEpic(epic.getId()).getStartTime(), "StartTime Epic не пересчиталось");
        assertEquals(subtask2ToUpdate.getDuration(), taskManager.getEpic(epic.getId()).getDuration(), "Duration Epic не пересчиталась");

        subtask2ToUpdate.setStartTime(null);
        subtask2ToUpdate.setDuration(Duration.ZERO);

        taskManager.updateSubtask(subtask2ToUpdate);

        final List<Task> prioritizedListAfterUpdatingSubtask2 = taskManager.getPrioritizedTasks();

        assertNull(taskManager.getEpic(epic.getId()).getStartTime(), "StartTime Epic не пересчиталось");
        assertEquals(Duration.ZERO, taskManager.getEpic(epic.getId()).getDuration(), "Duration Epic не пересчиталась");

        assertEquals(1, prioritizedListAfterUpdatingSubtask1.size(), "Subtask1 не удален из приоритизированного списка");
        assertEquals(0, prioritizedListAfterUpdatingSubtask2.size(), "Subtask2 не удален из приоритизированного списка");
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
