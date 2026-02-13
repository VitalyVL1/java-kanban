package manager;

import exception.NotFoundException;
import exception.TaskOverlappingException;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация менеджера задач, хранящая все данные в оперативной памяти.
 *
 * <p>Обеспечивает полный функционал управления задачами, эпиками и подзадачами
 * с проверкой временных пересечений и автоматическим обновлением статусов эпиков.
 * Все данные хранятся в коллекциях HashMap, история просмотров делегируется
 * {@link HistoryManager}.</p>
 *
 * <p>Ключевые особенности:</p>
 * <ul>
 *   <li><b>Проверка пересечений</b> - задачи не могут пересекаться по времени выполнения</li>
 *   <li><b>Приоритеты</b> - задачи автоматически сортируются по времени начала</li>
 *   <li><b>Статусы эпиков</b> - автоматически вычисляются на основе статусов подзадач</li>
 *   <li><b>Временные параметры эпиков</b> - вычисляются как сумма/диапазон подзадач</li>
 *   <li><b>Защитные копии</b> - при получении задач возвращаются копии для избежания внешних модификаций</li>
 * </ul>
 *
 * @see TaskManager
 * @see HistoryManager
 * @see model.Task
 * @see model.Epic
 * @see model.Subtask
 */
public class InMemoryTaskManager implements TaskManager {

    private final HistoryManager historyManager;

    private int taskId;
    private final Set<Integer> taskIds = new HashSet<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Comparator<Task> taskComparator = Comparator.comparing(Task::getStartTime);
    private final Set<Task> prioritizedTasks = new TreeSet<>(taskComparator);

