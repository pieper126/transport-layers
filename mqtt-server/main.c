#include <mosquitto.h>
#include <stdatomic.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#define MESSAGE_TO_SENT 100

const char *topic = "test/topic";
//const char *message = "{ \"json\": \"test\" }";
const char *message = "{ \"json\": \"test asdfasdfjakjlsdfhalksjdfhsakljdfhsalkdhfalskjdhfsakljdfhaslkjdfhsalkjdhflkasjdhflaskjdfhdskaljhfkljhafdjlkasfkhsadhjfl\" }";
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

  // printf("0,5");
  struct mosquitto *publisher = mosquitto_new("publisher", true, NULL);
  // struct mosquitto *consumer = mosquitto_new("consumer", false, NULL);

  // printf("1");
  err = mosquitto_connect(publisher, "localhost", 1883, 30);
  if (err != MOSQ_ERR_SUCCESS) {
    printf("error connecting to broker");
    mosquitto_lib_cleanup();
    return -1;
  }
  // printf("2");

  // err = mosquitto_connect(consumer, "localhost", 1883, 30);
  // if (err != MOSQ_ERR_SUCCESS) {
  //   printf("error connecting to broker");
  //   mosquitto_lib_cleanup();
  // }

  // printf("3");
  // mosquitto_message_callback_set(consumer, on_message_print);
  // printf("4");

  // int rc = mosquitto_subscribe(consumer, NULL, topic, qos);
  // if (rc != MOSQ_ERR_SUCCESS) {
  //   printf("error reading message to broker");
  //   mosquitto_disconnect(consumer);
  //   mosquitto_destroy(consumer);
  //   mosquitto_lib_cleanup();
  // }

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

  sleep(10);
  printf("done sending meesages!");

  mosquitto_disconnect(publisher);
  mosquitto_destroy(publisher);
  mosquitto_lib_cleanup();
}
