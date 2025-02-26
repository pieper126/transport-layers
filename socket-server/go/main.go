package main

import (
	"fmt"
	"net"
	"os"
	"time"
)

func handleConnection(conn net.Conn) {
	// Print client address
	fmt.Println("Client connected:", conn.RemoteAddr().String())

	// Set a deadline for the connection
	conn.SetDeadline(time.Now().Add(30 * time.Second))

	// Create a buffer to read data
	buffer := make([]byte, 1024)

	// Read data from the connection
	bytesRead, err := conn.Read(buffer)
	if err != nil {
		fmt.Println("Error reading:", err.Error())
		conn.Close()
		return
	}

	// Print received message
	fmt.Printf("Received %d bytes: %s\n", bytesRead, string(buffer[:bytesRead]))

	// Send a response back to the client
	response := "Message received successfully!\n"
	_, err = conn.Write([]byte(response))
	if err != nil {
		fmt.Println("Error writing:", err.Error())
	}

	// Close the connection when finished
	conn.Close()
	fmt.Println("Connection closed:", conn.RemoteAddr().String())
}

func main() {
	// Listen for incoming connections on port 8080
	listener, err := net.Listen("tcp", "localhost:8080")
	if err != nil {
		fmt.Println("Error listening:", err.Error())
		os.Exit(1)
	}
	defer listener.Close()

	fmt.Println("Server started on localhost:8080")

	// Listen for connections in a loop
	for {
		// Accept a connection
		conn, err := listener.Accept()
		if err != nil {
			fmt.Println("Error accepting connection:", err.Error())
			continue
		}

		// Handle the connection in a goroutine for concurrency
		go handleConnection(conn)
	}
}
