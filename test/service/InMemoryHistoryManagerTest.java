package service;

import manager.HistoryManager;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private static HistoryManager historyManager;
    private static Task task;
    private static Epic epic;
    private static Subtask subtask;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
        task = new Task("Task1", "Description Task1");
        epic = new Epic("Epic1", "Description Epic1");
        subtask = new Subtask("Subtask1", "Description Subtask1", epic);
        task.setId(1);
        epic.setId(2);
        subtask.setId(3);
    }

    @Test
    void testAdd_ShouldAddTaskToHistory() {
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(1, history.size(), "Task не добавлен в историю");
    }

    @Test
    void testGetHistory_ShouldReturnCorrectHistory() {
        final String description = "Description";
        final String newDescription = "New Description";

        task.setDescription(description);
        epic.setDescription(description);
        subtask.setDescription(description);

        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);

        final List<Task> history = historyManager.getHistory();
        assertFalse(history.size() < 3, "В историю добавлены не все объекты");

        for (int i = 0; i < history.size(); i++) {
            Task newTask = history.get(i);
            assertEquals(description, newTask.getDescription(), newTask.getClass() + " не верно сохранена история");
        }

        task.setDescription(newDescription);
        epic.setDescription(newDescription);
        subtask.setDescription(newDescription);

        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);

        final List<Task> newHistory = historyManager.getHistory();
        final List<Task> checkHistory = new LinkedList<>();

        checkHistory.add(task);
        checkHistory.add(epic);
        checkHistory.add(subtask);

        assertFalse(history.size() < 3, "В историю добавлены не все объекты");
        assertFalse(history.size() > 3, "В историю добавились, а не обновились объекты");

        for (int i = 0; i < newHistory.size(); i++) {
            Task newTask = newHistory.get(i);
            Task checkTask = checkHistory.get(i);

            assertEquals(newDescription, newTask.getDescription(), newTask.getClass() + " не верно обновились данные");
            assertEquals(checkTask, newTask, newTask.getClass() + " не верный порядок в истории");
        }
    }

    @Test
    void testRemove_ShouldRemoveTaskFromHistory() {

        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);
        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);

        final List<Task> historyBeforeRemove = historyManager.getHistory();

        assertFalse(historyBeforeRemove.size() < 3, "В историю добавлены не все объекты");
        assertFalse(historyBeforeRemove.size() > 3, "В историю добавились, а не обновились объекты");

        historyManager.remove(task.getId());
        historyManager.remove(epic.getId());
        historyManager.remove(subtask.getId());

        final List<Task> historyAfterRemove = historyManager.getHistory();

        assertEquals(0, historyAfterRemove.size(), "Не все элементы удалены");
    }

    @Test
    void testSetTaskSubtaskEpic_ShouldNotChangeCurrentDataInHistory() {
        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);

        final List<Task> history = historyManager.getHistory();

        for(Task task : history) {
            task.setDescription("CHECK");
        }

        final List<Task> checkHistory = historyManager.getHistory();

        for(Task task : checkHistory) {
            assertNotEquals("CHECK", task.getDescription(),task.getClass()
                    + " полученный из истории объект изменен с помощью setter'а");
        }


    }
}