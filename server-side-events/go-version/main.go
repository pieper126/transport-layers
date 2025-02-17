package main

import (
	"fmt"
	"time"
)

func main() {
	streamerCfg := StreamerCfg{count: 1_000, timeout: 0 * time.Millisecond}
	app := AppBuilder(streamerCfg)

	fmt.Println("listening on port 3000")
	app.Listen(":3000")
}
