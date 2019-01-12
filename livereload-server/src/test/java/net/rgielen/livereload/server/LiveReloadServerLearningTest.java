package net.rgielen.livereload.server;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class LiveReloadServerLearningTest {

    private static Object monitor = new Object();

    private LiveReloadServer server;
    private ExecutorService pool;
    private OkHttpClient client;

    @BeforeEach
    void startServer() {
        client = new OkHttpClient();
        server = new LiveReloadServer();
        pool = Executors.newFixedThreadPool(1);
        pool.execute(() -> {
            server.server.start();
        });
        server.waitUntilReady();
    }

    @Test
    public void createAndStartServerInNewThread() throws Exception {
        final Request request = new Request.Builder().url("http://localhost:" + server.port + "").get().build();
        final Response response = client.newCall(request).execute();
        assertThat(response.body().string()).isEqualTo("Hello World!");
    }

    @AfterEach
    void shutdown() throws InterruptedException {
        synchronized (monitor) {
            server.server.stop();
            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.MINUTES);
            System.out.println("Server shutdown completed");
        }
    }

}
