package com.mycompany.server;

import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;

public class EchoWebsocketEndpoint implements Session.Listener {
  private volatile Session session;

  @Override
  public void onWebSocketOpen(Session session) {
    this.session = session;
    session.setMaxTextMessageSize(16 * 1024);
    session.demand();
  }

  @Override
  public void onWebSocketText(String message) {
    System.out.println("onWebSocketText %s".formatted(message));
    session.sendText(message, Callback.from(session::demand, Throwable::printStackTrace));
  }

  @Override
  public void onWebSocketError(Throwable cause) {
    cause.printStackTrace();
  }

  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    System.out.printf("called close: %n %s%n", statusCode, reason);
    session.close();
  }
}
