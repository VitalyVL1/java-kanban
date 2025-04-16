package model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    private static Subtask subtask1;
    private static Subtask subtask2;
    private static Epic epic;
    private static LocalDateTime startTime;
    private static Duration duration;

    @BeforeAll
    static void setUpBeforeClass() {
        startTime = LocalDateTime.now();
        duration = Duration.ofMinutes(15);
        epic = new Epic("Epic1", "Description Epic1");
        epic.setId(1);
    }

    @BeforeEach
    void setUpBeforeEach() {
        subtask1 = new Subtask("Subtask1", "Description Subtask1", epic, startTime.minusHours(1), duration.plusMinutes(5));
        subtask2 = new Subtask("Subtask2", "Description Subtask2", epic, startTime.plusHours(1), duration.plusMinutes(20));
        subtask1.setId(2);
        subtask2.setId(3);
    }

    @Test
    void testGetEpic_ShouldReturnCorrectEpic() {
        assertEquals(epic.getId(), subtask1.getEpicId(), "Epic установленный в Subtask1 не равен полученному");
        assertEquals(epic.getId(), subtask2.getEpicId(), "Epic установленный в Subtask2 не равен полученному");
    }
}