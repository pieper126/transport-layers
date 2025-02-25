#include <mosquitto.h>
#include <stdio.h>
#include <string.h>

const char *topic = "test/topic";
const char *message = "{ \"json\": \"test\" }";
int qos = 1;
int keepalive = 60;

int received_counter = 0;

void on_message_print(struct mosquitto *client, void *userdata,
                      const struct mosquitto_message *msg) {
  printf("received message!: \n");
  if (msg->payloadlen) {
    printf("%s\n", (char *)msg->payload);
  } else {
    printf("empty message!");
  }

  fflush(stdout);
}

void on_message_count(struct mosquitto *client, void *userdata,
                      const struct mosquitto_message *msg) {
  if (!msg->payloadlen) {
    printf("empty message!");
  }

  received_counter = received_counter + 1;

  printf("%i\n", received_counter);
  fflush(stdout);
}

int main() {
  int err = mosquitto_lib_init();
  if (err == -1) {
    return -1;
  }

  struct mosquitto *consumer = mosquitto_new("consumer", true, NULL);

  mosquitto_message_callback_set(consumer, on_message_count);

  err = mosquitto_connect(consumer, "localhost", 1883, 30);
  if (err != MOSQ_ERR_SUCCESS) {
    printf("error connecting to broker");
    mosquitto_disconnect(consumer);
    mosquitto_destroy(consumer);
    mosquitto_lib_cleanup();
    return 1;
  }

  int rc = mosquitto_subscribe(consumer, NULL, topic, qos);
  if (rc != MOSQ_ERR_SUCCESS) {
    printf("error reading message to broker");
    mosquitto_disconnect(consumer);
    mosquitto_destroy(consumer);
    mosquitto_lib_cleanup();
  }

  mosquitto_loop_forever(consumer, -1, 1);

  mosquitto_disconnect(consumer);
  mosquitto_destroy(consumer);
  mosquitto_lib_cleanup();
}
