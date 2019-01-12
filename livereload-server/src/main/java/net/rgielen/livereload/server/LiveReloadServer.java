package net.rgielen.livereload.server;

import io.undertow.Undertow;
import io.undertow.util.Headers;

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

    Undertow createServer(int port) {
        return Undertow.builder()
                .addHttpListener(port, "localhost", exchange -> {
                    exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("Hello World!");
                })
                .build();
    }


}
