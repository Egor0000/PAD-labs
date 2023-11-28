package main

import (
    "log"
    "net/http"
    "sync"
	"time"
	"encoding/json"
	"io/ioutil"
    "github.com/leemcloughlin/logfile"
)

type ServiceInfo struct {
	Address string `json:"address"`
	Tag     string `json:"tag"`
}


var (
    mutex   = &sync.Mutex{}
	serviceMap = make(map[string][]string)
	heartBeatMap = make(map[ServiceInfo]time.Time)
	healthcheckTimeout = 10 //seconds
)


func getStatusHandler(w http.ResponseWriter, r *http.Request) {
    // Get the attribute (key) from the URL query parameters
    var key = r.URL.Query().Get("tag")

    mutex.Lock()
    defer mutex.Unlock()

    // Check if the key exists in the map
    var value = getNextService(key)
    if value != "" {
		var serviceInfo = ServiceInfo {
			Address:  value,
			Tag: key,
		}

		jsonData, err := json.Marshal(serviceInfo)
        if err != nil {
            http.Error(w, err.Error(), http.StatusInternalServerError)
            return
        }

		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		w.Write(jsonData)
		
        // Send the value as a response
        log.Printf("Value for key %s: %s\n", key, value[0])
    } else {
        // If the key doesn't exist, return an error response
        w.WriteHeader(http.StatusNotFound)
        log.Printf("Key not found: %s\n", key)
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

func getNextService(tag string) string {
    var services = serviceMap[tag]

    if services == nil || len(services) == 0 {
        return ""
    }

    var nextService = services[0]

    services = services[1:]

	services = append(services, nextService)

    serviceMap[tag] = services

    log.Println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&")

    log.Println(serviceMap)

    return nextService
}

func serviceDiscoveryStatus(writer http.ResponseWriter, r *http.Request) {
	log.Println("Request URL: %s, Method: %s\n", r.URL.Path, r.Method)
	writer.Header().Set("Content-Type", "application/json")
	err := json.NewEncoder(writer).Encode("OK")
	if err != nil {
			log.Fatalln("There was an error encoding the initialized struct")
	}
}



func main() {
    configureLogging()

	go heartBeatProcessor()

    http.HandleFunc("/discovery/status", func(w http.ResponseWriter, r *http.Request) {
        if r.Method == http.MethodPost {
            registerService(w, r)
        } else if r.Method == http.MethodGet {
            getStatusHandler(w, r)

        } else {
            w.WriteHeader(http.StatusMethodNotAllowed)
            log.Println("Method not allowed %s \n", w)
        }
    })

	http.HandleFunc("/service-discovery/health", serviceDiscoveryStatus)


	go func() {
		port := ":8888"
		log.Printf("Server is running on port %s... \n", port)
		http.ListenAndServe(port, nil)
    }()

    select {}
}

func configureLogging() {
    var logFileName = "/app/logs/pad/service_discovery/log.log"
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

