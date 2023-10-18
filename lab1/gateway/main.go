package main

import (
    "log"
    "net/http"
    "net/http/httputil"
    "net/url"
    "github.com/gorilla/mux"
	"io/ioutil"
	"encoding/json"
)


type ServiceInfo struct {
	Address string `json:"address"`
	Tag     string `json:"tag"`
}

var semaphore = make(chan struct{}, 2) // Semaphore with a capacity of 2

func main() {
    // go heartBeatProcessor()
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

        select {
        case semaphore <- struct{}{}:
        default:
            w.WriteHeader(http.StatusTeapot)
            return
        }

        defer func() {
            <-semaphore
        }()

        var nextAddress = getNextService(tag)

        // todo: error handling when nextAddress is empty
        var target, err = url.Parse(nextAddress)
        if err != nil {
            log.Fatal(err)
        }

        proxy := httputil.NewSingleHostReverseProxy(target)

        proxy.ServeHTTP(w, r)
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

        proxy.ServeHTTP(w, r)
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