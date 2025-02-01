package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManager {
    private static int taskCounter = 0;

    private Map<Integer, Epic> epics = new HashMap<>();
    private Map<Integer, Task> tasks = new HashMap<>();
    private Map<Integer, Subtask> subtasks = new HashMap<>();

    public Map<Integer, Epic> getEpics() {
        return epics;
    }

    public Map<Integer, Task> getTasks() {
        return tasks;
    }

    public Map<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public void addEpic(Epic ... epic) {
        for (Epic e : epic) {
            epics.put(e.getId(), e);
        }
    }

    public void addTask(Task ... task) {
        for (Task t : task) {
            tasks.put(t.getId(), t);
        }
    }

    public void addSubtask(Subtask ... subtask) {
        for (Subtask s : subtask) {
            subtasks.put(s.getId(), s);
        }
    }

    public Epic removeEpic(int id) {
        Epic epic = epics.remove(id);
        List<Integer> subtaskIdList = epic.getSubtasks().values().stream().map(subtask -> subtask.getId()).toList();

        for (int subtaskId : subtaskIdList) {
            subtasks.remove(subtaskId);
        }

        return epic;
    }

    public Task removeTask(int id) {
        return tasks.remove(id);
    }

    public Subtask removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        subtask.getEpic().removeSubtask(subtask);
        return subtask;
    }

    public Map<Integer, Subtask> getAllSubtasksByEpic(Epic epic) {
        return epic.getSubtasks();
    }

    public void clearEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearSubtasks() {
        for(Epic epic : epics.values()) {
            epic.getSubtasks().clear();
        }

        subtasks.clear();
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        subtask.getEpic().checkStatus();
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public static int incrementAndGetTaskCounter() {
        return ++taskCounter;
    }

}
