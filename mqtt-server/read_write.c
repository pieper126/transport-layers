#include <mosquitto.h>
#include <stdio.h>
#include <string.h>

#define MESSAGE_TO_SENT 10000

const char *topic = "test/topic";
const char *message = "{ \"json\": \"test\" }";
const char name_mosquitto[] = "consumer";

const int qos = 1;
const int keepalive = 60;

struct mosquitto *consumer = {};

int received_counter = 0;

void on_message_count(struct mosquitto *client, void *userdata,
                      const struct mosquitto_message *msg) {
  if (!msg->payloadlen) {
    printf("empty message!");
  }

  received_counter = received_counter + 1;
  printf("%i\n", received_counter);

  if (received_counter == MESSAGE_TO_SENT) {
    printf("%s\n", "done!");
    mosquitto_disconnect(consumer);
    fflush(stdout);
  }
}

int main() {
  int err = mosquitto_lib_init();
  if (err == -1) {
    return -1;
  }

  consumer = mosquitto_new("consumer", true, NULL);

  mosquitto_message_callback_set(consumer, on_message_count);
  mosquitto_int_option(consumer, MOSQ_OPT_SEND_MAXIMUM, MESSAGE_TO_SENT);

  err = mosquitto_connect(consumer, "localhost", 1883, 30);
  if (err != MOSQ_ERR_SUCCESS) {
    printf("error connecting to broker");
    mosquitto_disconnect(consumer);
    mosquitto_destroy(consumer);
    mosquitto_lib_cleanup();
  }

  int rc = mosquitto_subscribe(consumer, NULL, topic, qos);
  if (rc != MOSQ_ERR_SUCCESS) {
    printf("error subscribing to broker");
    mosquitto_disconnect(consumer);
    mosquitto_destroy(consumer);
    mosquitto_lib_cleanup();
  }

  for (int i = 0; i < MESSAGE_TO_SENT; i++) {
    int rc = mosquitto_publish(consumer, NULL, topic, strlen(message), message,
                               qos, false);
    if (rc != MOSQ_ERR_SUCCESS) {
      printf("error sending message to broker");
      mosquitto_disconnect(consumer);
      mosquitto_destroy(consumer);
      mosquitto_lib_cleanup();
      return -1;
    }
  }

  mosquitto_loop_forever(consumer, -1, 1);

  mosquitto_destroy(consumer);
  mosquitto_lib_cleanup();
}
