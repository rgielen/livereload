package net.rgielen.livereload.server;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LiveReloadServerLearningTest {

    private LiveReloadServer server;
    private OkHttpClient client;

    @BeforeEach
    void startServer() {
        client = new OkHttpClient();
        server = new LiveReloadServer();
        server.startAndWait();
    }

    @Test
    public void createAndStartServerInNewThread() throws Exception {
        final Request request = new Request.Builder().url(server.baseUrlString()).get().build();
        final Response response = client.newCall(request).execute();
        assertThat(response.body().string()).isEqualTo("Hello World!");
    }

    @AfterEach
    void shutdown() throws InterruptedException {
        server.shutdown();
    }

}
