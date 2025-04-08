package manager;

import exception.TaskOverlappingException;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    private final HistoryManager historyManager;

    private int taskId;
    private final Set<Integer> taskIds = new HashSet<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Comparator<Task> taskComparator = Comparator.comparing(Task::getStartTime);
    private final Set<Task> prioritizedTasks = new TreeSet<>(taskComparator);

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

            if (!isValidDateTime(task)) {
                tasks.put(task.getId(), task);
                taskIds.add(task.getId());
            } else if (!isOverlapping(task)) {
                tasks.put(task.getId(), task);
                taskIds.add(task.getId());
                prioritizedTasks.add(task);
            } else {
                throw new TaskOverlappingException("Указанное время задач пересекается с другими задачами или задано некорректно");
            }

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

            if (!isValidDateTime(subtask)) {
                subtasks.put(subtask.getId(), subtask);
                taskIds.add(subtask.getId());
            } else if (!isOverlapping(subtask)) {
                subtasks.put(subtask.getId(), subtask);
                taskIds.add(subtask.getId());
                prioritizedTasks.add(subtask);
            } else {
                throw new TaskOverlappingException("Указанное время задач пересекается с другими задачами или задано некорректно");
            }

            Integer epicId = subtask.getEpicId();

            if (epicId != null) {
                Epic epic = epics.get(epicId);
                epic.addOrUpdateSubtask(subtask);
                checkEpicStatus(epic);
                calculateTimeAndDurationOfEpic(epic);
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
                    prioritizedTasks.remove(subtasks.get(subtaskId));
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
        prioritizedTasks.remove(task);
        return task;
    }

    @Override
    public Subtask removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        historyManager.remove(id);
        taskIds.remove(id);
        prioritizedTasks.remove(subtask);

        Epic epic = epics.get(subtask.getEpicId());
        epic.removeSubtask(subtask);
        checkEpicStatus(epic);
        calculateTimeAndDurationOfEpic(epic);

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
            prioritizedTasks.remove(subtask);
        });

        epics.clear();
        subtasks.clear();
    }

    @Override
    public void clearTasks() {
        tasks.values().forEach(task -> {
            historyManager.remove(task.getId());
            taskIds.remove(task.getId());
            prioritizedTasks.remove(task);
        });

        tasks.clear();
    }

    @Override
    public void clearSubtasks() {
        epics.values().forEach(
                epic -> {
                    epic.getSubtasksId().clear();
                    checkEpicStatus(epic);
                    calculateTimeAndDurationOfEpic(epic);
                });

        subtasks.values().forEach(subtask -> {
            historyManager.remove(subtask.getId());
            taskIds.remove(subtask.getId());
            prioritizedTasks.remove(subtask);
        });

        subtasks.clear();
    }

    @Override
    public void updateEpic(Epic epic) {
        checkEpicStatus(epic);
        calculateTimeAndDurationOfEpic(epic);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateTask(Task task) {
        Task taskBeforeUpdate = tasks.get(task.getId());
        boolean isInPrioritizedList = prioritizedTasks.contains(taskBeforeUpdate);

        if (isInPrioritizedList) {
            prioritizedTasks.remove(taskBeforeUpdate);
        }

        if (!isValidDateTime(task)) {
            tasks.put(task.getId(), task);
        } else if (!isOverlapping(task)) {
            tasks.put(task.getId(), task);
            prioritizedTasks.add(task);
        } else {
            prioritizedTasks.add(taskBeforeUpdate);
            throw new TaskOverlappingException("Указанное время задач пересекается с другими задачами или задано некорректно");
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask subtaskBeforeUpdate = subtasks.get(subtask.getId());
        boolean isInPrioritizedList = prioritizedTasks.contains(subtaskBeforeUpdate);

        if (isInPrioritizedList) {
            prioritizedTasks.remove(subtaskBeforeUpdate);
        }

        if (!isValidDateTime(subtask)) {
            subtasks.put(subtask.getId(), subtask);
        } else if (!isOverlapping(subtask)) {
            subtasks.put(subtask.getId(), subtask);
            prioritizedTasks.add(subtask);
        } else {
            prioritizedTasks.add(subtaskBeforeUpdate);
            throw new TaskOverlappingException("Указанное время задач пересекается с другими задачами или задано некорректно");
        }

        checkEpicStatus(epics.get(subtask.getEpicId()));
        calculateTimeAndDurationOfEpic(epics.get(subtask.getEpicId()));
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

    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream().toList();
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

    private void calculateTimeAndDurationOfEpic(Epic epic) {
        List<Subtask> subtaskList = prioritizedTasks.stream()
                .filter(t -> epic.getSubtasksId().contains(t.getId()))
                .map(s -> (Subtask) s)
                .toList();

        if (!subtaskList.isEmpty()) {
            LocalDateTime startTime = subtaskList.stream()
                    .min(taskComparator).get().getStartTime();

            Duration duration = subtaskList.stream()
                    .map(Subtask::getDuration)
                    .reduce(Duration.ZERO, Duration::plus);

            LocalDateTime endTime = subtaskList.stream()
                    .max(Comparator.comparing(Subtask::getEndTime)).get().getEndTime();

            epic.setStartTime(startTime);
            epic.setDuration(duration);
            epic.setEndTime(endTime);
        } else {
            epic.setStartTime(LocalDateTime.MIN);
            epic.setDuration(Duration.ZERO);
            epic.setEndTime(null);
        }
    }

    private boolean isOverlapping(Task task) {
        if (prioritizedTasks.isEmpty()) return false;

        LocalDateTime startTimeTask = task.getStartTime();
        LocalDateTime endTimeTask = task.getEndTime();

        Map<LocalDateTime, LocalDateTime> startToEndTimeMap = prioritizedTasks.stream()
                .collect(Collectors.toMap(Task::getStartTime, Task::getEndTime));

        for (Map.Entry<LocalDateTime, LocalDateTime> entry : startToEndTimeMap.entrySet()) {
            boolean notOverlap = entry.getKey().isAfter(endTimeTask) || entry.getValue().isBefore(startTimeTask);
            if (!notOverlap) {
                return true;
            }
        }

        return false;
    }

    private static boolean isValidDateTime(Task task) {
        return task.getStartTime() != null && task.getStartTime() != LocalDateTime.MIN;
    }
}
