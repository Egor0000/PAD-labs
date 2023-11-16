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

func main() {
    configureLogging()

	circuitBreakerMap["bid"] = NewCircuitBreaker()
	circuitBreakerMap["inventory"] = NewCircuitBreaker()

    router := mux.NewRouter()

    router.HandleFunc("/gateway/health", gatewayStatus)

    bidService := httputil.NewSingleHostReverseProxy(nil)
    invetoryService := httputil.NewSingleHostReverseProxy(nil)

    var target, err = url.Parse("http://localhost:8001")
    if err != nil {
        log.Fatal(err)
    }

    serviceDiscovery := httputil.NewSingleHostReverseProxy(target)

    router.HandleFunc("/auctions/{path:.*}", reverseProxyHandler(bidService, "bid"))
    router.HandleFunc("/bids/{path:.*}", reverseProxyHandler(bidService, "bid"))
    router.HandleFunc("/products/{path:.*}", reverseProxyHandler(invetoryService, "inventory"))
    router.HandleFunc("/service-discovery/health", proxyHealthCheck(serviceDiscovery))

    router.HandleFunc("/discovery/status", registerService(serviceDiscovery))

    go func() {
        serverAddr := ":8005"
        log.Printf("Reverse Proxy Server is running on %s...", serverAddr)
        log.Fatal(http.ListenAndServe(serverAddr, router))
    }()

    select {}
}

func reverseProxyHandler(proxy *httputil.ReverseProxy, tag string) http.HandlerFunc {
    log.Println("Received new request")
    return func(w http.ResponseWriter, r *http.Request) {
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

        var proxyStatus = 400

        // for proxyStatus != 200 && circuitBreaker.reroutes <= 3 {

            var nextAddress = getNextService(tag)

            // todo: circuit breaker for service discovery
            var target, err = url.Parse(nextAddress)
            if err != nil {
                log.Fatal(err)
                addToCircuitBreaker(circuitBreaker, tag)
            }

            proxy := httputil.NewSingleHostReverseProxy(target)


            recorder := &StatusRecorder{
                ResponseWriter: w,
                Status:         200,
            }

            log.Println("|||||||||||||||||||||||||||\\")

            proxy.ServeHTTP(recorder, r)

            log.Println("Received status code for operation: ", recorder.Status, proxy, proxyStatus)

            proxyStatus = recorder.Status
    
            if recorder.Status >= 400 {
                addToCircuitBreaker(circuitBreaker, tag)
                circuitBreaker.reroutes += 1 
            }
        // }
    }
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


func ErrHandle(res http.ResponseWriter, req *http.Request, err error) {
    res.WriteHeader(http.StatusBadGateway)
}     

func (r *StatusRecorder) WriteHeader(status int) {
    r.Status = status
    // r.ResponseWriter.WriteHeader(status)
    r.Written = true
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
