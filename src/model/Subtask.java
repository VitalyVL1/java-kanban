package model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Класс, представляющий подзадачу, которая принадлежит определенному эпику.
 *
 * <p>Подзадача является частью эпика ({@link Epic}) и не может существовать
 * независимо от него. Статус и временные параметры подзадачи влияют на
 * соответствующие характеристики родительского эпика.</p>
 *
 * <p>Особенности подзадачи:</p>
 * <ul>
 *   <li><b>Принадлежность эпику</b> - каждая подзадача содержит ссылку на ID своего эпика</li>
 *   <li><b>Влияние на эпик</b> - изменения в подзадаче вызывают пересчет статуса и времени эпика</li>
 *   <li><b>Самостоятельность</b> - несмотря на принадлежность эпику, подзадача имеет собственные
 *       статус, время начала и длительность</li>
 * </ul>
 *
 * @see Task
 * @see Epic
 */
public class Subtask extends Task {

    /** Идентификатор эпика, которому принадлежит подзадача */
    private Integer epicId;

    /**
     * Создает новую подзадачу с указанными параметрами.
     *
     * <p>Тип задачи автоматически устанавливается в {@link TaskType#SUBTASK}.
     * Связь с эпиком устанавливается через его ID.</p>
     *
     * @param title название подзадачи
     * @param description описание подзадачи
     * @param epic эпик, которому принадлежит подзадача
     * @param startTime время начала выполнения
     * @param duration длительность выполнения
     */
    public Subtask(String title, String description, Epic epic, LocalDateTime startTime, Duration duration) {
        super(title, description, startTime, duration);
        this.epicId = epic.getId();
        setType(TaskType.SUBTASK);
    }

    /**
     * Создает копию существующей подзадачи.
     *
     * <p>Конструктор копирования для создания защищенных копий подзадач.
     * Копирует все поля, включая ID родительского эпика.</p>
     *
     * @param subtask подзадача для копирования
     */
    public Subtask(Subtask subtask) {
        super(subtask);
        this.epicId = subtask.getEpicId();
    }

    /**
     * Создает подзадачу со всеми параметрами.
     *
     * <p>Используется при десериализации из файла или других источников.</p>
     *
     * @param id идентификатор подзадачи
     * @param title название подзадачи
     * @param description описание подзадачи
     * @param status статус подзадачи
     * @param type тип задачи (должен быть {@link TaskType#SUBTASK})
     * @param epic эпик, которому принадлежит подзадача
     * @param startTime время начала выполнения
     * @param duration длительность выполнения
     */
    public Subtask(Integer id, String title, String description, TaskStatus status, TaskType type, Epic epic, LocalDateTime startTime, Duration duration) {
        super(id, title, description, status, type, startTime, duration);
        this.epicId = epic.getId();
    }

    /**
     * Возвращает идентификатор эпика, которому принадлежит подзадача.
     *
     * @return ID родительского эпика
     */
    public Integer getEpicId() {
        return epicId;
    }

    /**
     * Устанавливает новую принадлежность подзадачи к эпику.
     *
     * @param epic новый эпик для подзадачи
     */
    public void setEpic(Epic epic) {
        this.epicId = epic.getId();
    }

    /**
     * Возвращает строковое представление подзадачи.
     *
     * @return строка с данными подзадачи
     */
    @Override
    public String toString() {
        return "Subtask{" +
               "id=" + getId() +
               ", title='" + getTitle() + '\'' +
               ", description='" + getDescription() + '\'' +
               ", status=" + getStatus() +
               ", type=" + getType() +
               ", duration=" + getDuration() +
               ", startTime=" + getStartTime() +
               '}';
    }
}