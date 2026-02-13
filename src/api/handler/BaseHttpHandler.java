package api.handler;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Базовый класс для обработки HTTP запросов.
 *
 * <p>Предоставляет удобные методы для отправки ответов с различными HTTP статусами
 * и автоматическим управлением кодировкой UTF-8.</p>
 *
 * <p>Поддерживаемые статусы ответов:</p>
 * <ul>
 *   <li>200 OK - успешный ответ с данными</li>
 *   <li>404 Not Found - ресурс не найден</li>
 *   <li>406 Not Acceptable - конфликт/пересечение ресурсов</li>
 *   <li>другие статусы через метод {@link #writeResponse}</li>
 * </ul>
 *
 * <p>Пример использования в наследнике:</p>
 * <pre>
 * public class TaskHandler extends BaseHttpHandler {
 *     public void handle(HttpExchange exchange) throws IOException {
 *         String method = exchange.getRequestMethod();
 *
 *         if ("GET".equals(method)) {
 *             sendText(exchange, "{\"data\": \"task info\"}");
 *         } else {
 *             sendNotFound(exchange, "Метод не поддерживается");
 *         }
 *     }
 * }
 * </pre>
 *
 * @see com.sun.net.httpserver.HttpHandler
 * @see com.sun.net.httpserver.HttpExchange
 */
public class BaseHttpHandler {

    /** Кодировка по умолчанию для всех ответов - UTF-8 */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Отправляет успешный ответ с кодом 200.
     *
     * @param exchange объект HTTP обмена
     * @param text текст ответа (обычно JSON)
     * @throws IOException если возникает ошибка ввода-вывода
     */
    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] response = text.getBytes(DEFAULT_CHARSET);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    /**
     * Отправляет ответ с кодом 404 (Not Found).
     *
     * @param exchange объект HTTP обмена
     * @param text текст сообщения об ошибке
     * @throws IOException если возникает ошибка ввода-вывода
     */
    protected void sendNotFound(HttpExchange exchange, String text) throws IOException {
        byte[] resp = text.getBytes(DEFAULT_CHARSET);
        exchange.getResponseHeaders().add("Content-Type", "text/html;charset=utf-8");
        exchange.sendResponseHeaders(404, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    /**
     * Отправляет ответ с кодом 406 (Not Acceptable).
     *
     * <p>Используется для указания на конфликт или невозможность выполнения операции,
     * например при попытке создать задачу с пересекающимся временем.</p>
     *
     * @param exchange объект HTTP обмена
     * @param text текст сообщения об ошибке
     * @throws IOException если возникает ошибка ввода-вывода
     */
    protected void sendHasInteractions(HttpExchange exchange, String text) throws IOException {
        byte[] response = text.getBytes(DEFAULT_CHARSET);
        exchange.getResponseHeaders().add("Content-Type", "text/html;charset=utf-8");
        exchange.sendResponseHeaders(406, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    /**
     * Универсальный метод для отправки ответа с произвольным HTTP статусом.
     *
     * <p>Автоматически закрывает OutputStream и HTTP соединение.</p>
     *
     * @param exchange объект HTTP обмена
     * @param responseString текст ответа
     * @param responseCode HTTP статус код (200, 404, 500 и т.д.)
     * @throws IOException если возникает ошибка ввода-вывода
     */
    protected void writeResponse(HttpExchange exchange, String responseString, int responseCode) throws IOException {
        byte[] response = responseString.getBytes(DEFAULT_CHARSET);

        try (OutputStream os = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(responseCode, response.length);
            os.write(response);
        }

        exchange.close();
    }
}