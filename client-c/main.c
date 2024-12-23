#include <bits/time.h>
#include <curl/curl.h>
#include <curl/easy.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
#include <unistd.h>

#define MAX_PARALLEL 10

#define AMOUNT_OF_CALLS 1

static const char *body = "{ \"json\": \"test\" }";

int request_done = 0;

size_t write_ignore_callback(void *ptr, size_t size, size_t nmemb,
                             void *userdata) {
  // printf("size: %d \n", size);
  // printf("numbers: %d \n", nmemb);
  // printf("length body: %d \n", strlen(body));
  return size * nmemb;
}

void callServer() {
  CURL *curl = curl_easy_init();
  if (curl == NULL) {
    printf("curl!");
    return;
  }

  CURLcode res;
  curl_easy_setopt(curl, CURLOPT_URL, "http://localhost:3000/echo/");
  curl_easy_setopt(curl, CURLOPT_POSTFIELDS, body);
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_ignore_callback);
  res = curl_easy_perform(curl);

  if (res != CURLE_OK) {
    printf("something went not correctly! \n");
    printf("err: %s \n", curl_easy_strerror(res));
    printf("err at %d\n", request_done);
  }

  request_done += 1;
  curl_easy_cleanup(curl);
}

int main() {
  for (int i = 0; i < AMOUNT_OF_CALLS; i++) {
    // struct timespec start, end;
    // double time_taken;

    // clock_gettime(CLOCK_MONOTONIC, &start);

    callServer();

    // clock_gettime(CLOCK_MONOTONIC, &end);

    // double diff = (end.tv_nsec - start.tv_nsec) / 1e6;

    // printf("time past: %f \n", diff);
  }

  return 0;
}
