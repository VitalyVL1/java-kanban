package api.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gson адаптер для сериализации и десериализации объектов {@link LocalDateTime}.
 *
 * <p>Преобразует LocalDateTime в строку формата "dd-MM-yyyy HH:mm:ss.SSS"
 * при сериализации в JSON и обратно в LocalDateTime при десериализации из JSON.</p>
 *
 * <p>Адаптер корректно обрабатывает null значения:</p>
 * <ul>
 *   <li>При сериализации null преобразуется в JSON null</li>
 *   <li>При десериализации JSON null возвращает null</li>
 * </ul>
 *
 * <p>Пример использования:</p>
 * <pre>
 * Gson gson = new GsonBuilder()
 *     .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
 *     .create();
 *
 * LocalDateTime dateTime = LocalDateTime.now();
 * String json = gson.toJson(dateTime); // "15-03-2024 14:30:25.123"
 *
 * LocalDateTime deserialized = gson.fromJson("\"15-03-2024 14:30:25.123\"", LocalDateTime.class);
 * </pre>
 *
 * @see com.google.gson.GsonBuilder
 * @see java.time.LocalDateTime
 * @see java.time.format.DateTimeFormatter
 */
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    /** Форматтер для даты и времени: день-месяц-год часы:минуты:секунды.миллисекунды */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS");

    /**
     * Сериализует объект LocalDateTime в JSON строку.
     *
     * @param jsonWriter писатель JSON потока
     * @param localDateTime объект LocalDateTime для сериализации (может быть null)
     * @throws IOException если возникает ошибка ввода-вывода при записи
     */
    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
        if (localDateTime != null) {
            jsonWriter.value(localDateTime.format(DATE_TIME_FORMATTER));
        } else {
            jsonWriter.nullValue();
        }
    }

    /**
     * Десериализует JSON строку в объект LocalDateTime.
     *
     * @param jsonReader читатель JSON потока
     * @return объект LocalDateTime, или null если JSON значение null
     * @throws IOException если возникает ошибка ввода-вывода при чтении
     */
    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        if (jsonReader.hasNext()) {
            return LocalDateTime.parse(jsonReader.nextString(), DATE_TIME_FORMATTER);
        }
        return null;
    }
}