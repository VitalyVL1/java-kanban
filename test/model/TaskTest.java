package model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private static Task task1;
    private static Task task2;
    private static LocalDateTime startTime;
    private static Duration duration;

    @BeforeAll
    static void setUpBeforeClass() {
        startTime = LocalDateTime.now();
        duration = Duration.ofMinutes(15);
    }

    @BeforeEach
    void setUpBeforeEach() {
        task1 = new Task("Task1", "Description Task1", startTime.plusMinutes(10), duration.plusMinutes(10));
        task2 = new Task("Task2", "Description Task2", startTime.plusMinutes(30), duration.plusMinutes(5));
    }

    @Test
    void testTaskEquals_ShouldReturnTrue() {
        assertEquals(task1, task2, "сравнение с id = null, должен быть true");

        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2, "Tasks не равны друг другу");

        Epic epic = new Epic("Epic", "Description Epic");
        Subtask subtask = new Subtask("Subtask", "Description Subtask", epic, startTime.minusHours(1), duration.plusMinutes(5));

        epic.setId(1);
        subtask.setId(1);

        assertNotEquals(task1, epic, "Объекты Task и Epic не должны быть равными");
        assertNotEquals(task1, subtask, "Объекты Task и Subtask не должны быть равными");
    }

    @Test
    void testTaskHashCode_ShouldReturnCorrectHashCode() {
        task1.setId(1);
        task2.setId(1);

        assertEquals(task1.hashCode(), task2.hashCode(), "Task hashCode для равных объектов не равен");
    }

    @Test
    void testNullIdForNewTask_ShouldReturnNullWhenTaskIsNew() {
        assertNull(task1.getId(), "Для нового объекта без указания id, id не равен null");
    }

    @Test
    void testTaskSetAndGetId_ShouldReturnCorrectId() {
        int id = 1;
        task1.setId(id);

        assertEquals(id, task1.getId(), "Возвращенный id не совпадает с установленным");
    }

    @Test
    void shouldReturnStatusNewForNewTask() {
        assertEquals(TaskStatus.NEW, task1.getStatus(), "Статус не равен NEW для нового объекта");
    }

    @Test
    void testSetAndGetStatus_ShouldReturnCorrectStatusForTask() {
        TaskStatus status = TaskStatus.IN_PROGRESS;

        task1.setStatus(status);
        assertEquals(status, task1.getStatus(), "Статус не равен установленному");

        status = TaskStatus.IN_PROGRESS;
        task1.setStatus(status);
        assertEquals(status, task1.getStatus(), "Статус не равен установленному");

        status = TaskStatus.NEW;
        task1.setStatus(status);
        assertEquals(status, task1.getStatus(), "Статус не равен установленному");
    }

    @Test
    void testSetAndGetDescription_ShouldReturnCorrectDescriptionForNewTask() {
        String taskDescription = "DescriptionTest1";
        String taskTitle = "TaskTest1";

        task1.setDescription(taskDescription);
        task1.setTitle(taskTitle);

        assertEquals(taskTitle, task1.getTitle(), "Title не соответствует установленному");
        assertEquals(taskDescription, task1.getDescription(), "Description не соответствует установленному");
    }

}