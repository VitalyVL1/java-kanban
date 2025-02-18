package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private static Task task1 = new Task("Task1", "Description Task1");
    private static Task task2 = new Task("Task2", "Description Task2");

    @BeforeEach
    void setUpBeforeEach() {
        task1 = new Task("Task1", "Description Task1");
        task2 = new Task("Task2", "Description Task2");
    }

    @Test
    void testTaskEquals_ShouldReturnTrue() {
        task1.setId(1);
        task2.setId(1);

        assertTrue(task1.equals(task2), "Tasks не равны друг другу");

        Epic epic = new Epic("Epic", "Description Epic");
        Subtask subtask = new Subtask("Subtask", "Description Subtask", epic);

        epic.setId(1);
        subtask.setId(1);

        assertFalse(task1.equals(epic),"Объекты Task и Epic не должны быть равными");
        assertFalse(task1.equals(subtask),"Объекты Task и Subtask не должны быть равными");
    }

    @Test
    void testTaskHashCode_ShouldReturnCorrectHashCode() {
        task1.setId(1);
        task2.setId(1);

        assertEquals(task1.hashCode(), task2.hashCode(), "Task hashCode для равных объектов не равен");
    }

    @Test
    void testNullIdForNewTask_ShouldReturnNullWhenTaskIsNew() {
        assertNull(task1.getId(),"Для нового объекта без указания id, id не равен null");
    }

    @Test
    void testTaskSetAndGetId_ShouldReturnCorrectId() {
        int id = 1;
        task1.setId(id);

        assertEquals(id,task1.getId(),"Возвращенный id не совпадает с установленным");
    }

    @Test
    void shouldReturnStatusNewForNewTask() {
        assertEquals(TaskStatus.NEW, task1.getStatus(),"Статус не равен NEW для нового объекта");
    }

    @Test
    void testSetAndGetStatus_ShouldReturnCorrectStatusForTask() {
        TaskStatus status = TaskStatus.IN_PROGRESS;

        task1.setStatus(status);
        assertEquals(status, task1.getStatus(),"Статус не равен установленному");

        status = TaskStatus.IN_PROGRESS;
        task1.setStatus(status);
        assertEquals(status, task1.getStatus(),"Статус не равен установленному");

        status = TaskStatus.NEW;
        task1.setStatus(status);
        assertEquals(status, task1.getStatus(),"Статус не равен установленному");
    }

    @Test
    void testSetAndGetDescription_ShouldReturnCorrectDescriptionForNewTask() {
        String taskDescription = "DescriptionTest1";
        String taskTitle = "TaskTest1";

        task1.setDescription(taskDescription);
        task1.setTitle(taskTitle);

        assertEquals(taskTitle, task1.getTitle(),"Title не соответствует установленному");
        assertEquals(taskDescription, task1.getDescription(),"Description не соответствует установленному");
    }

}