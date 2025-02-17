package service;

import manager.HistoryManager;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {

    private int taskCounter = 0;
    private final HistoryManager historyManager;

    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public List<Epic> getEpics() {
        return epics.values().stream().toList();
    }

    @Override
    public List<Task> getTasks() {
        return tasks.values().stream().toList();
    }

    @Override
    public List<Subtask> getSubtasks() {
        return subtasks.values().stream().toList();
    }

    @Override
    public int addEpic(Epic epic) {
        int id = ++taskCounter;

        epic.setId(id);
        epics.put(epic.getId(), epic);

        return id;
    }

    @Override
    public int addTask(Task task) {
        int id = ++taskCounter;

        task.setId(id);
        tasks.put(task.getId(), task);

        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        Epic epic = subtask.getEpic();
        int id = ++taskCounter;

        subtask.setId(id);
        epic.addOrUpdateSubtask(subtask);
        checkEpicStatus(epic);
        subtasks.put(subtask.getId(), subtask);

        return id;
    }

    @Override
    public Epic removeEpic(int id) {
        Epic epic = epics.remove(id);
        List<Integer> subtaskIdList = epic.getSubtasks().values().stream().map(Task::getId).toList();

        for (int subtaskId : subtaskIdList) {
            subtasks.remove(subtaskId);
        }

        return epic;
    }

    @Override
    public Task removeTask(int id) {
        return tasks.remove(id);
    }

    @Override
    public Subtask removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        Epic epic = subtask.getEpic();
        epic.removeSubtask(subtask);
        checkEpicStatus(epic);
        return subtask;
    }

    @Override
    public List<Subtask> getAllSubtasksByEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            return new ArrayList<>();
        }

        return epic.getSubtasks().values().stream().toList();
    }

    @Override
    public void clearEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void clearTasks() {
        tasks.clear();
    }

    @Override
    public void clearSubtasks() {
        List<Epic> epicList = epics.values().stream().toList();

        for (Epic epic : epicList) {
            epic.getSubtasks().clear();
            checkEpicStatus(epic);
        }

        subtasks.clear();
    }

    @Override
    public void updateEpic(Epic epic) {
        checkEpicStatus(epic);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        checkEpicStatus(subtask.getEpic());
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
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
