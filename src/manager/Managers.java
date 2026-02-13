package manager;

/**
 * Фабричный класс для создания экземпляров менеджеров приложения.
 *
 * <p>Предоставляет статические методы для получения настроенных экземпляров
 * {@link TaskManager} и {@link HistoryManager}. Централизует логику создания
 * менеджеров и упрощает конфигурацию приложения.</p>
 *
 * <p>Примеры использования:</p>
 * <pre>
 * // Получение стандартного менеджера задач (в памяти)
 * TaskManager manager = Managers.getDefault();
 *
 * // Получение менеджера с сохранением в файл
 * TaskManager fileManager = Managers.getFileBackedTaskManager("tasks.csv");
 *
 * // Получение отдельного менеджера истории
 * HistoryManager historyManager = Managers.getDefaultHistory();
 * </pre>
 *
 * @see TaskManager
 * @see HistoryManager
 * @see InMemoryTaskManager
 * @see InMemoryHistoryManager
 * @see FileBackedTaskManager
 */
public class Managers {

    /**
     * Возвращает экземпляр менеджера задач по умолчанию.
     *
     * <p>Создает {@link InMemoryTaskManager} с историей просмотров,
     * полученной через {@link #getDefaultHistory()}.</p>
     *
     * @return менеджер задач для работы в памяти
     */
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    /**
     * Возвращает экземпляр менеджера истории по умолчанию.
     *
     * <p>Создает {@link InMemoryHistoryManager} - реализацию истории просмотров,
     * хранящую задачи в оперативной памяти с ограниченным размером.</p>
     *
     * @return менеджер для управления историей просмотров
     */
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    /**
     * Возвращает экземпляр менеджера задач с автоматическим сохранением в файл.
     *
     * <p>Создает {@link FileBackedTaskManager} который автоматически сохраняет
     * состояние задач в указанный файл при каждом изменении и восстанавливает
     * состояние при запуске.</p>
     *
     * @param pathName путь к файлу для сохранения данных
     * @return менеджер задач с файловым persistence
     */
    public static TaskManager getFileBackedTaskManager(String pathName) {
        return new FileBackedTaskManager(getDefaultHistory(), pathName);
    }
}