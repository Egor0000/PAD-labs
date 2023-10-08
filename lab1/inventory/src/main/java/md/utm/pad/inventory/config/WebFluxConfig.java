package md.utm.pad.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
public class WebFluxConfig {

    @Bean
    public WebFilter globalTimeoutFilter() {
        Duration timeoutDuration = Duration.ofSeconds(2);

        return (exchange, chain) -> {
            Mono<Void> timeoutMono = Mono.delay(timeoutDuration)
                    .flatMap(time -> {
                        exchange.getResponse().setStatusCode(HttpStatus.REQUEST_TIMEOUT);
                        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                                .bufferFactory().wrap("Request timed out".getBytes())));
                    });

            return Mono.first(chain.filter(exchange), timeoutMono);
        };
    }
}
