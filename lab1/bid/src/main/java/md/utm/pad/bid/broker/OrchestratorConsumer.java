package md.utm.pad.bid.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.pad.bid.dto.Saga;
import md.utm.pad.bid.dto.SagaStatus;
import md.utm.pad.bid.service.AuctionService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrchestratorConsumer {
    private final OrchestratorProducer orchestratorProducer;
    private final ObjectMapper objectMapper;
    private final AuctionService auctionService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga-request-bid-service", durable = "true"),
            exchange = @Exchange(value = "saga_request", type = "topic"),
            key = "bid")
    )
    public void listen(String in) {
        try {
            Saga saga = objectMapper.readValue(in, Saga.class);

            try {
                String res = auctionService.create(saga);
                saga.setStatusBody(res);
                saga.setStatusCode(200);
                saga.setStatusBody(res);
                saga.setStatus(SagaStatus.OK);
            } catch (Exception ex) {
                saga.setStatus(SagaStatus.Failed);
                saga.setStatusCode(500);
                saga.setStatusBody(ex.toString());
            } finally {
                log.info("Sending saga transaction {} to orchestrator", saga.getUuid());
                orchestratorProducer.sendResponse(saga);
            }

        } catch (Exception e) {
            log.error("Failed to processed saga request ", e);
        }
    }
}
