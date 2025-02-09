package com.mycompany.server;

import org.eclipse.jetty.io.ByteBufferAccumulator;
import org.eclipse.jetty.io.Content.Chunk;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.websocket.api.Session.Listener;


public class Echo {
  private ByteBufferAccumulator acc;

  public Echo() {
    acc = new ByteBufferAccumulator();
  }

  public void run(Request request, Response response, Callback callback) {
    while (true) {
      Chunk chunk = request.read();

      if (chunk == null) {
        request.demand(() -> this.run(request, response, callback));
        return;
      }

      if (Chunk.isFailure(chunk)) {
        if (chunk.isLast()) {
          return;
        } else {
          continue;
        }
      }

      acc.copyBuffer(chunk.getByteBuffer());

      chunk.release();

      if (chunk.isLast()) {
        response.write(true, acc.takeByteBuffer(), callback);
        return;
      }
    }
  }
}
