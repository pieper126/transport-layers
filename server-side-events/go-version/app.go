package main

import (
	"bufio"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/valyala/fasthttp"
)

type StreamerCfg struct {
	count   int
	timeout time.Duration
}

func AppBuilder(cfg StreamerCfg) *fiber.App {
	app := fiber.New()

	app.Get("/", func(c *fiber.Ctx) error {
		return c.Status(200).Send(nil)
	})

	s := streamer{count: cfg.count, timeout: cfg.timeout}

	app.Get("/sse", s.addSseHandler)

	return app
}

type streamer struct {
	count   int
	timeout time.Duration
}

func (s *streamer) addSseHandler(c *fiber.Ctx) error {
	c.Set("Content-Type", "text/event-stream")
	c.Set("Cache-Control", "no-cache")
	c.Set("Connection", "keep-alive")
	c.Set("Transfer-Encoding", "chunked")

	c.Status(fiber.StatusOK).Context().SetBodyStreamWriter(fasthttp.StreamWriter(func(sw *bufio.Writer) {
		for i := 0; i < s.count; i++ {
			msg := "{ \"json\": \"test\" }"
			sw.WriteString(msg)
			time.Sleep(s.timeout)
		}

		sw.Flush()
	}))

	return nil
}
