package net.rgielen.livereload.server;

import io.undertow.Undertow;
import io.undertow.util.Headers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * LiveReloadServer.
 *
 * @author Rene Gielen
 */
public class LiveReloadServer {

    public static final int LIVEREOLAD_DEFAULT_PORT = 35729;
    private static Object monitor = new Object();

    final int port;
    Undertow server;
    private ExecutorService pool;

    public LiveReloadServer() {
        this(LIVEREOLAD_DEFAULT_PORT);
    }

    public LiveReloadServer(int port) {
        this.port = port;
        this.server = createServer(port);
    }

    public String baseUrlString() {
        return "http://localhost:" + port;
    }

    public URL baseUrl() {
        try {
            return new URL(baseUrlString());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    boolean isReady() {
        try {
            HttpURLConnection urlConn = (HttpURLConnection) baseUrl().openConnection();
            urlConn.connect();
            urlConn.disconnect();
            return true;
        } catch (IOException e) {
            return false;
        }

    }

    Undertow createServer(int port) {
        return Undertow.builder()
                .addHttpListener(port, "localhost", exchange -> {
                    exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("Hello World!");
                })
                .build();
    }

    void waitUntilReady() {
        while (!isReady()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e1) {
                // just wait
            }
        }
    }

    public void start() {
        pool = Executors.newFixedThreadPool(1);
        pool.execute(() -> {
            server.start();
        });
        waitUntilReady();
    }

    public void shutdown() throws InterruptedException {
        synchronized (monitor) {
            server.stop();
            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.MINUTES);
            System.out.println("Server shutdown completed");
        }
    }
}
