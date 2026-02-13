package model;

/**
 * Перечисление статусов выполнения задачи.
 *
 * <p>Определяет возможные состояния, в которых может находиться задача
 * в процессе своего жизненного цикла. Статус используется для отслеживания
 * прогресса выполнения и автоматического вычисления статуса эпиков на основе
 * статусов их подзадач.</p>
 *
 * <p>Возможные статусы:</p>
 * <ul>
 *   <li><b>NEW</b> - задача создана, но ещё не начата</li>
 *   <li><b>IN_PROGRESS</b> - задача в процессе выполнения</li>
 *   <li><b>DONE</b> - задача завершена</li>
 * </ul>
 *
 * <p>Правила вычисления статуса эпика:</p>
 * <ul>
 *   <li>Если все подзадачи NEW → эпик NEW</li>
 *   <li>Если все подзадачи DONE → эпик DONE</li>
 *   <li>Иначе (есть подзадачи в разных статусах) → эпик IN_PROGRESS</li>
 * </ul>
 *
 * <p>Пример использования:</p>
 * <pre>
 * Task task = new Task("Уборка", "Помыть полы", TaskStatus.NEW);
 *
 * if (task.getStatus() == TaskStatus.DONE) {
 *     System.out.println("Задача выполнена!");
 * } else {
 *     task.setStatus(TaskStatus.IN_PROGRESS);
 * }
 * </pre>
 *
 * @see model.Task
 * @see model.Epic
 * @see model.Subtask
 * @see manager.InMemoryTaskManager#checkEpicStatus(Epic)
 */
public enum TaskStatus {
    /** Задача создана, но ещё не начата */
    NEW,

    /** Задача в процессе выполнения */
    IN_PROGRESS,

    /** Задача успешно завершена */
    DONE
}