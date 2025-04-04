package manager;

import model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTaskManagerTest {

    private static TaskManager taskManager;
    private static File file;
    private static Task task;
    private static Epic epic;
    private static Subtask subtask1;
    private static Subtask subtask2;
    private static LocalDateTime startTime;
    private static Duration duration;


    @BeforeAll
    static void setUpBeforeClass() {
        try {
            file = File.createTempFile("test", ".txt");
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }

        startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Instant.now().toEpochMilli()), ZoneId.systemDefault());
        duration = Duration.ofMinutes(15);

        taskManager = new FileBackedTaskManager(new InMemoryHistoryManager(), file.getPath());
        task = new Task("Task1", "Description Task1", startTime.plusMinutes(10), duration.plusMinutes(10));
        task.setId(1);
        epic = new Epic("Epic1", "Description Epic1");
        epic.setId(2);
        subtask1 = new Subtask("Subtask1", "Description Subtask1", epic, startTime.minusHours(1), duration.plusMinutes(5));
        subtask1.setId(3);
        subtask2 = new Subtask("Subtask2", "Description Subtask2", epic, startTime.plusHours(1), duration.plusMinutes(20));
        subtask2.setId(4);
    }

    @Test
    void testSaveAndLoadTask_ShouldSaveTaskToFileAndLoadFromIt() {
        taskManager.addTask(task);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        TaskManager loadedTaskManager = FileBackedTaskManager.loadFromFile(file);
        Task actualTask = loadedTaskManager.getTask(task.getId());
        Epic actualEpic = loadedTaskManager.getEpic(epic.getId());
        Subtask actualSubtask1 = loadedTaskManager.getSubtask(subtask1.getId());
        Subtask actualSubtask2 = loadedTaskManager.getSubtask(subtask2.getId());

        assertTrue(equalTasks(task, actualTask), "Tasks не равны");
        assertTrue(equalTasks(epic, actualEpic), "Epics не равны");
        assertTrue(equalTasks(subtask1, actualSubtask1) && (Objects.equals(subtask1.getEpicId(), actualSubtask1.getEpicId())), "Subtask1 не равны");
        assertTrue(equalTasks(subtask2, actualSubtask2) && (Objects.equals(subtask2.getEpicId(), actualSubtask2.getEpicId())), "Subtask2 не равны");

        List<Subtask> actualSubtasksByEpic = loadedTaskManager.getAllSubtasksByEpic(epic);
        assertTrue(actualSubtasksByEpic.containsAll(List.of(subtask1, subtask2)), "Epic не содержит нужные Subtask");

        taskManager.removeTask(task.getId());
        taskManager.removeSubtask(subtask2.getId());

        loadedTaskManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(0, loadedTaskManager.getTasks().size(), "Task не удалился");
        assertEquals(1, loadedTaskManager.getSubtasks().size(), "Subtask не удалился");

        taskManager.removeEpic(epic.getId());
        loadedTaskManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(0, loadedTaskManager.getTasks().size(), "Epic не удалился");
        assertEquals(0, loadedTaskManager.getSubtasks().size(), "Subtask не удалился при удалении эпика");

        taskManager.addTask(task);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        task.setTitle("Test Title");
        task.setDescription("Test Description");
        taskManager.updateTask(task);

        epic.setTitle("Test Title");
        epic.setDescription("Test Description");
        taskManager.updateEpic(epic);

        subtask1.setTitle("Test Title");
        subtask1.setDescription("Test Description");
        taskManager.updateSubtask(subtask1);

        loadedTaskManager = FileBackedTaskManager.loadFromFile(file);
        actualTask = loadedTaskManager.getTask(task.getId());
        actualEpic = loadedTaskManager.getEpic(epic.getId());
        actualSubtask1 = loadedTaskManager.getSubtask(subtask1.getId());
        actualSubtask2 = loadedTaskManager.getSubtask(subtask2.getId());

        assertTrue(equalTasks(task, actualTask), "Tasks не равны");
        assertTrue(equalTasks(epic, actualEpic), "Epics не равны");
        assertTrue(equalTasks(subtask1, actualSubtask1) && (Objects.equals(subtask1.getEpicId(), actualSubtask1.getEpicId())), "Subtask1 не равны");
        assertTrue(equalTasks(subtask2, actualSubtask2) && (Objects.equals(subtask2.getEpicId(), actualSubtask2.getEpicId())), "Subtask2 не равны");

        taskManager.clearTasks();
        taskManager.clearSubtasks();
        taskManager.clearEpics();

        loadedTaskManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(0, loadedTaskManager.getTasks().size(), "Список Task не очистился");
        assertEquals(0, loadedTaskManager.getTasks().size(), "Список Epic не очистился");
        assertEquals(0, loadedTaskManager.getSubtasks().size(), "Список Subtask не очистился");
    }

    @Test
    void testLoadFromEmptyFile_ShouldReturnEmptyTask() throws IOException {
        TaskManager tempTaskManager = FileBackedTaskManager.loadFromFile(File.createTempFile("empty", ".txt"));

        assertEquals(0, tempTaskManager.getTasks().size(), "Tasks должен быть пустым");
        assertEquals(0, tempTaskManager.getEpics().size(), "Epics должен быть пустым");
        assertEquals(0, tempTaskManager.getSubtasks().size(), "Subtask должен быть пустым");
    }

    private static boolean equalTasks(Task expectedTask, Task actualTask) {
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
