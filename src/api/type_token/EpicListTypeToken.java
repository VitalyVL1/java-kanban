package api.type_token;

import com.google.gson.reflect.TypeToken;
import model.Epic;

import java.util.List;

/**
 * TypeToken для корректной десериализации списка эпиков {@link Epic} из JSON.
 *
 * <p>Используется Gson для сохранения информации о типе {@code List<Epic>}
 * во время выполнения, так как Java стирает информацию об обобщенных типах
 * (type erasure).</p>
 *
 * <p>Пример использования:</p>
 * <pre>
 * // Чтение списка эпиков из JSON
 * String jsonEpics = "[{\"id\":1,\"name\":\"Epic 1\"}, {\"id\":2,\"name\":\"Epic 2\"}]";
 * List&lt;Epic&gt; epics = gson.fromJson(jsonEpics, new EpicListTypeToken().getType());
 *
 * // Использование в TypeAdapter или при работе с коллекциями
 * Type epicListType = new EpicListTypeToken().getType();
 * </pre>
 *
 * @see com.google.gson.reflect.TypeToken
 * @see model.Epic
 * @see <a href="https://github.com/google/gson/blob/master/UserGuide.md#TOC-Serializing-and-Deserializing-Collection-with-Objects-of-Arbitrary-Types">Gson User Guide - Collections</a>
 */
public class EpicListTypeToken extends TypeToken<List<Epic>> {
}