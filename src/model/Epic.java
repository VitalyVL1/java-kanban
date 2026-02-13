package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Класс, представляющий эпик - составную задачу, объединяющую несколько подзадач.
 *
 * <p>Эпик не выполняется сам по себе, его статус и временные параметры вычисляются
 * на основе статусов и времени выполнения его подзадач ({@link Subtask}).
 * Эпик может содержать любое количество подзадач.</p>
 *
 * <p>Особенности эпика:</p>
 * <ul>
 *   <li><b>Статус</b> - вычисляется автоматически на основе статусов подзадач</li>
 *   <li><b>Время начала</b> - равно самому раннему времени начала среди подзадач</li>
 *   <li><b>Длительность</b> - сумма длительностей всех подзадач</li>
 *   <li><b>Время окончания</b> - равно самому позднему времени окончания среди подзадач</li>
 *   <li><b>Подзадачи</b> - хранятся только идентификаторы подзадач, сами объекты хранятся в менеджере</li>
 * </ul>
 *
 * @see Task
 * @see Subtask
 * @see manager.InMemoryTaskManager
 */
public class Epic extends Task {

    /** Множество идентификаторов подзадач, принадлежащих этому эпику */
    private Set<Integer> subtasksId;

    /** Время окончания эпика (самое позднее время окончания среди подзадач) */
    private LocalDateTime endTime;

    /**
     * Создает новый эпик с указанными названием и описанием.
     *
     * <p>Тип задачи автоматически устанавливается в {@link TaskType#EPIC}.
     * Время начала и длительность инициализируются значениями по умолчанию
     * и будут вычислены позже на основе подзадач.</p>
     *
     * @param title название эпика
     * @param description описание эпика
     */
    public Epic(String title, String description) {
        super(title, description, null, Duration.ZERO);
        subtasksId = new HashSet<>();
        setType(TaskType.EPIC);
    }

    /**
     * Создает копию существующего эпика.
     *
     * <p>Конструктор копирования для создания защищенных копий эпиков.
     * Все поля копируются, включая множество идентификаторов подзадач.</p>
     *
     * @param epic эпик для копирования
     */
    public Epic(Epic epic) {
        super(epic);
        subtasksId = new HashSet<>(epic.subtasksId);
        endTime = epic.endTime;
    }

    /**
     * Создает эпик со всеми параметрами.
     *
     * <p>Используется при десериализации из файла или других источников.</p>
     *
     * @param id идентификатор эпика
     * @param title название эпика
     * @param description описание эпика
     * @param status статус эпика
     * @param type тип задачи (должен быть {@link TaskType#EPIC})
     * @param subtasks множество идентификаторов подзадач
     * @param startTime время начала эпика
     * @param duration длительность эпика
     */
    public Epic(Integer id, String title, String description, TaskStatus status, TaskType type, Set<Integer> subtasks, LocalDateTime startTime, Duration duration) {
        super(id, title, description, status, type, startTime, duration);
        this.subtasksId = subtasks;
    }

    /**
     * Добавляет или обновляет подзадачу в эпике.
     *
     * <p>Добавляет идентификатор подзадачи в множество подзадач эпика.
     * Если подзадача уже существует, повторное добавление не создает дубликат
     * благодаря использованию {@link Set}.</p>
     *
     * @param subtask подзадача для добавления
     */
    public void addOrUpdateSubtask(Subtask subtask) {
        subtasksId.add(subtask.getId());
    }

    /**
     * Возвращает множество идентификаторов подзадач эпика.
     *
     * @return множество ID подзадач
     */
    public Set<Integer> getSubtasksId() {
        return subtasksId;
    }

    /**
     * Устанавливает множество идентификаторов подзадач.
     *
     * @param subtasksId новое множество ID подзадач
     */
    public void setSubtasks(Set<Integer> subtasksId) {
        this.subtasksId = subtasksId;
    }

    /**
     * Удаляет подзадачу из эпика.
     *
     * @param subtask подзадача для удаления
     */
    public void removeSubtask(Subtask subtask) {
        subtasksId.remove(subtask.getId());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Для эпика возвращает время окончания, вычисленное на основе
     * самой поздней подзадачи. Может быть null, если у эпика нет подзадач
     * или ни одна подзадача не имеет времени начала.</p>
     */
    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Устанавливает время окончания эпика.
     *
     * <p>Обычно вызывается менеджером при пересчете временных параметров эпика.</p>
     *
     * @param endTime новое время окончания
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Возвращает строковое представление эпика.
     *
     * @return строка с данными эпика
     */
    @Override
    public String toString() {
        return "Epic{" +
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