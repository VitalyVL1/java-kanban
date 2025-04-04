package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
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

            subtasks.put(subtask.getId(), subtask);
            taskIds.add(subtask.getId());

            Integer epicId = subtask.getEpicId();

            if (epicId != null) {
                Epic epic = epics.get(epicId);
                epic.addOrUpdateSubtask(subtask);
                checkEpicStatus(epic);
                calculateStartAndEndTimeOfEpic(epic);
            }

            return subtask.getId();
        }
        return -1;
    }

    @Override
    public Epic removeEpic(int id) {
        Epic epic = epics.remove(id);

        historyManager.remove(id);
        taskIds.remove(id);

        epic.getSubtasksId().forEach(
                subtaskId -> {
                    historyManager.remove(subtaskId);
                    subtasks.remove(subtaskId);
                    taskIds.remove(subtaskId);
                });

        return epic;
    }

    @Override
    public Task removeTask(int id) {
        Task task = tasks.remove(id);
        historyManager.remove(id);
        taskIds.remove(id);
        return task;
    }

    @Override
    public Subtask removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        historyManager.remove(id);
        taskIds.remove(id);

        Epic epic = epics.get(subtask.getEpicId());
        epic.removeSubtask(subtask);
        checkEpicStatus(epic);
        calculateStartAndEndTimeOfEpic(epic);

        return subtask;
    }

    @Override
    public List<Subtask> getAllSubtasksByEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            return new ArrayList<>();
        }

        List<Integer> subtasksId = epic.getSubtasksId().stream().toList();

        return subtasks.values().stream().filter(subtask -> subtasksId.contains(subtask.getId())).toList();
    }

    @Override
    public void clearEpics() {
        epics.values().forEach(epic -> {
            historyManager.remove(epic.getId());
            taskIds.remove(epic.getId());
        });

        subtasks.values().forEach(subtask -> {
            historyManager.remove(subtask.getId());
            taskIds.remove(subtask.getId());
        });

        epics.clear();
        subtasks.clear();
    }

    @Override
    public void clearTasks() {
        tasks.values().forEach(task -> {
            historyManager.remove(task.getId());
            taskIds.remove(task.getId());
        });

        tasks.clear();
    }

    @Override
    public void clearSubtasks() {
        epics.values().forEach(
                epic -> {
                    epic.getSubtasksId().clear();
                    checkEpicStatus(epic);
                    calculateStartAndEndTimeOfEpic(epic);
                });

        subtasks.values().forEach(subtask -> {
            historyManager.remove(subtask.getId());
            taskIds.remove(subtask.getId());
        });

        subtasks.clear();
    }

    @Override
    public void updateEpic(Epic epic) {
        checkEpicStatus(epic);
        calculateStartAndEndTimeOfEpic(epic);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        checkEpicStatus(epics.get(subtask.getEpicId()));
        calculateStartAndEndTimeOfEpic(epics.get(subtask.getEpicId()));
    }

    @Override
    public Task getTask(int id) {
        Optional<Task> taskOptional = Optional.ofNullable(tasks.get(id));

        if (taskOptional.isPresent()) {
            Task task = new Task(taskOptional.get());
            historyManager.add(task);
            return task;
        }

        return null;
    }

    @Override
    public Epic getEpic(int id) {
        Optional<Epic> epicOptional = Optional.ofNullable(epics.get(id));

        if (epicOptional.isPresent()) {
            Epic epic = new Epic(epicOptional.get());
            historyManager.add(epic);
            return epic;
        }

        return null;
    }

    @Override
    public Subtask getSubtask(int id) {
        Optional<Subtask> subtaskOptional = Optional.ofNullable(subtasks.get(id));

        if (subtaskOptional.isPresent()) {
            Subtask subtask = new Subtask(subtaskOptional.get());
            historyManager.add(subtask);
            return subtask;
        }

        return null;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void checkEpicStatus(Epic epic) {
        if (epic == null) return;

        List<Subtask> subtaskList = subtasks.values().stream()
                .filter(subtask -> epic.getSubtasksId().contains(subtask.getId()))
                .toList();

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

    private void calculateStartAndEndTimeOfEpic(Epic epic) {
        List<Subtask> subtaskList = getAllSubtasksByEpic(epic);

        if (!subtaskList.isEmpty()) {
            LocalDateTime startTime = subtaskList.stream()
                    .min(Task::compareTo).get().getStartTime();

            Duration duration = subtaskList.stream()
                    .map(Subtask::getDuration)
                    .reduce(Duration.ZERO, Duration::plus);

            LocalDateTime endTime = startTime.plus(duration);

            epic.setStartTime(startTime);
            epic.setDuration(duration);
            epic.setEndTime(endTime);
        }
    }

}
