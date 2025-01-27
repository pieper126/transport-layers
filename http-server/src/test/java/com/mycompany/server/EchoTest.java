package com.mycompany.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.io.Content.Chunk;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;

/**
 * Unit test for simple App.
 */
@ExtendWith(MockitoExtension.class)
public class EchoTest {

  @Mock
  Request request;

  @Mock
  Response response;

  @Mock
  Callback callback;

  @Test
  public void dealsWithFatalFailure() {
    final var error = Chunk.from(new Exception("some error"));

    when(request.read()).thenReturn(error);
    new Echo().run(request, response, callback);
  }

  @Test
  public void dealsWithFailure() {
    final var error = Chunk.from(new Exception("some error"), false);
    final var chunks = generateChunks(1);

    when(request.read()).thenReturn(error, chunks.get(0));
    new Echo().run(request, response, callback);

    final var buffer = chunksToByteBuffer(chunks);
    verify(response).write(eq(true), eq(buffer), any());
  }

  @Test
  public void shouldWaitForNewChunkWhenNullIsGiven() {
    when(request.read()).thenReturn(null);
    new Echo().run(request, response, callback);

    verify(request).demand(any());
  }

  @Test
  public void writesTheLastChunk() {
    final var chunks = generateChunks(1);

    final var echo = new Echo();

    when(request.read()).thenReturn(chunks.get(0));

    echo.run(request, response, callback);

    final var buffer = chunksToByteBuffer(chunks);

    verify(response).write(eq(true), eq(buffer), any());
  }

  @Test
  public void writeIsAbleToDealWithMultiple() {
    final var chunks = generateChunks(2);

    final var echo = new Echo();

    when(request.read())
        .thenReturn(chunks.get(0), chunks.get(1));

    echo.run(request, response, callback);

    final var buffer = chunksToByteBuffer(chunks);

    verify(response).write(eq(true), eq(buffer), any());
  }

  public List<Chunk> generateChunks(int n) {
    if (n < 1) {
      return new ArrayList<Chunk>();
    }

    Random random = new Random();
    final var res = new ArrayList<Chunk>();

    for (int i = 0; i < n - 1; i++) {
      byte[] bytes = new byte[10];
      random.nextBytes(bytes);
      res.add(Chunk.from(ByteBuffer.wrap(bytes), false));
    }

    byte[] bytes = new byte[10];
    random.nextBytes(bytes);
    res.add(Chunk.from(ByteBuffer.wrap(bytes), true));

    return res;
  }

  private ByteBuffer chunksToByteBuffer(List<Chunk> chunks) {
    var finalSize = 0;
    for (Content.Chunk chunk : chunks) {
      finalSize += chunk.getByteBuffer().array().length;
    }

    var finalChunk = new byte[finalSize];
    var offset = 0;
    for (Content.Chunk chunk : chunks) {
      final var currentArrray = chunk.getByteBuffer().array();
      System.arraycopy(currentArrray, 0, finalChunk, offset, currentArrray.length);
      offset += currentArrray.length;
    }

    return ByteBuffer.wrap(finalChunk, 0, finalSize);
  }
}
