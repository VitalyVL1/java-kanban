package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {
    private static Epic epic1;
    private static Epic epic2;
    private static Subtask subtask1;
    private static Subtask subtask2;

    @BeforeEach
    void setUpBeforeEach() {
        epic1 = new Epic("Epic1", "Description Epic1");
        epic2 = new Epic("Epic2", "Description Epic2");
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
        assertEquals(subtask1, epic1.getSubtasks().get(id), "Subtask не добавлен");

        epic1.addOrUpdateSubtask(subtask2);
        assertEquals(subtask2, epic1.getSubtasks().get(id), "Subtask не обновлен");
    }

    @Test
    void TestRemoveSubtask_ShouldRemoveSubtask() {
        epic1.setId(1);
        subtask1.setId(1);

        epic1.addOrUpdateSubtask(subtask1);
        assertEquals(1, epic1.getSubtasks().size(), "Subtask не добавлен");

        epic1.removeSubtask(subtask1);
        assertEquals(0, epic1.getSubtasks().size(), "Subtask не удален");
    }

    @Test
    void TestGetSubtaskById_ShouldReturnSubtask() {
        epic1.setId(1);
        subtask1.setId(1);
        epic1.addOrUpdateSubtask(subtask1);

        assertEquals(subtask1, epic1.getSubtaskById(subtask1.getId()), "Subtask не возвращен по id");
    }

    @Test
    void TestGetSubtasks_ShouldReturnAllSubtasks() {
        epic1.setId(1);
        subtask1.setId(1);
        subtask2.setId(2);

        epic1.addOrUpdateSubtask(subtask1);
        epic1.addOrUpdateSubtask(subtask2);

        assertEquals(2, epic1.getSubtasks().size());
    }

}