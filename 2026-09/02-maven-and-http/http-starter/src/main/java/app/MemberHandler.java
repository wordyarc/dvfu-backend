package app;

import app.GymService;
import app.InvalidDataException;
import app.Member;
import app.MemberType;
import app.NotFoundException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MemberHandler implements HttpHandler {

    private final GymService service;

    public MemberHandler(GymService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) {
        Http.run(exchange, this::route);
    }

    private void route(HttpExchange exchange) throws IOException {
        String[] parts = Http.segments(exchange, "/members");
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
                case "GET" -> Http.sendJson(exchange, 200, Views.member(service.getMember(id)));
                case "PUT" -> update(exchange, id);
                case "DELETE" -> {
                    service.deleteMember(id);
                    Http.sendEmpty(exchange, 204);
                }
                default -> Http.methodNotAllowed(exchange, "GET, PUT, DELETE");
            }
        } else if (parts.length == 2 && parts[1].equals("sessions")) {
            int id = Http.parseId(parts[0]);
            if (method.equals("GET")) {
                List<Map<String, Object>> result = service.sessionsOf(id).stream()
                        .map(Views::session)
                        .toList();
                Http.sendJson(exchange, 200, result);
            } else {
                Http.methodNotAllowed(exchange, "GET");
            }
        } else {
            throw new NotFoundException("путь не найден");
        }
    }

    private void list(HttpExchange exchange) throws IOException {
        Map<String, String> q = Http.query(exchange, "type", "name", "canBook");

        MemberType type = null;
        if (q.containsKey("type")) {
            try {
                type = MemberType.valueOf(q.get("type").toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new InvalidDataException("type должен быть VIP, SILVER или GOLD");
            }
        }
        Boolean canBook = Http.boolParam(q, "canBook");

        List<Map<String, Object>> result = service.listMembers(type, q.get("name"), canBook)
                .stream()
                .map(Views::member)
                .toList();
        Http.sendJson(exchange, 200, result);
    }

    private void create(HttpExchange exchange) throws IOException {
        Requests.MemberRequest r = Http.readJson(exchange, Requests.MemberRequest.class);
        Member member = service.addMember(r.type(), r.name(), r.age());
        Http.sendJson(exchange, 201, Views.member(member));
    }

    private void update(HttpExchange exchange, int id) throws IOException {
        Requests.MemberRequest r = Http.readJson(exchange, Requests.MemberRequest.class);
        Member member = service.updateMember(id, r.type(), r.name(), r.age());
        Http.sendJson(exchange, 200, Views.member(member));
    }
}