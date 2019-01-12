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
import java.util.concurrent.atomic.AtomicBoolean;

import static io.undertow.Handlers.routing;

/**
 * LiveReloadServer.
 *
 * @author Rene Gielen
 */
public class LiveReloadServer {

    public static final int LIVEREOLAD_DEFAULT_PORT = 35729;
    static final String SERVER_HELLO_MESSAGE = "Livereload Server ready - https://github.com/rgielen/livereload";

    private final Object monitor = new Object();
    private final AtomicBoolean exceptionalExit = new AtomicBoolean(false);

    public final int port;
    private final Undertow server;

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

    public boolean isReady() {
        try {
            HttpURLConnection connection = (HttpURLConnection) baseUrl().openConnection();
            connection.connect();
            connection.disconnect();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isFailed() {
        return exceptionalExit.get();
    }

    private Undertow createServer(int port) {
        return Undertow.builder()
                .addHttpListener(port, "0.0.0.0")
                .setHandler(
                        routing()
                                .get("/",
                                        exchange -> {
                                            exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/plain");
                                            exchange.getResponseSender().send(SERVER_HELLO_MESSAGE);
                                        })
                                //.setFallbackHandler()
                )
                .build();
    }

    public void waitUntilReady() {
        while (!(isReady() || isFailed())) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new IllegalStateException("waitUntilReady was interrupted", e);
            }
        }
    }

    public void start() {
        synchronized (monitor) {
            startThreaded();
        }
    }

    public void startAndWait() {
        synchronized (monitor) {
            startThreaded();
            waitUntilReady();
        }
    }

    public void shutdown() throws InterruptedException {
        synchronized (monitor) {
            server.stop();
            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.MINUTES);
            System.out.println("Server shutdown completed");
        }
    }

    private void startThreaded() {
        pool = Executors.newFixedThreadPool(1);
        pool.execute(() -> {
            try {
                server.start();
            } catch (Exception e) {
                exceptionalExit.set(true);
            }
        });
    }
}
