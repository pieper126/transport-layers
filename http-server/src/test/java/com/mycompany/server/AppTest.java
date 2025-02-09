package com.mycompany.server;

import java.time.Duration;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.*;
import org.eclipse.jetty.server.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for simple App.
 */
public class AppTest {

  final static int port = 4000;

  static Server server = null;

  @BeforeAll
  public static void beforeAll() {
    server = new ServerBuilder().build(port);

    try {
      server.start();
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @AfterAll
  public static void afterAll() {
    try {
      server.stop();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void shouldBeAbleToBeCalled() {
    try {
      Request httpRequest = new Request.Builder()
          .url(String.format("http://localhost:%d/", port))
          .get()
          .build();
      final var client = new OkHttpClient();
      final var res = client.newCall(httpRequest).execute();
      assertEquals(200, res.code());
    } catch (Exception e) {
      assertTrue(false);
    }
    assertTrue(true);
  }

  @Test
  public void shouldHaveAHealthEndpoint() {
    try {
      Request httpRequest = new Request.Builder()
          .url(String.format("http://localhost:%d/health/", port))
          .get()
          .build();
      final var client = new OkHttpClient();
      final var res = client.newCall(httpRequest).execute();
      assertEquals(200, res.code());
    } catch (Exception e) {
      assertTrue(false);
    }
    assertTrue(true);
  }

  @Test
  public void shouldHaveAReadyEndpoint() {
    try {
      Request httpRequest = new Request.Builder()
          .url(String.format("http://localhost:%d/ready/", port))
          .get()
          .build();
      final var client = new OkHttpClient();
      final var res = client.newCall(httpRequest).execute();
      assertEquals(200, res.code());
    } catch (Exception e) {
      assertTrue(false);
    }
    assertTrue(true);
  }

  @Test
  public void echoSmallCase() {
    try {
      final var bodyContent = "content";
      RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), bodyContent);

      Request httpRequest = new Request.Builder()
          .url(String.format("http://localhost:%d/echo/", port))
          .post(requestBody)
          .build();

      final var client = new OkHttpClient();
      final var res = client.newCall(httpRequest).execute();
      assertEquals(200, res.code());

      final var resBody = res.body().string();

      assertEquals(bodyContent, resBody);
      res.body().close();

    } catch (Exception e) {
      assertTrue(false);
    }
    assertTrue(true);
  }

  @Test
  public void echoMiddleCase() {
    try {
      StringBuilder stringBuilder = new StringBuilder();

      for (int i = 0; i < 10_000; i++) {
        stringBuilder.append(String.format("something:%d", i));
      }

      final var bodyContent = stringBuilder.toString();

      RequestBody requestBody = RequestBody.create(MediaType.parse("application/pdf"), bodyContent);

      Request httpRequest = new Request.Builder()
          .url(String.format("http://localhost:%d/echo/", port))
          .post(requestBody)
          .build();

      final var client = new OkHttpClient.Builder()
              .connectTimeout(Duration.ofSeconds(30))
              .build();

      final var res = client.newCall(httpRequest).execute();
      assertEquals(200, res.code());

      // final var resBody = res.body().bytes();
      final var resBody = res.body().string();

      assertEquals(bodyContent, resBody);
      res.body().close();

    } catch (Exception e) {
      e.printStackTrace();
      assertTrue(false);
    }
    assertTrue(true);
  }

  @Test
  public void echoBigCase() {
    try {
      final var bodyContent = getClass().getClassLoader().getResourceAsStream("big_test_case.pdf").readAllBytes();

      RequestBody requestBody = RequestBody.create(MediaType.parse("application/pdf"), bodyContent);

      Request httpRequest = new Request.Builder()
          .url(String.format("http://localhost:%d/echo/", port))
          .post(requestBody)
          .build();

      final var client = new OkHttpClient.Builder()
              .connectTimeout(Duration.ofSeconds(30))
              .build();

      final var res = client.newCall(httpRequest).execute();
      assertEquals(200, res.code());

      // final var resBody = res.body().bytes();
      final var resBody = res.body().byteStream().readAllBytes();

      assertEquals(bodyContent.length, resBody.length);
      res.body().close();

    } catch (Exception e) {
      e.printStackTrace();
      assertTrue(false);
    }
    assertTrue(true);
  }

  @Test
  public void testSocket() throws InterruptedException {
    final var client = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    final var url = "ws://localhost:%d/ctx/echo".formatted(port);

    final var request = new Request.Builder().url(url).build();

    final var messageToSent = """
            { "json": "test" }
            """;

    final AtomicBoolean success = new AtomicBoolean(false);

    Exchanger<String> exchanger = new Exchanger<>();

    WebSocketListener listener = new WebSocketListener() {
      @Override
      public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
          try {
              exchanger.exchange(t.getMessage());
          } catch (InterruptedException e) {
              throw new RuntimeException(e);
          }
          super.onFailure(webSocket, t, response);
      }

      @Override
      public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        try {
          if (text.equals(messageToSent)) {
            exchanger.exchange("OK");
          }
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        super.onMessage(webSocket, text);
      }

      @Override
      public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        System.out.println("on open!");
        super.onOpen(webSocket, response);
      }
    };

    final var socket = client.newWebSocket(request, listener);

    socket.send(messageToSent);

    try {
      final var res = exchanger.exchange(null, 1, TimeUnit.SECONDS);
      if (!"OK".equals(res))  {
        fail(res);
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    socket.close(1000, "done with test");
  }
}


