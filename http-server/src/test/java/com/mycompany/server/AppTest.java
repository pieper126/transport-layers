package com.mycompany.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

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

      final var client = new OkHttpClient();
      client.setReadTimeout(30, TimeUnit.SECONDS);

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

      final var client = new OkHttpClient();
      client.setReadTimeout(30, TimeUnit.SECONDS);

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
}
