package service;

import manager.HistoryManager;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assertEquals(3, history.size(), "В историю добавлены не все объекты");


        task.setDescription(newDescription);
        epic.setDescription(newDescription);
        subtask.setDescription(newDescription);

        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);

        final List<Task> newHistory = historyManager.getHistory();

        for (int i = 0; i < newHistory.size(); i++) {
            Task newTask = newHistory.get(i);

            if (i < 3) {
                assertEquals(description, newTask.getDescription(), newTask.getClass() + " не верно сохранена история");
            } else {
                assertEquals(newDescription, newTask.getDescription(), newTask.getClass() + " не верно сохранена история");
            }
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

        assertEquals(6, historyBeforeRemove.size(), "Не все элементы добавлены в History");

        historyManager.remove(task);
        historyManager.remove(epic);
        historyManager.remove(subtask);

        final List<Task> historyAfterRemove = historyManager.getHistory();

        assertEquals(0,historyAfterRemove.size(),"Не все элементы удалены");
    }
}