package main

import (
	"fmt"
	"io"
	"net/http"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
)

func TestAbleToConnect(t *testing.T) {
	app := AppBuilder(StreamerCfg{})

	go func() {
		app.Listen(":3000")
	}()

	client := http.Client{}

	res, err := client.Get("http://localhost:3000")
	assert.NoError(t, err)
	assert.NotNil(t, res)

	assert.Equal(t, 200, res.StatusCode)

	app.Shutdown()
}

func TestAbleToReceiveAllEvents(t *testing.T) {
	app := AppBuilder(StreamerCfg{count: 10, timeout: 1 * time.Millisecond})

	go func() {
		app.Listen(":3000")
	}()

	client := http.Client{}

	res, err := client.Get("http://localhost:3000/sse")
	assert.NoError(t, err)
	assert.NotNil(t, res)
	defer res.Body.Close()

	assert.Equal(t, 200, res.StatusCode)

	var buffer = make([]byte, 18)
	var receivedMessages = make([]string, 10)
	var n = 0
	for {
		i, err := res.Body.Read(buffer)
		if i == -1 || i == 0 {
			break
		}
		if err != nil && err != io.EOF {
			assert.NoError(t, err)
			break
		}

		receivedMessages[n] = fmt.Sprintf("%s", buffer)
		n += 1
	}

	assert.Equal(t, 10, n)
	expectedMsg := "{ \"json\": \"test\" }"
	assert.Equal(t, expectedMsg, receivedMessages[0])

	app.Shutdown()
}
