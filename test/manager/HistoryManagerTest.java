package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class HistoryManagerTest {

    private static HistoryManager historyManager;
    private static Task task;
    private static Epic epic;
    private static Subtask subtask1;
    private static Subtask subtask2;
    private static LocalDateTime startTime;
    private static Duration duration;

    protected abstract HistoryManager init();

    @BeforeAll
    static void setUpBeforeClass() {
        startTime = LocalDateTime.now();
        duration = Duration.ofMinutes(15);
    }

    @BeforeEach
    void setUp() {
        historyManager = init();
        task = new Task("Task1", "Description Task1", startTime.plusHours(1), duration.plusMinutes(10));
        epic = new Epic("Epic1", "Description Epic1");
        subtask1 = new Subtask("Subtask1", "Description Subtask1", epic, startTime.plusHours(2), duration.plusMinutes(5));
        subtask2 = new Subtask("Subtask2", "Description Subtask2", epic, startTime.plusHours(3), duration.plusMinutes(5));
        task.setId(1);
        epic.setId(2);
        subtask1.setId(3);
        subtask2.setId(4);
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
        subtask1.setDescription(description);

        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask1);

        final List<Task> history = historyManager.getHistory();
        assertFalse(history.size() < 3, "В историю добавлены не все объекты");

        for (Task newTask : history) {
            assertEquals(description, newTask.getDescription(), newTask.getClass() + " не верно сохранена история");
        }

        task.setDescription(newDescription);
        epic.setDescription(newDescription);
        subtask1.setDescription(newDescription);

        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask1);

        final List<Task> newHistory = historyManager.getHistory();
        final List<Task> checkHistory = new LinkedList<>();

        checkHistory.add(task);
        checkHistory.add(epic);
        checkHistory.add(subtask1);

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
        historyManager.add(subtask1);
        historyManager.add(subtask2);

        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask1);
        historyManager.add(subtask2);

        final List<Task> historyBeforeRemove = historyManager.getHistory();

        assertFalse(historyBeforeRemove.size() < 4, "В историю добавлены не все объекты");
        assertFalse(historyBeforeRemove.size() > 4, "В историю добавились, а не обновились объекты");

        historyManager.remove(historyBeforeRemove.get(1).getId());
        final List<Task> historyAfterRemoveFromMiddle = historyManager.getHistory();

        historyManager.remove(historyAfterRemoveFromMiddle.getLast().getId());
        final List<Task> historyAfterRemoveLast = historyManager.getHistory();

        historyManager.remove(historyAfterRemoveLast.getFirst().getId());
        final List<Task> historyAfterRemoveFirst = historyManager.getHistory();

        historyManager.remove(historyAfterRemoveFirst.getFirst().getId());
        final List<Task> historyAfterRemoveAll = historyManager.getHistory();

        assertEquals(3, historyAfterRemoveFromMiddle.size(), "Не удалился средний элемент");
        assertEquals(2, historyAfterRemoveLast.size(), "Не удалился последний элемент");
        assertEquals(1, historyAfterRemoveFirst.size(), "Не удалился первый элемент");
        assertEquals(0, historyAfterRemoveAll.size(), "Не все элементы удалены");
    }

    @Test
    void testSetTaskSubtaskEpic_ShouldNotChangeCurrentDataInHistory() {
        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask1);

        final List<Task> history = historyManager.getHistory();

        for (Task task : history) {
            task.setDescription("CHECK");
        }

        final List<Task> checkHistory = historyManager.getHistory();

        for (Task task : checkHistory) {
            assertNotEquals("CHECK", task.getDescription(), task.getClass()
                    + " полученный из истории объект изменен с помощью setter'а");
        }
    }

    @Test
    void testGetEmptyHistory_ShouldReturnEmptyList() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(),"Неверное выводится пустая история");
    }
}
