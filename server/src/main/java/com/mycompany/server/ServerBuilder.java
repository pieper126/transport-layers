package com.mycompany.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

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

    mainHandler.addHandler(new ContextHandler(new EchoHandler(), "/echo"));
    mainHandler.addHandler(new ContextHandler(new SimpleHandler(), "/health"));
    mainHandler.addHandler(new ContextHandler(new SimpleHandler(), "/ready"));
    mainHandler.addHandler(new ContextHandler(new SimpleHandler(), ""));

    return server;
  }
}
