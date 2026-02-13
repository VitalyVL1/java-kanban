package api.type_token;

import com.google.gson.reflect.TypeToken;
import model.Subtask;

import java.util.List;

/**
 * TypeToken для корректной десериализации списка подзадач {@link Subtask} из JSON.
 *
 * <p>Используется Gson для сохранения информации о типе {@code List<Subtask>}
 * во время выполнения, так как Java стирает информацию об обобщенных типах
 * (type erasure).</p>
 *
 * <p>Пример использования:</p>
 * <pre>
 * // Чтение списка подзадач из JSON
 * String jsonSubtasks = "[{\"id\":1,\"name\":\"Subtask 1\",\"epicId\":1}, {\"id\":2,\"name\":\"Subtask 2\",\"epicId\":1}]";
 * List&lt;Subtask&gt; subtasks = gson.fromJson(jsonSubtasks, new SubtaskListTypeToken().getType());
 *
 * // Использование в TypeAdapter или при работе с коллекциями
 * Type subtaskListType = new SubtaskListTypeToken().getType();
 * </pre>
 *
 * @see com.google.gson.reflect.TypeToken
 * @see model.Subtask
 * @see <a href="https://github.com/google/gson/blob/master/UserGuide.md#TOC-Serializing-and-Deserializing-Collection-with-Objects-of-Arbitrary-Types">Gson User Guide - Collections</a>
 */
public class SubtaskListTypeToken extends TypeToken<List<Subtask>> {
}