package main

import (
	"fmt"
	"net/http"
	"strings"
	"sync"
)

func main() {
	client := http.Client{}

	wg := sync.WaitGroup{}

	amount := 100_000
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

	res, err := client.Post("http://localhost:3000/echo/", "application/json", reader)
	if err != nil {
		fmt.Printf("err %e", err)
	}

	defer res.Body.Close()

	res_body := make([]byte, 256)
	res.Body.Read(res_body)
}
