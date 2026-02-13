package manager;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

/**
 * Интерфейс, определяющий контракт менеджера для управления задачами.
 *
 * <p>Предоставляет методы для выполнения всех основных операций с задачами (Task),
 * эпиками (Epic) и подзадачами (Subtask): создание, чтение, обновление, удаление,
 * а также дополнительные операции для работы с историей просмотров и приоритетами.</p>
 *
 * <p>Основные возможности:</p>
 * <ul>
 *   <li><b>CRUD операции</b> - добавление, получение, обновление, удаление задач всех типов</li>
 *   <li><b>Работа с эпиками</b> - управление эпиками и их подзадачами</li>
 *   <li><b>История просмотров</b> - отслеживание последних просмотренных задач</li>
 *   <li><b>Приоритеты</b> - получение задач, отсортированных по времени начала</li>
 * </ul>
 *
 * <p>Все методы, работающие с идентификаторами, могут выбрасывать {@link exception.NotFoundException}
 * при обращении к несуществующей задаче. Методы добавления и обновления могут выбрасывать
 * {@link exception.TaskOverlappingException} при пересечении временных интервалов.</p>
 *
 * @see model.Task
 * @see model.Epic
 * @see model.Subtask
 * @see exception.NotFoundException
 * @see exception.TaskOverlappingException
 */
public interface TaskManager {

    /**
     * Возвращает список всех эпиков.
     *
     * @return список эпиков (может быть пустым)
     */
    List<Epic> getEpics();

    /**
     * Возвращает список всех обычных задач.
     *
     * @return список задач (может быть пустым)
     */
    List<Task> getTasks();

    /**
     * Возвращает список всех подзадач.
     *
     * @return список подзадач (может быть пустым)
     */
    List<Subtask> getSubtasks();

    /**
     * Добавляет новый эпик в менеджер.
     *
     * @param epic эпик для добавления (не может быть null)
     * @return сгенерированный идентификатор эпика
     */
    int addEpic(Epic epic);

    /**
     * Добавляет новую задачу в менеджер.
     *
     * @param task задача для добавления (не может быть null)
     * @return сгенерированный идентификатор задачи
     * @throws exception.TaskOverlappingException если задача пересекается по времени с существующими
     */
    int addTask(Task task);

    /**
     * Добавляет новую подзадачу в менеджер.
     *
     * @param subtask подзадача для добавления (не может быть null)
     * @return сгенерированный идентификатор подзадачи
     * @throws exception.NotFoundException если указанный в подзадаче эпик не существует
     * @throws exception.TaskOverlappingException если подзадача пересекается по времени с существующими
     */
    int addSubtask(Subtask subtask);

    /**
     * Удаляет эпик по идентификатору.
     *
     * @param id идентификатор эпика
     * @return удаленный эпик
     * @throws exception.NotFoundException если эпик с указанным id не найден
     */
    Epic removeEpic(int id);

    /**
     * Удаляет задачу по идентификатору.
     *
     * @param id идентификатор задачи
     * @return удаленную задачу
     * @throws exception.NotFoundException если задача с указанным id не найдена
     */
    Task removeTask(int id);

    /**
     * Удаляет подзадачу по идентификатору.
     *
     * @param id идентификатор подзадачи
     * @return удаленную подзадачу
     * @throws exception.NotFoundException если подзадача с указанным id не найдена
     */
    Subtask removeSubtask(int id);

    /**
     * Возвращает все подзадачи, принадлежащие указанному эпику.
     *
     * @param epic эпик, для которого нужно получить подзадачи
     * @return список подзадач эпика (может быть пустым)
     */
    List<Subtask> getAllSubtasksByEpic(Epic epic);

    /**
     * Удаляет все эпики и их подзадачи.
     */
    void clearEpics();

    /**
     * Удаляет все обычные задачи.
     */
    void clearTasks();

    /**
     * Удаляет все подзадачи.
     */
    void clearSubtasks();

    /**
     * Обновляет существующий эпик.
     *
     * @param epic эпик с обновленными данными (должен содержать корректный id)
     * @throws exception.NotFoundException если эпик с указанным id не найден
     */
    void updateEpic(Epic epic);

    /**
     * Обновляет существующую задачу.
     *
     * @param task задача с обновленными данными (должна содержать корректный id)
     * @throws exception.NotFoundException если задача с указанным id не найдена
     * @throws exception.TaskOverlappingException если обновленная задача пересекается по времени с существующими
     */
    void updateTask(Task task);

    /**
     * Обновляет существующую подзадачу.
     *
     * @param subtask подзадача с обновленными данными (должна содержать корректный id)
     * @throws exception.NotFoundException если подзадача с указанным id не найдена
     * @throws exception.TaskOverlappingException если обновленная подзадача пересекается по времени с существующими
     */
    void updateSubtask(Subtask subtask);

    /**
     * Возвращает задачу по идентификатору.
     *
     * <p>При успешном получении задача добавляется в историю просмотров.</p>
     *
     * @param id идентификатор задачи
     * @return задача с указанным id
     * @throws exception.NotFoundException если задача не найдена
     */
    Task getTask(int id);

    /**
     * Возвращает эпик по идентификатору.
     *
     * <p>При успешном получении эпик добавляется в историю просмотров.</p>
     *
     * @param id идентификатор эпика
     * @return эпик с указанным id
     * @throws exception.NotFoundException если эпик не найден
     */
    Epic getEpic(int id);

    /**
     * Возвращает подзадачу по идентификатору.
     *
     * <p>При успешном получении подзадача добавляется в историю просмотров.</p>
     *
     * @param id идентификатор подзадачи
     * @return подзадача с указанным id
     * @throws exception.NotFoundException если подзадача не найдена
     */
    Subtask getSubtask(int id);

    /**
     * Возвращает историю просмотров задач.
     *
     * <p>История содержит последние просмотренные задачи в порядке от старых к новым.
     * Размер истории ограничен (обычно 10 задач).</p>
     *
     * @return список задач из истории просмотров
     */
    List<Task> getHistory();

    /**
     * Возвращает список задач, отсортированный по времени начала.
     *
     * <p>Задачи без времени начала (null) помещаются в конец списка.
     * Сортировка используется для определения приоритетов и проверки пересечений.</p>
     *
     * @return список задач, отсортированный по времени начала
     */
    List<Task> getPrioritizedTasks();
}