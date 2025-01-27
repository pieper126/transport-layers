#include <mosquitto.h>
#include <stdio.h>

const char *topic = "test/topic";
const char *message = "{ \"json\": \"test\" }";
int qos = 2;
int keepalive = 60;

void on_message_print(struct mosquitto *client, void *userdata,
                      const struct mosquitto_message *msg) {
  printf("received message!");
  if (msg->payloadlen) {
    printf("%s", (char *)msg->payload);
  } else {
    printf("empty message!");
  }
}

int main() {
  int err = mosquitto_lib_init();
  if (err == -1) {
    return -1;
  }

  struct mosquitto *consumer = mosquitto_new("consumer", false, NULL);

  err = mosquitto_connect(consumer, "localhost", 1883, 30);
  if (err != MOSQ_ERR_SUCCESS) {
    printf("error connecting to broker");
    mosquitto_lib_cleanup();
  }

  mosquitto_message_callback_set(consumer, on_message_print);

  int rc = mosquitto_subscribe(consumer, NULL, topic, qos);
  if (rc != MOSQ_ERR_SUCCESS) {
    printf("error reading message to broker");
    mosquitto_disconnect(consumer);
    mosquitto_destroy(consumer);
    mosquitto_lib_cleanup();
  }

  mosquitto_loop_forever(consumer, -1, 1);
}
