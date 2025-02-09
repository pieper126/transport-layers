package main

import (
	"fmt"
	"net/http"
	"os"
	"os/signal"
	"strings"
	"sync"
	"time"

	"github.com/gorilla/websocket"
)

const amount = 1000
const message = `{ "json": "test" }`

func main() {
	if len(os.Args) != 2 {
		fmt.Println("incorrect amount of args were given: should 1")
		fmt.Println("options: websocket or http")
		return
	}

	chosen_option := os.Args[1]
	switch chosen_option {
	case "websocket":
		callingViaWebsocket()
	case "http":
		callingViaNormallHttp()
	default:
		fmt.Printf("allowed options: websocket or http")
	}
}

func callingViaWebsocket() {
	conn, _, err := websocket.DefaultDialer.Dial("ws://localhost:3000/ctx/echo", nil)
	if err != nil {
		fmt.Printf("err occurred: %e", err)
		return
	}
	defer conn.Close()

	err = conn.WriteMessage(websocket.TextMessage, []byte(message))
	if err != nil {
		fmt.Printf("err occurred: %e", err)
		return
	}

	interupt := make(chan os.Signal, 1)
	signal.Notify(interupt, os.Interrupt)

	go func() {
		for {
			_, msg, err := conn.ReadMessage()
			if err != nil {
				fmt.Printf("err occurred during ReadMessage: %e", err)
				return
			}
			fmt.Println("Received: ", string(msg))

			if string(msg) == string(message) {
				interupt <- os.Interrupt
			}
		}
	}()

	for {
		select {
		case <-interupt:
			fmt.Println("closing connection...")
			conn.WriteMessage(websocket.CloseMessage, websocket.FormatCloseMessage(websocket.CloseNormalClosure, "bye!"))
			time.Sleep(time.Millisecond * 100)
			return
		}
	}
}

func callingViaNormallHttp() {
	client := http.Client{}

	wg := sync.WaitGroup{}

	wg.Add(amount)

	for i := 0; i < amount; i++ {
		go func() {
			defer wg.Done()
			callServer(&client)
		}()
	}

	wg.Wait()
}

func callServer(client *http.Client) {
	bodyString := `{ "json": "test" }`

	reader := strings.NewReader(bodyString)

	res, err := client.Post("http://localhost:3000/ws/echo/", "application/json", reader)
	// res, err := client.Post("http://localhost:3000/echo/", "application/json", reader)
	if err != nil {
		fmt.Printf("err %e", err)
	}

	defer res.Body.Close()

	res_body := make([]byte, 256)
	res.Body.Read(res_body)

	fmt.Printf("res: %s", string(res_body))
}
