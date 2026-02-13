package exception;

/**
 * Исключение, выбрасываемое при ошибках сохранения данных менеджера задач.
 *
 * <p>Возникает при проблемах с файловой системой, сериализацией или
 * другими операциями ввода-вывода при сохранении или загрузке состояния.</p>
 *
 * <p>Примеры ситуаций:</p>
 * <ul>
 *   <li>Ошибка записи в файл</li>
 *   <li>Ошибка чтения из файла</li>
 *   <li>Отсутствие прав доступа к файлу</li>
 *   <li>Повреждение данных при десериализации</li>
 * </ul>
 *
 * @see manager.FileBackedTaskManager
 */
public class ManagerSaveException extends RuntimeException {

    /**
     * Создает исключение с указанным сообщением.
     *
     * @param message детальное сообщение об ошибке
     */
    public ManagerSaveException(String message) {
        super(message);
    }
}