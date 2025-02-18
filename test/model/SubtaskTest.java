package model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    private static Subtask subtask1;
    private static Subtask subtask2;
    private static Epic epic;

    @BeforeAll
    static void setUpBeforeClass() {
        epic = new Epic("Epic1", "Description Epic1");
    }

    @BeforeEach
    void setUpBeforeEach() {
        subtask1 = new Subtask("Subtask1", "Description Subtask1",
                epic);
        subtask2 = new Subtask("Subtask2", "Description Subtask2",
                epic);
    }

    @Test
    void testGetEpic_ShouldReturnCorrectEpic() {
        assertEquals(epic, subtask1.getEpic(), "Epic установленный в Subtask не равен полученному");
    }
}