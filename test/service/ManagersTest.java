package service;

import manager.HistoryManager;
import manager.TaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void testGetDefault_ShouldReturnDefaultTaskManager() {
        assertTrue(Managers.getDefault() instanceof TaskManager,
                "Default task manager должен имплементировать TaskManager");
    }

    @Test
    void testGetDefaultHistoryManager_ShouldReturnDefaultHistoryManager() {
        assertTrue(Managers.getDefaultHistory() instanceof HistoryManager,
                "Default history manager должен имплементировать HistoryManager");
    }
}