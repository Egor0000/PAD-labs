package md.utm.pad.bid.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class StatusService {
    private final ScheduledExecutorService statusExecutor = Executors.newSingleThreadScheduledExecutor();
    private final String period;
    private final String address;
    private final String port;
    private final String tag;
    private final ObjectMapper objectMapper;
    private final String serviceDiscoveryEndpoint;
    private final CircuitBreaker circuitBreaker;

    public StatusService(@Value("${service-discovery.period}") String period,
                         @Value("${server.address}") String address,
                         @Value("${server.port}") String port,
                         @Value("${service-discovery.tag}") String tag,
                         @Value("${service-discovery.endpoint}") String serviceDiscoveryEndpoint, CircuitBreaker circuitBreaker) {
        this.period = period;
        this.address = address;
        this.port = port;
        this.tag = tag;
        this.serviceDiscoveryEndpoint = serviceDiscoveryEndpoint;
        this.circuitBreaker = circuitBreaker;
        objectMapper = new ObjectMapper();
        notifyStatus();
    }

    private void notifyStatus() {
        WebClient webClient = WebClient.builder()
                .baseUrl(serviceDiscoveryEndpoint)
                .build();

        statusExecutor.scheduleAtFixedRate(() -> {
            Status status = new Status(address + ":" + port, tag);
            log.debug("Sending service status");
            try {
                webClient.post()
                        .uri("/status") // Replace with your POST endpoint
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(objectMapper.writeValueAsString(status))
                        .retrieve()
                        .bodyToMono(Void.class)
                        .doOnError(x -> {
//                            log.error("Failed. Passing to circuit breaker");
//                            circuitBreaker.add(serviceDiscoveryEndpoint);
                        })
                        .subscribe();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }, 0, Long.parseLong(period), TimeUnit.SECONDS);
    }


    @AllArgsConstructor
    @Getter
    private static class Status {
        private String address;
        private String tag;
    }
}
