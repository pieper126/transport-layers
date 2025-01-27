package com.mycompany.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public class App {
  public static void main(final String[] args) {
    final var port = 3000;

    final var server = new ServerBuilder().build(port);

    System.out.println("Starting server on %s".formatted(port));

    try {
      server.start();
      server.join();
    } catch (final Exception e) {
      System.out.println("errror!");
    }
  }
}

class SimpleHandler extends Handler.Abstract.NonBlocking {
  @Override
  public boolean handle(Request request, Response response, Callback callback) throws Exception {
    response.setStatus(200);
    callback.succeeded();
    return true;
  }
}

class EchoHandler extends Handler.Abstract {
  private final Echo echo = new Echo();

  @Override
  public boolean handle(Request request, Response response, Callback callback) throws Exception {
    if (request.getMethod() != "POST") {
      System.out.println("got non post here namely: %s!".formatted(request.getMethod()));
      return false;
    }

    // echoes the data
    echo.run(request, response, callback);

    response.setStatus(200);
    return true;
  }
}
