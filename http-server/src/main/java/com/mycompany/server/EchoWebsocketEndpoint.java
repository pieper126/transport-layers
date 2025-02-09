package com.mycompany.server;

import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;

public class EchoWebsocketEndpoint implements Session.Listener  {
    private Session session;

    @Override
    public void onWebSocketOpen(Session session) {
        // The WebSocket endpoint has been opened.

        // Store the session to be able to send data to the remote peer.
        this.session = session;

        // You may configure the session.
        session.setMaxTextMessageSize(16 * 1024);

        // You may immediately send a message to the remote peer.
        session.demand();
        //session.sendText("", Callback.from(session::demand, Throwable::printStackTrace));
    }

    @Override
    public void onWebSocketText(String message) {
        session.sendText(message, Callback.from(session::demand, Throwable::printStackTrace));
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace();
        // session.close();
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        System.out.printf("%n %s%n", statusCode, reason);
    }
}
