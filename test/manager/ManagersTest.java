package manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void testGetDefault_ShouldReturnDefaultTaskManager() {
        assertInstanceOf(TaskManager.class, Managers.getDefault(),
                "Default task manager должен имплементировать TaskManager");
    }

    @Test
    void testGetDefaultHistoryManager_ShouldReturnDefaultHistoryManager() {
        assertInstanceOf(HistoryManager.class, Managers.getDefaultHistory(),
                "Default history manager должен имплементировать HistoryManager");
    }

    @Test
    void testGetFileBackedTaskManager_ShouldReturnFileBackedTaskManager() {
        assertInstanceOf(TaskManager.class, Managers.getFileBackedTaskManager("test"),
                "FileBackedTaskManager должен имплементировать TaskManager");

        assertInstanceOf(InMemoryTaskManager.class, Managers.getFileBackedTaskManager("test"),
                "FileBackedTaskManager должен наследоваться от InMemoryTaskManager");
    }
}