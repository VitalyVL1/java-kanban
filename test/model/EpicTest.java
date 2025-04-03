package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpicTest {
    private static Epic epic1;
    private static Epic epic2;
    private static Subtask subtask1;
    private static Subtask subtask2;

    @BeforeEach
    void setUpBeforeEach() {
        epic1 = new Epic("Epic1", "Description Epic1");
        epic2 = new Epic("Epic2", "Description Epic2");
        epic1.setId(1);
        epic2.setId(2);
        subtask1 = new Subtask("Subtask1", "Description Subtask1", epic1);
        subtask2 = new Subtask("Subtask2", "Description Subtask2", epic1);


    }

    @Test
    void TestAddOrUpdateSubtask_ShouldReturnTrue() {
        int id = 1;

        epic1.setId(id);
        subtask1.setId(id);
        subtask2.setId(id);

        epic1.addOrUpdateSubtask(subtask1);
        assertTrue(epic1.getSubtasksId().contains(subtask1.getId()), "Subtask1 не добавлен");

        epic1.addOrUpdateSubtask(subtask2);
        assertTrue(epic1.getSubtasksId().contains(subtask2.getId()), "Subtask2 не обновлен");
    }

    @Test
    void TestRemoveSubtask_ShouldRemoveSubtask() {
        epic1.setId(1);
        subtask1.setId(1);

        epic1.addOrUpdateSubtask(subtask1);
        assertEquals(1, epic1.getSubtasksId().size(), "Subtask не добавлен");

        epic1.removeSubtask(subtask1);
        assertEquals(0, epic1.getSubtasksId().size(), "Subtask не удален");
    }

    @Test
    void TestGetSubtasks_ShouldReturnAllSubtasks() {
        epic1.setId(1);
        subtask1.setId(2);
        subtask2.setId(3);

        epic1.addOrUpdateSubtask(subtask1);
        epic1.addOrUpdateSubtask(subtask2);

        assertEquals(2, epic1.getSubtasksId().size());
    }

}