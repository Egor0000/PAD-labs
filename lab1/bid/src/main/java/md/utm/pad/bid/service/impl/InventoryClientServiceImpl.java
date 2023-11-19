package md.utm.pad.bid.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import md.utm.pad.bid.dto.ProductDto;
import md.utm.pad.bid.service.InventoryClientService;
import md.utm.pad.bid.status.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class InventoryClientServiceImpl implements InventoryClientService {
    private final String gatewayEndpoint;
    private final CircuitBreaker circuitBreaker;
    private final ObjectMapper objectMapper;

    private final WebClient webClient;

    public InventoryClientServiceImpl(@Value("${gateway.endpoint}") String gatewayEndpoint,
                                      CircuitBreaker circuitBreaker, ObjectMapper objectMapper) {
        this.gatewayEndpoint = gatewayEndpoint;
        this.circuitBreaker = circuitBreaker;

        webClient = WebClient.builder()
                .baseUrl(gatewayEndpoint)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<ProductDto> getAndChangeStatus(String productId, boolean status) {

        String uri = UriComponentsBuilder
                .fromPath("/products/lock")
                .queryParam("id", productId)
                .queryParam( "status", true)
                .toUriString();

        return webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .doOnError(x -> {
//                    log.error("Failed. Passing to circuit breaker");
//                    circuitBreaker.add(gatewayEndpoint);
                });

//        return null;
    }
}
