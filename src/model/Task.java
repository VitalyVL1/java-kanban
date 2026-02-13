package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Базовый класс для всех типов задач в системе управления задачами.
 *
 * <p>Содержит общие поля и методы, характерные для любой задачи:
 * идентификатор, название, описание, статус выполнения, временные параметры.
 * Является родительским классом для {@link Epic} и {@link Subtask}.</p>
 *
 * <p>Временные параметры задачи:</p>
 * <ul>
 *   <li><b>startTime</b> - дата и время начала выполнения задачи</li>
 *   <li><b>duration</b> - продолжительность выполнения задачи</li>
 *   <li><b>endTime</b> - вычисляемое время окончания (startTime + duration)</li>
 * </ul>
 *
 * <p>Идентификация задач выполняется по полю {@link #id}. Две задачи считаются
 * равными, если у них одинаковый идентификатор (см. {@link #equals(Object)}).</p>
 *
 * @see Epic
 * @see Subtask
 * @see TaskStatus
 * @see TaskType
 */
public class Task {

    /** Уникальный идентификатор задачи */
    private Integer id;

    /** Название задачи */
    private String title;

    /** Подробное описание задачи */
    private String description;

    /** Текущий статус выполнения задачи */
    private TaskStatus status;

    /** Тип задачи (TASK, EPIC, SUBTASK) */
    private TaskType type;

    /** Продолжительность выполнения задачи */
    private Duration duration;

    /** Дата и время начала выполнения задачи */
    private LocalDateTime startTime;

    /**
     * Создает новую задачу с указанными параметрами.
     *
     * <p>Статус задачи по умолчанию устанавливается в {@link TaskStatus#NEW},
     * тип - в {@link TaskType#TASK}. Идентификатор будет присвоен позже
     * при добавлении задачи в менеджер.</p>
     *
     * @param title название задачи
     * @param description описание задачи
     * @param startTime время начала выполнения (может быть null)
     * @param duration длительность выполнения
     */
    public Task(String title, String description, LocalDateTime startTime, Duration duration) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;
        status = TaskStatus.NEW;
        type = TaskType.TASK;
    }

    /**
     * Создает копию существующей задачи.
     *
     * <p>Конструктор копирования для создания защищенных копий задач.
     * Используется в менеджере истории и при возврате задач из методов,
     * чтобы предотвратить внешние модификации оригиналов.</p>
     *
     * @param task задача для копирования
     */
    public Task(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.type = task.getType();
        this.startTime = task.getStartTime();
        this.duration = task.getDuration();
    }

    /**
     * Создает задачу со всеми параметрами.
     *
     * <p>Используется при десериализации из файла или других источников,
     * когда все поля задачи уже известны.</p>
     *
     * @param id идентификатор задачи
     * @param title название задачи
     * @param description описание задачи
     * @param status статус задачи
     * @param type тип задачи
     * @param startTime время начала выполнения
     * @param duration длительность выполнения
     */
    public Task(Integer id, String title, String description, TaskStatus status, TaskType type, LocalDateTime startTime, Duration duration) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.type = type;
        this.startTime = startTime;
        this.duration = duration;
    }

    /**
     * Возвращает идентификатор задачи.
     *
     * @return идентификатор задачи или null, если задача ещё не сохранена
     */
    public Integer getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор задачи.
     *
     * @param id новый идентификатор
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Возвращает название задачи.
     *
     * @return название задачи
     */
    public String getTitle() {
        return title;
    }

    /**
     * Устанавливает название задачи.
     *
     * @param title новое название
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Возвращает описание задачи.
     *
     * @return описание задачи
     */
    public String getDescription() {
        return description;
    }

    /**
     * Устанавливает описание задачи.
     *
     * @param description новое описание
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Возвращает текущий статус задачи.
     *
     * @return статус выполнения
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Устанавливает новый статус задачи.
     *
     * @param status новый статус
     */
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    /**
     * Сравнивает задачи по идентификатору.
     *
     * <p>Две задачи считаются равными, если у них одинаковый ID.
     * Это позволяет корректно работать с задачами в коллекциях.</p>
     *
     * @param object объект для сравнения
     * @return true если задачи имеют одинаковый ID
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Task task = (Task) object;
        return Objects.equals(id, task.id);
    }

    /**
     * Возвращает хеш-код задачи на основе идентификатора.
     *
     * @return хеш-код задачи
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    /**
     * Возвращает строковое представление задачи.
     *
     * @return строка с данными задачи
     */
    @Override
    public String toString() {
        return "Task{" +
               "id=" + id +
               ", title='" + title + '\'' +
               ", description='" + description + '\'' +
               ", status=" + status +
               ", type=" + type +
               ", duration=" + duration +
               ", startTime=" + startTime +
               '}';
    }

    /**
     * Возвращает тип задачи.
     *
     * @return тип задачи (TASK, EPIC или SUBTASK)
     */
    public TaskType getType() {
        return type;
    }

    /**
     * Устанавливает тип задачи.
     *
     * @param type новый тип задачи
     */
    public void setType(TaskType type) {
        this.type = type;
    }

    /**
     * Возвращает длительность выполнения задачи.
     *
     * @return длительность задачи
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Устанавливает длительность выполнения задачи.
     *
     * @param duration новая длительность
     */
    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    /**
     * Возвращает время начала выполнения задачи.
     *
     * @return время начала или null, если не задано
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Устанавливает время начала выполнения задачи.
     *
     * @param startTime новое время начала
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Вычисляет и возвращает время окончания задачи.
     *
     * <p>Время окончания вычисляется как {@code startTime + duration}.
     * Если время начала не задано (null), возвращает null.</p>
     *
     * @return время окончания задачи или null
     */
    public LocalDateTime getEndTime() {
        return startTime == null ? null : startTime.plus(duration);
    }
}