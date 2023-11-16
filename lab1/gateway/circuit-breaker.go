package main

import (
	"fmt"
	"sync"
	"time"
)

type CircuitBreaker struct {
	mu          sync.Mutex
	requestTime map[string][]time.Time
	failures    int
	state       int
	reroutes 	int
}

const (
	Closed   = iota // Circuit is closed, requests are allowed
	Open             // Circuit is open, requests are blocked
	HalfOpen         // Circuit is partially open, allowing limited requests for testing health
)

func NewCircuitBreaker() *CircuitBreaker {
	fmt.Println("New circuit breaker")
	return &CircuitBreaker{
		requestTime: make(map[string][]time.Time),
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
			ch = make([]time.Time, 0)
			cb.requestTime[address] = ch
		}

		fmt.Println("Request time MAP: ", len(cb.requestTime[address]))

		now := time.Now()

		for len(ch) > 0 && len(ch) < 3 && now.Sub(ch[0]).Seconds() > 5.0*3.5 {
			ch = ch[1:]
		}
		ch = append(ch, now)

		cb.requestTime[address] = ch

		// fmt.Println("Request time MAP 2: ", len(cb.requestTime[address]))

		if len(ch) >= 3 && now.Sub(ch[0]).Seconds() < 5.0*3.5 {
			cb.failures++
			cb.state = Open
			
			go cb.backgroundCheckServiceStatus()

			fmt.Printf("Too many failures for %s !!!\n", address)
			// Clear the channel
			ch = make([]time.Time, 0)
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
		cb.reroutes = 0
		cb.state = Closed
		fmt.Println("Service is OK. Resetting circuit.")
	}
}

