package app;

import app.NotFoundException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SessionHandler implements HttpHandler {

    private final GymService service;

    public SessionHandler(GymService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) {
        Http.run(exchange, this::route);
    }

    private void route(HttpExchange exchange) throws IOException {
        String[] parts = Http.segments(exchange, "/sessions");
        String method = exchange.getRequestMethod();

        if (parts.length == 0) {
            switch (method) {
                case "GET" -> list(exchange);
                case "POST" -> create(exchange);
                default -> Http.methodNotAllowed(exchange, "GET, POST");
            }
        } else if (parts.length == 1) {
            int id = Http.parseId(parts[0]);
            switch (method) {
                case "GET" -> Http.sendJson(exchange, 200, Views.session(service.getSession(id)));
                case "PUT" -> update(exchange, id);
                case "DELETE" -> {
                    service.deleteSession(id);
                    Http.sendEmpty(exchange, 204);
                }
                default -> Http.methodNotAllowed(exchange, "GET, PUT, DELETE");
            }
        } else if (parts.length == 2 && parts[1].equals("members")) {
            int id = Http.parseId(parts[0]);
            if (method.equals("GET")) {
                List<Map<String, Object>> result = service.membersOf(id).stream()
                        .map(Views::member)
                        .toList();
                Http.sendJson(exchange, 200, result);
            } else {
                Http.methodNotAllowed(exchange, "GET");
            }
        } else if (parts.length == 2 && parts[1].equals("book")) {
            int id = Http.parseId(parts[0]);
            if (method.equals("POST")) {
                Requests.BookRequest r = Http.readJson(exchange, Requests.BookRequest.class);
                Http.sendJson(exchange, 200, Views.session(service.book(id, r.memberId())));
            } else {
                Http.methodNotAllowed(exchange, "POST");
            }
        } else if (parts.length == 2 && parts[1].equals("cancel")) {
            int id = Http.parseId(parts[0]);
            if (method.equals("POST")) {
                Requests.BookRequest r = Http.readJson(exchange, Requests.BookRequest.class);
                Http.sendJson(exchange, 200, Views.session(service.cancel(id, r.memberId())));
            } else {
                Http.methodNotAllowed(exchange, "POST");
            }
        } else {
            throw new NotFoundException("путь не найден");
        }
    }

    private void list(HttpExchange exchange) throws IOException {
        Map<String, String> q = Http.query(exchange, "hasFreePlaces", "name");
        Boolean hasFreePlaces = Http.boolParam(q, "hasFreePlaces");

        List<Map<String, Object>> result = service.listSessions(hasFreePlaces, q.get("name"))
                .stream()
                .map(Views::session)
                .toList();
        Http.sendJson(exchange, 200, result);
    }

    private void create(HttpExchange exchange) throws IOException {
        Requests.SessionRequest r = Http.readJson(exchange, Requests.SessionRequest.class);
        Session session = service.addSession(r.name(), r.capacity());
        Http.sendJson(exchange, 201, Views.session(session));
    }

    private void update(HttpExchange exchange, int id) throws IOException {
        Requests.SessionRequest r = Http.readJson(exchange, Requests.SessionRequest.class);
        Session session = service.updateSession(id, r.name(), r.capacity());
        Http.sendJson(exchange, 200, Views.session(session));
    }
}