package exception;

/**
 * Исключение, выбрасываемое при попытке обращения к несуществующей задаче.
 *
 * <p>Возникает когда запрашивается задача, эпик или подзадача по идентификатору,
 * который отсутствует в менеджере задач.</p>
 *
 * <p>Примеры ситуаций:</p>
 * <ul>
 *   <li>Получение задачи по несуществующему ID</li>
 *   <li>Обновление задачи с несуществующим ID</li>
 *   <li>Удаление уже удаленной задачи</li>
 *   <li>Запрос подзадач для несуществующего эпика</li>
 * </ul>
 *
 * @see manager.TaskManager
 * @see api.handler.TaskHttpHandler
 */
public class NotFoundException extends RuntimeException {

    /**
     * Создает исключение с указанным сообщением.
     *
     * @param message детальное сообщение об ошибке
     */
    public NotFoundException(String message) {
        super(message);
    }
}