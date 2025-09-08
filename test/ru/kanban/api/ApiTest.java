package ru.kanban.api;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import ru.kanban.HttpTaskServer;
import ru.kanban.manager.InMemoryTaskManager;
import ru.kanban.manager.TaskManager;

public class ApiTest {
    protected TaskManager manager;
    protected HttpTaskServer server;
    protected Gson gson;
    protected HttpClient client;
    protected int port = 8080;

    @BeforeEach
    void setUpBase() throws IOException {
        manager = new InMemoryTaskManager();
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();

        manager.clearAll();

        server = new HttpTaskServer(manager, port);
        server.start();
    }

    @AfterEach
    void tearDownBase() {
        server.stop();
    }

    protected HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:"+port+path)).GET().build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> post(String path, String body) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:"+port+path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> delete(String path) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:"+port+path)).DELETE().build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }
}
