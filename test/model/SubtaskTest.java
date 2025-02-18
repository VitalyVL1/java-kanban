package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    void testSubtaskEquals() {
        Subtask subtask1 = new Subtask("Subtask1", "Description Subtask1",
                new Epic("Epic1","Description Epic1"));

        Subtask subtask2 = new Subtask("Subtask2", "Description Subtask2",
                new Epic("Epic1","Description Epic2"));
        subtask1.setId(1);
        subtask2.setId(1);
        assertEquals(subtask1, subtask2, "Subtasks не равны друг другу");
    }
}