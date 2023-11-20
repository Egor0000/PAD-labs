package main

import (
    "log"
    "fmt"
    "net/http"
    "net/http/httputil"
    "net/url"
    "github.com/gorilla/mux"
	"io/ioutil"
	"encoding/json"
    "github.com/leemcloughlin/logfile"
    "time"
)

type StatusRecorder struct {
    http.ResponseWriter
    Status int
    Written bool
}

type ServiceInfo struct {
	Address string `json:"address"`
	Tag     string `json:"tag"`
}

var semaphore = make(chan struct{}, 2) // Semaphore with a capacity of 2
var circuitBreakerMap = make(map[string]*CircuitBreaker)
var servers = []string{"http://localhost:8080", "http://localhost:8085"}

func main() {
    configureLogging()

	circuitBreakerMap["bid"] = NewCircuitBreaker()
	circuitBreakerMap["inventory"] = NewCircuitBreaker()


    router := mux.NewRouter()

    router.HandleFunc("/gateway/health", gatewayStatus)

    var target, err = url.Parse("http://localhost:8001")
    if err != nil {
        log.Fatal(err)
    }

    serviceDiscovery := httputil.NewSingleHostReverseProxy(target)

    router.HandleFunc("/auctions/{path:.*}", func(w http.ResponseWriter, r *http.Request) {
		TryServersHandler(w, r, "bid")
	})
    router.HandleFunc("/bids/{path:.*}", func(w http.ResponseWriter, r *http.Request) {
		TryServersHandler(w, r, "bid")
	})
    router.HandleFunc("/products/{path:.*}", func(w http.ResponseWriter, r *http.Request) {
		TryServersHandler(w, r, "inventory")
	})
    router.HandleFunc("/service-discovery/health", proxyHealthCheck(serviceDiscovery))

    router.HandleFunc("/discovery/status", registerService(serviceDiscovery))

    go func() {
        serverAddr := ":8005"
        log.Printf("Reverse Proxy Server is running on %s...", serverAddr)
        log.Fatal(http.ListenAndServe(serverAddr, router))
    }()

    select {}
}

func proxyHealthCheck(proxy *httputil.ReverseProxy) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        select {
        case semaphore <- struct{}{}:
        default:
            w.WriteHeader(http.StatusTeapot)
            return
        }

        defer func() {
            <-semaphore
        }()

        recorder := &StatusRecorder{
            ResponseWriter: w,
            Status:         200,
        }

        proxy.ServeHTTP(recorder, r)

    }
}

func registerService(proxy *httputil.ReverseProxy) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        select {
        case semaphore <- struct{}{}:
        default:
            w.WriteHeader(http.StatusTeapot)
            return
        }

        defer func() {
            <-semaphore
        }()

        proxy.ServeHTTP(w, r)
    }
}

func getNextService(tag string) string {

    var url = "http://localhost:8001/discovery/status?tag="+tag// Replace with your desired URL and key value.

	// Make the GET request to the URL.
	var resp, err = http.Get(url)
	if err != nil {
		log.Println("Error:", err)
		return ""
	}
	defer resp.Body.Close()

	// Check if the response status code is OK (200).
	if resp.StatusCode != http.StatusOK {
		log.Printf("Request failed with status: %s\n", resp.Status)
		return ""
	}

	// Read the response body.
	var body, errUtil = ioutil.ReadAll(resp.Body)
	if errUtil != nil {
		log.Println("Error reading response body:", err)
		return ""
	}

    var serviceInfo ServiceInfo
	err = json.Unmarshal(body, &serviceInfo)
	if err != nil {
        log.Printf("Error parsing the update status requests %s", (err))
		return ""
	}

	serviceAddress := serviceInfo.Address

	// Print the response body as a string.
	log.Println("Response body:", string(body))

    return "http://"+ serviceAddress
}

func gatewayStatus(writer http.ResponseWriter, r *http.Request) {
	log.Println("Request URL: %s, Method: %s\n", r.URL.Path, r.Method)
	writer.Header().Set("Content-Type", "application/json")
	err := json.NewEncoder(writer).Encode("OK")
	if err != nil {
			log.Fatalln("There was an error encoding the initialized struct")
	}
}


func addToCircuitBreaker(circuitBreaker *CircuitBreaker, tag string) {
    if (circuitBreaker.state == Closed) {
        circuitBreaker.Add(tag);
    }
}

func configureLogging() {
    var logFileName = "/app/logs/pad/gateway/log.log"
    if logfile.Defaults.FileName != "" {
        logFileName = logfile.Defaults.FileName
    }
    
    logFile, err := logfile.New(
        &logfile.LogFile{
            FileName: logFileName,
            MaxSize:  10 * 1024 * 1024, // 10Mb duh!
            Flags:    logfile.OverWriteOnStart | logfile.RotateOnStart})
    if err != nil {
        log.Fatalf("Failed to create logFile %s: %s\n", logFileName, err)
    }
    
    log.SetOutput(logFile)
    // defer logFile.Close()
}











func TryServersHandler(w http.ResponseWriter, r *http.Request, tag string) {

    log.Println("Received new request 1")

    circuitBreaker, _ := circuitBreakerMap[tag];

    if circuitBreaker.state != Closed {
        log.Println("Circuit breaker is opened !!! Cannot send more requests")
        w.WriteHeader(400)
        fmt.Fprint(w, "Circuit breaker is opened !!! Cannot send more requests") 
        return
    }

    select {
    case semaphore <- struct{}{}:
    default:
        w.WriteHeader(http.StatusTeapot)
        return
    }

    defer func() {
        <-semaphore
    }()

	// List of servers to try
	for i := 0; i < 3; i++ {
        if circuitBreaker.state != Closed {
            log.Println("Circuit breaker is opened !!! Cannot send more requests")
            w.WriteHeader(400)
            fmt.Fprint(w, "Circuit breaker is opened !!! Cannot send more requests") 
            return
        }

        var nextAddress = getNextService(tag)

        // todo: circuit breaker for service discovery
        var _, err = url.Parse(nextAddress)
        if err != nil {
            log.Fatal(err)
            addToCircuitBreaker(circuitBreaker, tag)
        }

		fmt.Printf("Trying server: %s\n", nextAddress)


		// Create a new HTTP client with a timeout
		client := &http.Client{
			Timeout: 5 * time.Second,
		}

		// Create a new request with the same method and URL as the original request
		req, err := http.NewRequest(r.Method, nextAddress+r.URL.String(), r.Body)
		if err != nil {
			// Handle error, log, or return an error response
			fmt.Printf("Error creating request for server %s: %v\n", nextAddress, err)
			continue
		}

		// Copy headers from the original request to the new request
		req.Header = r.Header

		// Make a request to the current server
		resp, err := client.Do(req)
		if err == nil && resp.StatusCode == http.StatusOK {
            
			defer resp.Body.Close()
			body, _ := ioutil.ReadAll(resp.Body)

			// If the request was successful, respond to the original request with the server's response
			w.WriteHeader(http.StatusOK)
			w.Write(body)
			return
		}

		// Log the error if the request to the current server failed
		fmt.Printf("Error connecting to server %s: %v\n", nextAddress, resp.StatusCode)

        if resp.StatusCode >= 400 {
            addToCircuitBreaker(circuitBreaker, tag)
            circuitBreaker.reroutes += 1 
        }
	}

    if circuitBreaker.state != Closed {
        log.Println("Circuit breaker is opened !!! Cannot send more requests")
        w.WriteHeader(400)
        w.Write([]byte("Circuit breaker is opened !!! Cannot send more requests"))
        return
    }
}











