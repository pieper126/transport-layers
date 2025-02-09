package com.mycompany.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeHandler;

public class ServerBuilder {
  public Server build(int port) {
    final QueuedThreadPool threadPool = new QueuedThreadPool();

    threadPool.setName("server");

    final Server server = new Server(threadPool);

    final ServerConnector connector = new ServerConnector(server);

    connector.setPort(port);

    server.addConnector(connector);

    final var mainHandler = new Handler.Sequence();

    server.setHandler(mainHandler);

    ContextHandler contextHandler = new ContextHandler("/ctx");
    // server.setHandler(contextHandler);

    // Create a WebSocketUpgradeHandler that implicitly creates a
    // ServerWebSocketContainer.

    WebSocketUpgradeHandler webSocketHandler = WebSocketUpgradeHandler.from(server, contextHandler, container -> {
      // Configure the ServerWebSocketContainer.
      container.setMaxTextMessageSize(128 * 1024);

      // Map a request URI to a WebSocket endpoint, for example using a regexp.
      container.addMapping("/*", (rq, rs, cb) -> {
        System.out.println("here!");
        return new EchoWebsocketEndpoint();
      });
    });
    contextHandler.setHandler(webSocketHandler);

    mainHandler.addHandler(contextHandler);
    mainHandler.addHandler(new ContextHandler(new EchoHandler(), "/echo"));
    mainHandler.addHandler(new ContextHandler(new SimpleHandler(), "/health"));
    mainHandler.addHandler(new ContextHandler(new SimpleHandler(), "/ready"));
    mainHandler.addHandler(new ContextHandler(new SimpleHandler(), ""));

    return server;
  }
}
