#include <mosquitto.h>
#include <stdatomic.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#define MESSAGE_TO_SENT 100

const char *topic = "test/topic";
const char *message = "{ \"json\": \"test\" }";
int qos = 1;
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

  struct mosquitto *publisher = mosquitto_new("publisher", true, NULL);

  mosquitto_int_option(publisher, MOSQ_OPT_SEND_MAXIMUM, MESSAGE_TO_SENT);

  err = mosquitto_connect(publisher, "localhost", 1883, 30);
  if (err != MOSQ_ERR_SUCCESS) {
    printf("error connecting to broker");
    mosquitto_lib_cleanup();
    return -1;
  }

  for (int i = 0; i < MESSAGE_TO_SENT; i++) {
    int rc = mosquitto_publish(publisher, NULL, topic, strlen(message), message,
                               qos, false);
    if (rc != MOSQ_ERR_SUCCESS) {
      printf("error sending message to broker");
      mosquitto_disconnect(publisher);
      mosquitto_destroy(publisher);
      mosquitto_lib_cleanup();
      return -1;
    }
  }

  printf("done sending meesages!");

  mosquitto_disconnect(publisher);
  mosquitto_destroy(publisher);
  mosquitto_lib_cleanup();
}
