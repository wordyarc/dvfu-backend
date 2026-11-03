package app;

import app.InvalidDataException;
import app.NotFoundException;
import app.RuleViolationException;
import com.sun.net.httpserver.HttpExchange;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class Http {

    static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final int MAX_BODY_BYTES = 1_000_000;

    private Http() {
    }

    @FunctionalInterface
    interface Route {
        void handle(HttpExchange exchange) throws IOException;
    }

    static void run(HttpExchange exchange, Route route) {
        try {
            route.handle(exchange);
        } catch (NotFoundException e) {
            fail(exchange, 404, e.getMessage());
        } catch (InvalidDataException e) {
            fail(exchange, 400, e.getMessage());
        } catch (RuleViolationException e) {
            fail(exchange, 409, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail(exchange, 500, "внутренняя ошибка сервера");
        } finally {
            exchange.close();
        }
    }

    private static void fail(HttpExchange exchange, int status, String message) {
        try {
            sendJson(exchange, status, Map.of("error", message));
        } catch (Exception ignored) {
            // ответ уже не отправить (например, клиент отключился)
        }
    }

    static void sendJson(HttpExchange exchange, int status, Object data) throws IOException {
        byte[] body = MAPPER.writeValueAsBytes(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    static void sendEmpty(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, -1);
    }

    static void methodNotAllowed(HttpExchange exchange, String allow) throws IOException {
        exchange.getResponseHeaders().set("Allow", allow);
        exchange.sendResponseHeaders(405, -1);
    }

    static <T> T readJson(HttpExchange exchange, Class<T> type) throws IOException {
        byte[] raw;
        try (InputStream in = exchange.getRequestBody()) {
            raw = in.readNBytes(MAX_BODY_BYTES + 1);
        }
        if (raw.length > MAX_BODY_BYTES) {
            throw new InvalidDataException("тело запроса слишком большое");
        }
        if (raw.length == 0) {
            throw new InvalidDataException("тело запроса пустое, ожидался JSON");
        }
        T value;
        try {
            value = MAPPER.readValue(raw, type);
        } catch (Exception e) {
            throw new InvalidDataException("неверный JSON или неверные типы полей");
        }
        if (value == null) {
            throw new InvalidDataException("тело запроса не должно быть null");
        }
        return value;
    }

    static String[] segments(HttpExchange exchange, String base) {
        String path = exchange.getRequestURI().getPath();
        String rest = path.substring(base.length());
        if (!rest.isEmpty() && !rest.startsWith("/")) {
            throw new NotFoundException("путь не найден");
        }
        return Arrays.stream(rest.split("/")).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }

    static int parseId(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new NotFoundException("ресурс не найден");
        }
    }

    static Map<String, String> query(HttpExchange exchange, String... allowedNames) {
        Map<String, String> result = new HashMap<>();
        String raw = exchange.getRequestURI().getRawQuery();
        if (raw != null && !raw.isEmpty()) {
            for (String pair : raw.split("&")) {
                if (pair.isEmpty()) {
                    continue;
                }
                int eq = pair.indexOf('=');
                String key = eq < 0 ? pair : pair.substring(0, eq);
                String value = eq < 0 ? "" : pair.substring(eq + 1);
                try {
                    result.put(URLDecoder.decode(key, StandardCharsets.UTF_8),
                            URLDecoder.decode(value, StandardCharsets.UTF_8));
                } catch (IllegalArgumentException e) {
                    throw new InvalidDataException("неверная строка запроса");
                }
            }
        }
        Set<String> allowed = Set.of(allowedNames);
        for (String name : result.keySet()) {
            if (!allowed.contains(name)) {
                throw new InvalidDataException("неизвестный параметр фильтра: " + name);
            }
        }
        return result;
    }

    static Boolean boolParam(Map<String, String> params, String name) {
        String value = params.get(name);
        if (value == null) {
            return null;
        }
        if (value.equals("true")) {
            return true;
        }
        if (value.equals("false")) {
            return false;
        }
        throw new InvalidDataException(name + " должен быть true или false");
    }
}