package main

import (
	"fmt"
	"sync"
	"time"
)

type CircuitBreaker struct {
	mu          sync.Mutex
	requestTime map[string]chan time.Time
	failures    int
	state       int
}

const (
	Closed   = iota // Circuit is closed, requests are allowed
	Open             // Circuit is open, requests are blocked
	HalfOpen         // Circuit is partially open, allowing limited requests for testing health
)

func NewCircuitBreaker() *CircuitBreaker {
	fmt.Println("New circuit breaker")
	return &CircuitBreaker{
		requestTime: make(map[string]chan time.Time),
		state:       Closed,
	}
}

func (cb *CircuitBreaker) Add(address string) {
	cb.mu.Lock()
	defer cb.mu.Unlock()

	switch cb.state {
	case Closed:
		ch, ok := cb.requestTime[address]
		if !ok {
			ch = make(chan time.Time, 3)
			cb.requestTime[address] = ch
		}

		fmt.Println("Request time MAP: ", len(cb.requestTime[address]))

		if len(cb.requestTime[address]) >=3 {
			fmt.Println("TEST To many errors. Ignored the request")
			// return
		}

		now := time.Now()

		var newCh = make(chan time.Time, 3)

		for len(ch) > 0 && len(ch) <= 3 {
			fmt.Println("LLLLLLLLLLLLLl")

			var oldTimestamp = <-ch

			if now.Sub(oldTimestamp).Seconds() <= 5.0*3.5 {
				newCh <- oldTimestamp
			}
		}

		// go func() {
		// 	for {
		// 		fmt.Println("ndlnwejdnwenldn")
		// 		val, ok := <-newCh
		// 		if !ok {
		// 			// // Source channel closed, close the destination channel
		// 			close(newCh)
		// 			return
		// 		}
		// 		fmt.Println("MMMMMMMMMMMMMMMMMMMm")

		// 		ch <- val
		// 	}

		// 	fmt.Println("FFFFFFFFFFFFFFFFFFFFF")
		// }()

		ch <- now

		fmt.Println("ch size ",len(ch))

		if len(ch) > 3 && now.Sub(<-ch).Seconds() < 5.0*3.5 {
			cb.failures++
			if cb.failures >= 3 {
				cb.state = Open
				go cb.backgroundCheckServiceStatus()
			}
			fmt.Printf("Too many failures for %s !!!\n", address)
			// Clear the channel
			ch = make(chan time.Time, 3)
			cb.requestTime[address] = ch
		}
	case Open:
		fmt.Printf("Circuit is open. Requests blocked for %s\n", address)
	default:
		fmt.Println("Unknown state")
	}
}

func (cb *CircuitBreaker) backgroundCheckServiceStatus() {
	time.Sleep(10 * time.Second) // Adjust this duration based on your requirements

	cb.mu.Lock()
	defer cb.mu.Unlock()

	// Simulate checking service status (replace this with your actual service status check logic)
	serviceIsOK := true

	if serviceIsOK {
		cb.failures = 0
		cb.state = Closed
		fmt.Println("Service is OK. Resetting circuit.")
	}
}

