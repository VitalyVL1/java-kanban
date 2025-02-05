package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

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

    public void addEpic(Epic... epic) {
        for (Epic e : epic) {
            e.setId(++taskCounter);
            epics.put(e.getId(), e);
        }
    }

    public void addTask(Task... task) {
        for (Task t: task) {
            t.setId(++taskCounter);
            tasks.put(t.getId(), t);
        }
    }

    public void addSubtask(Subtask... subtask) {
        for (Subtask s: subtask) {
            Epic epic = s.getEpic();
            s.setId(++taskCounter);
            epic.addOrUpdateSubtask(s);
            checkEpicStatus(epic);
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
        Epic epic = subtask.getEpic();
        epic.removeSubtask(subtask);
        checkEpicStatus(epic);
        return subtask;
    }

    public Map<Integer, Subtask> getAllSubtasksByEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            return new HashMap<>();
        }

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
        List<Epic> epicList = epics.values().stream().toList();

        for (Epic epic : epicList) {
            epic.getSubtasks().clear();
            checkEpicStatus(epic);
        }

        subtasks.clear();
    }

    public void updateEpic(Epic epic) {
        checkEpicStatus(epic);
        epics.put(epic.getId(), epic);
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        checkEpicStatus(subtask.getEpic());
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

    public void checkEpicStatus(Epic epic) {
        if (epic == null) return;

        List<Subtask> subtaskList = epic.getSubtasks().values().stream().toList();

        int subtasksCount = subtaskList.size();

        if (subtasksCount == 0) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        long doneCount = subtaskList.stream().filter(subtask -> subtask.getStatus() == TaskStatus.DONE).count();

        long newCount = subtaskList.stream().filter(subtask -> subtask.getStatus() == TaskStatus.NEW).count();

        if (doneCount == subtasksCount) {
            epic.setStatus(TaskStatus.DONE);
        } else if (newCount == subtasksCount) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

}
