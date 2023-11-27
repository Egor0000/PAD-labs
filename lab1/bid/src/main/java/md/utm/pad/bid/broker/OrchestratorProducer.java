package md.utm.pad.bid.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.pad.bid.dto.Saga;
import md.utm.pad.bid.dto.SagaStatus;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrchestratorProducer {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void sendResponse(Saga saga) {
        try {
            String sagaJson = objectMapper.writeValueAsString(saga);

            rabbitTemplate.convertAndSend("saga_response", sagaJson);
        } catch (Exception ex) {
            log.error("Failed to send saga response ", ex);
        }
    }
}
