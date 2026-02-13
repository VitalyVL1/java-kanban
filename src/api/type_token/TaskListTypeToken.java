package api.type_token;

import com.google.gson.reflect.TypeToken;
import model.Task;

import java.util.List;

/**
 * TypeToken для корректной десериализации списка задач {@link Task} из JSON.
 *
 * <p>Используется Gson для сохранения информации о типе {@code List<Task>}
 * во время выполнения, так как Java стирает информацию об обобщенных типах
 * (type erasure).</p>
 *
 * <p>Пример использования:</p>
 * <pre>
 * // Чтение списка задач из JSON
 * String jsonTasks = "[{\"id\":1,\"name\":\"Task 1\"}, {\"id\":2,\"name\":\"Task 2\"}]";
 * List&lt;Task&gt; tasks = gson.fromJson(jsonTasks, new TaskListTypeToken().getType());
 *
 * // Использование в TypeAdapter или при работе с коллекциями
 * Type taskListType = new TaskListTypeToken().getType();
 * </pre>
 *
 * @see com.google.gson.reflect.TypeToken
 * @see model.Task
 * @see <a href="https://github.com/google/gson/blob/master/UserGuide.md#TOC-Serializing-and-Deserializing-Collection-with-Objects-of-Arbitrary-Types">Gson User Guide - Collections</a>
 */
public class TaskListTypeToken extends TypeToken<List<Task>> {
}