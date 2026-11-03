package app;

import app.GymService;
import app.MemberHandler;
import app.SessionHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Каждый контекст — это префикс пути и обработчик для него.
        // Внимание: контекст "/ping" получит и запросы на "/ping/что-угодно".
        server.createContext("/ping", new PingHandler());

        GymService service = new GymService();
        server.createContext("/members", new MemberHandler(service));
        server.createContext("/sessions", new SessionHandler(service));

        // Пул потоков: один медленный клиент не блокирует остальных
        server.setExecutor(Executors.newFixedThreadPool(8));

        server.start();
        System.out.println("Сервер запущен: http://localhost:" + PORT + "/sessions");
    }
}