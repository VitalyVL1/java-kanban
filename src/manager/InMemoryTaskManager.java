package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private final HistoryManager historyManager;

    private int taskId;
    private final Set<Integer> taskIds = new HashSet<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    public InMemoryTaskManager(HistoryManager historyManager) {
        taskId = 1;
        this.historyManager = historyManager;
    }

    @Override
    public List<Epic> getEpics() {
        return epics.values().stream().map(Epic::new).toList();
    }

    @Override
    public List<Task> getTasks() {
        return tasks.values().stream().map(Task::new).toList();
    }

    @Override
    public List<Subtask> getSubtasks() {
        return subtasks.values().stream().map(Subtask::new).toList();
    }

    @Override
    public int addEpic(Epic epic) {
        if (epic != null && !taskIds.contains(epic.getId())) {
            if (epic.getId() == null) {
                int id = generateId();
                epic.setId(id);
            }

            epics.put(epic.getId(), epic);
            taskIds.add(epic.getId());
            return epic.getId();
        }

        return -1;
    }

    @Override
    public int addTask(Task task) {
        if (task != null && !taskIds.contains(task.getId())) {
            if (task.getId() == null) {
                int id = generateId();
                task.setId(id);
            }

            tasks.put(task.getId(), task);
            taskIds.add(task.getId());

            return task.getId();
        }

        return -1;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        if (subtask != null && !taskIds.contains(subtask.getId())) {
            if (subtask.getId() == null) {
                int id = generateId();
                subtask.setId(id);
            }
            Epic epic = subtask.getEpic();

            if (epic != null) {
                epic.addOrUpdateSubtask(subtask);
                checkEpicStatus(epic);
            }

            subtasks.put(subtask.getId(), subtask);
            taskIds.add(subtask.getId());

            return subtask.getId();
        }
        return -1;
    }

    @Override
    public Epic removeEpic(int id) {
        Epic epic = epics.remove(id);

        historyManager.remove(id);

        epic.getSubtasks().values().forEach(
                subtask -> {
                    historyManager.remove(subtask.getId());
                    subtasks.remove(subtask.getId());
                });

        return epic;
    }

    @Override
    public Task removeTask(int id) {
        Task task = tasks.remove(id);
        historyManager.remove(id);
        return task;
    }

    @Override
    public Subtask removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        historyManager.remove(id);

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

        return epic.getSubtasks().values().stream().map(Subtask::new).toList();
    }

    @Override
    public void clearEpics() {
        epics.values().forEach(epic -> historyManager.remove(epic.getId()));

        subtasks.values().forEach(subtask -> historyManager.remove(subtask.getId()));

        epics.clear();
        subtasks.clear();
    }

    @Override
    public void clearTasks() {
        tasks.values().forEach(task -> historyManager.remove(task.getId()));

        tasks.clear();
    }

    @Override
    public void clearSubtasks() {
        epics.values().forEach(
                epic -> {
                    epic.getSubtasks().clear();
                    checkEpicStatus(epic);
                });

        subtasks.values().forEach(subtask -> historyManager.remove(subtask.getId()));

        subtasks.clear();
    }

    @Override
    public void updateEpic(Epic epic) {
        checkEpicStatus(epic);

        epic.getSubtasks().values().forEach(subtask -> subtasks.put(subtask.getId(), subtask));

        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpic().getId());

        if (epic != null) {
            epic.addOrUpdateSubtask(subtask);
        }

        subtask.setEpic(epic);
        subtasks.put(subtask.getId(), subtask);
        checkEpicStatus(subtask.getEpic());
    }

    @Override
    public Task getTask(int id) {
        Task task = new Task(tasks.get(id));
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = new Epic(epics.get(id));
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = new Subtask(subtasks.get(id));
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void checkEpicStatus(Epic epic) {
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

    private int generateId() {
        while (taskIds.contains(taskId)) {
            taskId++;
        }

        return taskId;
    }

}
