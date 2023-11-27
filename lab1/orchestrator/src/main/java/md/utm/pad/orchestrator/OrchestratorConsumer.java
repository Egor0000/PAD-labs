package md.utm.pad.orchestrator;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrchestratorConsumer {
    private final OrchestratorService orchestratorService;

    @RabbitListener(queues = "saga_response")
    public void listen(String in) {

        try {
            orchestratorService.processResp(in);
        } catch (JsonProcessingException ex) {
            log.error("Failed to decode json ", ex);
        } catch (Exception ex) {
            log.error("Failed to process ", ex);
        }
    }
}
