package main

import (
    "log"
    "net/http"
    "net/http/httputil"
    "net/url"
    "github.com/gorilla/mux"
	"io/ioutil"
	"sync"
	"encoding/json"
    "time"
)

type ServiceInfo struct {
	Address string `json:"address"`
	Tag     string `json:"tag"`
}

var semaphore = make(chan struct{}, 2) // Semaphore with a capacity of 2
var mutex sync.Mutex
var serviceMap = make(map[string][]string)
var roundRobinMap = make(map[string][]string)
var heartBeatMap = make(map[ServiceInfo]time.Time)
var healthcheckTimeout = 10 //seconds


func main() {
    go heartBeatProcessor()
    router := mux.NewRouter()

    router.HandleFunc("/gateway/health", gatewayStatus)

    bidService := httputil.NewSingleHostReverseProxy(nil)
    invetoryService := httputil.NewSingleHostReverseProxy(nil)

    router.HandleFunc("/auctions/{path:.*}", reverseProxyHandler(bidService, "bid"))
    router.HandleFunc("/bids/{path:.*}", reverseProxyHandler(bidService, "bid"))
    router.HandleFunc("/products/{path:.*}", reverseProxyHandler(invetoryService, "inventory"))

    router.HandleFunc("/discovery/status", registerService)

    go func() {
        serverAddr := "0.0.0.0:8005"
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

        log.Println("Received new request 2")


        var nextAddress = getNextService(tag, proxy)

        // todo: error handling when nextAddress is empty
        var target, err = url.Parse(nextAddress)
        if err != nil {
            log.Fatal(err)
        }

        proxy := httputil.NewSingleHostReverseProxy(target)

        proxy.ServeHTTP(w, r)
    }
}

func registerService(w http.ResponseWriter, r *http.Request) {
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		http.Error(w, "Error reading request body", http.StatusInternalServerError)
		return
	}
	defer r.Body.Close()


	var serviceInfo ServiceInfo
	err = json.Unmarshal(body, &serviceInfo)
	if err != nil {
        log.Printf("Error parsing the update status requests %s", (err))
		http.Error(w, "Error parsing JSON body", http.StatusBadRequest)
		return
	}

	serviceAddress := serviceInfo.Address

	log.Printf("endpoint update from %s for %s service", serviceAddress, serviceInfo.Tag)

	mutex.Lock()
	defer mutex.Unlock()


    var found = false

    if serviceMap[serviceInfo.Tag]== nil {
        serviceMap[serviceInfo.Tag] = []string{serviceAddress}
    } else {
        for _, v := range serviceMap[serviceInfo.Tag] {
            if v == serviceAddress {
                found = true
                break
            }
        }
    
        // If not found, append the element to the list
        if !found {
            serviceMap[serviceInfo.Tag] = append(serviceMap[serviceInfo.Tag], serviceAddress)
        }
    }

    //heartbeat
    heartBeatMap[serviceInfo] = time.Now()
}

func heartBeatProcessor() {
	for {
		log.Println("Heartbeat processing is running...")
        keysToDelete := []ServiceInfo{}

        for serviceInfo, registeredTime :=range heartBeatMap {
            currentTime := time.Now()
			timeDifference := currentTime.Sub(registeredTime)
            if timeDifference > time.Duration(healthcheckTimeout)*time.Second {
                log.Printf("Endpoint healtcheck timeout: %s: %v. Scheduled for deletion from service discovery map\n", serviceInfo.Address, timeDifference)
                keysToDelete = append(keysToDelete, serviceInfo)
            }
        }


        for _, key := range keysToDelete {

            mutex.Lock()
            
            delete(heartBeatMap, key)

            if arr, exists := serviceMap[key.Tag]; exists {
                for i, item := range arr {
                    if item == key.Address {
                        serviceMap[key.Tag] = append(arr[:i], arr[i+1:]...)
                        break
                    }
                }
            }

            mutex.Unlock()


            log.Println(serviceMap)
        }


		time.Sleep(2 * time.Second)
	}
}

func getNextService(tag string, proxy *httputil.ReverseProxy) string {
    var services = serviceMap[tag]

    if services == nil || len(services) == 0 {
        return ""
    }

    var nextService = services[0]

    services = services[1:]

	services = append(services, nextService)

    serviceMap[tag] = services

    log.Println(serviceMap)

    return "http://" + nextService
}

func gatewayStatus(writer http.ResponseWriter, r *http.Request) {
	log.Println("Request URL: %s, Method: %s\n", r.URL.Path, r.Method)
	writer.Header().Set("Content-Type", "application/json")
	err := json.NewEncoder(writer).Encode("OK")
	if err != nil {
			log.Fatalln("There was an error encoding the initialized struct")
	}
}