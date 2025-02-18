package service;

import manager.HistoryManager;
import manager.TaskManager;
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
}