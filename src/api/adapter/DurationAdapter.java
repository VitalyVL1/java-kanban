package api.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

/**
 * Gson адаптер для сериализации и десериализации объектов {@link Duration}.
 *
 * <p>Преобразует Duration в количество минут (тип Long) при сериализации в JSON
 * и обратно в Duration при десериализации из JSON.</p>
 *
 * <p>Пример использования:</p>
 * <pre>
 * Gson gson = new GsonBuilder()
 *     .registerTypeAdapter(Duration.class, new DurationAdapter())
 *     .create();
 *
 * Duration duration = Duration.ofHours(2); // 120 минут
 * String json = gson.toJson(duration); // Результат: 120
 *
 * Duration deserialized = gson.fromJson("30", Duration.class); // 30 минут
 * </pre>
 *
 * @see com.google.gson.GsonBuilder
 * @see java.time.Duration
 */
public class DurationAdapter extends TypeAdapter<Duration> {

    /**
     * Сериализует объект Duration в JSON.
     *
     * @param jsonWriter писатель JSON потока
     * @param duration   объект Duration для сериализации (не может быть null)
     * @throws IOException если возникает ошибка ввода-вывода при записи
     */
    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        jsonWriter.value(duration.toMinutes());
    }

    /**
     * Десериализует JSON в объект Duration.
     *
     * @param jsonReader читатель JSON потока
     * @return объект Duration, представляющий количество минут из JSON
     * @throws IOException если возникает ошибка ввода-вывода при чтении
     */
    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        return Duration.ofMinutes(jsonReader.nextLong());
    }
}