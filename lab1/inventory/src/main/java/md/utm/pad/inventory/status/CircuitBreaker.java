package md.utm.pad.inventory.status;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Slf4j
public class CircuitBreaker {
    private final Map<String, Queue<Instant>> requestTime = new ConcurrentHashMap<>();
    public void add (String address) {
        Queue<Instant> errorQueues = requestTime.get(address);
        if (errorQueues == null) {
            errorQueues = new ConcurrentLinkedQueue<>();
            errorQueues.add(Instant.now());
            requestTime.put(address, errorQueues);
            return;
        }

        Instant now = Instant.now();

        while (!errorQueues.isEmpty() && errorQueues.size() < 3
                && Duration.between(errorQueues.peek(), now).getSeconds() > 5.0*3.5) {
            errorQueues.poll();
        }

        errorQueues.add(now);

        if (errorQueues.size() > 3 && Duration.between(errorQueues.peek(), now).getSeconds() < 5.0*3.5) {
            log.error("Too many failures for {} !!!", address);
            errorQueues.clear();
        }
    };
}
