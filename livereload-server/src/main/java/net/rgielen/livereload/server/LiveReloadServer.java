package net.rgielen.livereload.server;

import io.undertow.Undertow;
import io.undertow.util.Headers;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * LiveReloadServer.
 *
 * @author Rene Gielen
 */
public class LiveReloadServer {

    public static final int LIVEREOLAD_DEFAULT_PORT = 35729;

    final int port;
    Undertow server;


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

    Undertow createServer(int port) {
        return Undertow.builder()
                .addHttpListener(port, "localhost", exchange -> {
                    exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("Hello World!");
                })
                .build();
    }


}
