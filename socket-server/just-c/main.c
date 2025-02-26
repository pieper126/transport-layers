#include <arpa/inet.h>
#include <asm-generic/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#define PORT 8080
#define IP_ADDRESS "127.0.0.1"
#define BACKLOG 5
#define ANY_PROTOCOL 0
#define MAX_BUFFER 1024

const int yes = 1;

int main(int argc, char *argv[]) {
  int socket_fd;
  struct sockaddr_in address;
  address.sin_addr.s_addr = inet_addr(IP_ADDRESS);
  address.sin_port = htons(PORT);
  address.sin_family = AF_INET;

  int addr_len = sizeof(address);
  char buffer[MAX_BUFFER] = {0};

  socket_fd = socket(AF_INET, SOCK_STREAM, ANY_PROTOCOL);
  if (socket_fd == -1) {
    perror("starting socket!");
    return 1;
  }

  if (setsockopt(socket_fd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof yes)) {
    perror("set socket options");
    return 1;
  }

  if (bind(socket_fd, (struct sockaddr *)&address, addr_len) < 0) {
    perror("bind");
    return 1;
  }

  if (listen(socket_fd, BACKLOG) < 0) {
    perror("listening to socket failed!");
    close(socket_fd);
    return 1;
  }

  printf("listening for connections...\n");

  struct sockaddr client_addr = {0};
  socklen_t client_len = sizeof(client_addr);
  int connected_socket =
      accept(socket_fd, (struct sockaddr *)&client_addr, &client_len);
  if (connected_socket < 0) {
    perror("error accepting socket connection!");
    close(socket_fd);
    return 1;
  }

  printf("accepted connection!\n");

  ssize_t res_len = recv(connected_socket, buffer, MAX_BUFFER, 0);
  if (res_len < 0) {
    perror("error reading message");
    close(connected_socket);
    close(socket_fd);
    return 1;
  }

  printf("data received\n");
  printf("%s", buffer);

  send(connected_socket, buffer, res_len, 0);

  close(connected_socket);
  close(socket_fd);

  return 0;
}