    /**
     * Создает менеджер с указанным менеджером истории.
     *
     * @param historyManager менеджер для управления историей просмотров
     */
    public InMemoryTaskManager(HistoryManager historyManager) {
        taskId = 1;
        this.historyManager = historyManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Epic> getEpics() {
        return epics.values().stream()
                .map(Epic::new)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Task> getTasks() {
        return tasks.values().stream()
                .map(Task::new)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Subtask> getSubtasks() {
        return subtasks.values().stream()
                .map(Subtask::new)
                .toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Эпики не участвуют в проверке временных пересечений и не добавляются
     * в список приоритетных задач.</p>
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>При добавлении задачи проверяется пересечение по времени с существующими
     * задачами в {@link #prioritizedTasks}. Валидные задачи добавляются в
     * отсортированный список приоритетов.</p>
     *
     * @throws TaskOverlappingException если задача пересекается по времени
     */
    @Override
    public int addTask(Task task) {
        if (task != null && !taskIds.contains(task.getId())) {
            if (task.getId() == null) {
                int id = generateId();
                task.setId(id);
            }

            if (isInvalidDateTime(task)) {
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

    /**
     * {@inheritDoc}
     *
     * <p>При добавлении подзадачи выполняются дополнительные действия:</p>
     * <ul>
     *   <li>Проверка существования эпика</li>
     *   <li>Добавление подзадачи в эпик</li>
     *   <li>Пересчет статуса эпика</li>
     *   <li>Пересчет временных параметров эпика</li>
     * </ul>
     *
     * @throws NotFoundException если указанный эпик не существует
     * @throws TaskOverlappingException если подзадача пересекается по времени
     */
    @Override
    public int addSubtask(Subtask subtask) {
        if (subtask != null && !taskIds.contains(subtask.getId())) {
            if (subtask.getId() == null) {
                int id = generateId();
                subtask.setId(id);
            }

            if (isInvalidDateTime(subtask)) {
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

            if (epicId == null || !epics.containsKey(epicId)) {
                throw new NotFoundException("Epic c id = " + epicId + " отсутствует");
            } else {
                Epic epic = epics.get(epicId);
                epic.addOrUpdateSubtask(subtask);
                checkEpicStatus(epic);
                calculateTimeAndDurationOfEpic(epic);
            }

            return subtask.getId();
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     *
     * <p>При удалении эпика также удаляются все его подзадачи из всех хранилищ
     * и из истории просмотров.</p>
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Task removeTask(int id) {
        Task task = tasks.remove(id);
        historyManager.remove(id);
        taskIds.remove(id);
        prioritizedTasks.remove(task);
        return task;
    }

    /**
     * {@inheritDoc}
     *
     * <p>При удалении подзадачи обновляются статус и временные параметры
     * соответствующего эпика.</p>
     */
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

    /**
     * {@inheritDoc}
     *
     * @throws NotFoundException если эпик с указанным id не найден
     */
    @Override
    public List<Subtask> getAllSubtasksByEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            throw new NotFoundException("Epic с id = " + epic.getId() + " не найден!");
        }

        List<Integer> subtasksId = epic.getSubtasksId().stream().toList();

        return subtasks.values().stream()
                .filter(subtask -> subtasksId.contains(subtask.getId()))
                .toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>При удалении всех эпиков также удаляются все подзадачи.</p>
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearTasks() {
        tasks.values().forEach(task -> {
            historyManager.remove(task.getId());
            taskIds.remove(task.getId());
            prioritizedTasks.remove(task);
        });

        tasks.clear();
    }

    /**
     * {@inheritDoc}
     *
     * <p>При удалении всех подзадач обновляются статусы и временные параметры
     * всех эпиков.</p>
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>При обновлении эпика пересчитываются его статус и временные параметры.</p>
     */
    @Override
    public void updateEpic(Epic epic) {
        checkEpicStatus(epic);
        calculateTimeAndDurationOfEpic(epic);
        epics.put(epic.getId(), epic);
    }

    /**
     * {@inheritDoc}
     *
     * <p>При обновлении задачи проверяется временное пересечение с учетом
     * исключения самой обновляемой задачи из проверки.</p>
     *
     * @throws TaskOverlappingException если обновленная задача пересекается по времени
     */
    @Override
    public void updateTask(Task task) {
        Task taskBeforeUpdate = tasks.get(task.getId());
        boolean isInPrioritizedList = prioritizedTasks.contains(taskBeforeUpdate);

        if (isInPrioritizedList) {
            prioritizedTasks.remove(taskBeforeUpdate);
        }

        if (isInvalidDateTime(task)) {
            tasks.put(task.getId(), task);
        } else if (!isOverlapping(task)) {
            tasks.put(task.getId(), task);
            prioritizedTasks.add(task);
        } else {
            prioritizedTasks.add(taskBeforeUpdate);
            throw new TaskOverlappingException("Указанное время задач пересекается с другими задачами или задано некорректно");
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>При обновлении подзадачи дополнительно обновляются статус и временные
     * параметры соответствующего эпика.</p>
     *
     * @throws TaskOverlappingException если обновленная подзадача пересекается по времени
     */
    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask subtaskBeforeUpdate = subtasks.get(subtask.getId());
        boolean isInPrioritizedList = prioritizedTasks.contains(subtaskBeforeUpdate);

        if (isInPrioritizedList) {
            prioritizedTasks.remove(subtaskBeforeUpdate);
        }

        if (isInvalidDateTime(subtask)) {
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

    /**
     * {@inheritDoc}
     *
     * @throws NotFoundException если задача не найдена
     */
    @Override
    public Task getTask(int id) {
        Optional<Task> taskOptional = Optional.ofNullable(tasks.get(id));

        if (taskOptional.isPresent()) {
            Task task = new Task(taskOptional.get());
            historyManager.add(task);
            return task;
        } else {
            throw new NotFoundException("Task с id = " + id + " не найден!");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws NotFoundException если эпик не найден
     */
    @Override
    public Epic getEpic(int id) {
        Optional<Epic> epicOptional = Optional.ofNullable(epics.get(id));

        if (epicOptional.isPresent()) {
            Epic epic = new Epic(epicOptional.get());
            historyManager.add(epic);
            return epic;
        } else {
            throw new NotFoundException("Epic с id = " + id + " не найден!");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws NotFoundException если подзадача не найдена
     */
    @Override
    public Subtask getSubtask(int id) {
        Optional<Subtask> subtaskOptional = Optional.ofNullable(subtasks.get(id));

        if (subtaskOptional.isPresent()) {
            Subtask subtask = new Subtask(subtaskOptional.get());
            historyManager.add(subtask);
            return subtask;
        } else {
            throw new NotFoundException("Subtask с id = " + id + " не найден!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Задачи возвращаются в порядке возрастания времени начала.
     * Задачи без времени начала (null) в этот список не попадают.</p>
     */
    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream().toList();
    }

    /**
     * Обновляет статус эпика на основе статусов его подзадач.
     *
     * <p>Правила вычисления статуса:</p>
     * <ul>
     *   <li>NEW - если все подзадачи NEW или подзадач нет</li>
     *   <li>DONE - если все подзадачи DONE</li>
     *   <li>IN_PROGRESS - во всех остальных случаях</li>
     * </ul>
     *
     * @param epic эпик для обновления статуса
     */
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

        long doneCount = subtaskList.stream()
                .filter(subtask -> subtask.getStatus() == TaskStatus.DONE)
                .count();

        long newCount = subtaskList.stream()
                .filter(subtask -> subtask.getStatus() == TaskStatus.NEW)
                .count();

        if (doneCount == subtasksCount) {
            epic.setStatus(TaskStatus.DONE);
        } else if (newCount == subtasksCount) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    /**
     * Генерирует уникальный идентификатор для новой задачи.
     *
     * @return уникальный идентификатор
     */
    private int generateId() {
        while (taskIds.contains(taskId)) {
            taskId++;
        }

        return taskId;
    }

    /**
     * Вычисляет и устанавливает временные параметры эпика на основе его подзадач.
     *
     * <p>Вычисляемые параметры:</p>
     * <ul>
     *   <li>startTime - самое раннее время начала среди подзадач</li>
     *   <li>duration - сумма длительности всех подзадач</li>
     *   <li>endTime - самое позднее время окончания среди подзадач</li>
     * </ul>
     *
     * @param epic эпик для вычисления временных параметров
     */
    private void calculateTimeAndDurationOfEpic(Epic epic) {
        List<Subtask> subtaskList = prioritizedTasks.stream()
                .filter(t -> epic.getSubtasksId().contains(t.getId()))
                .map(s -> (Subtask) s)
                .toList();

        if (!subtaskList.isEmpty()) {
            LocalDateTime startTime = subtaskList.stream()
                    .min(taskComparator)
                    .get()
                    .getStartTime();

            Duration duration = subtaskList.stream()
                    .map(Subtask::getDuration)
                    .reduce(Duration.ZERO, Duration::plus);

            LocalDateTime endTime = subtaskList.stream()
                    .max(Comparator.comparing(Subtask::getEndTime))
                    .get()
                    .getEndTime();

            epic.setStartTime(startTime);
            epic.setDuration(duration);
            epic.setEndTime(endTime);
        } else {
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
            epic.setEndTime(null);
        }
    }

    /**
     * Проверяет пересечение временного интервала задачи с существующими задачами.
     *
     * @param task задача для проверки
     * @return true если есть пересечение, false если пересечений нет
     */
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

    /**
     * Проверяет, является ли временной интервал задачи некорректным.
     *
     * @param task задача для проверки
     * @return true если время начала не задано, false если задано
     */
    private static boolean isInvalidDateTime(Task task) {
        return task.getStartTime() == null;
    }
}