package md.utm.pad.inventory.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.pad.inventory.dtos.*;
import md.utm.pad.inventory.service.ProductService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrchestratorConsumer {
    private final ProductService productService;
    private final ObjectMapper objectMapper;
    private final OrchestratorProducer orchestratorProducer;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga-request-inventory-service", durable = "true"),
            exchange = @Exchange(value = "saga_request", type = "topic"),
            key = "inventory")
    )
    public void listen(String in) {
        try {
            // basic logic. Just to show workign saga. For more bussiness flow, need to refactor the service and new saga fields
            Saga saga = objectMapper.readValue(in, Saga.class);
            try {
                String obj = saga.getObject();
                AuctionDto auctionDto = objectMapper.readValue(obj, AuctionDto.class);
                ProductDto productDto = productService.getAndUpdateStatus(auctionDto);
                String productJson = objectMapper.writeValueAsString(productDto);
                String transactionJson = objectMapper.writeValueAsString(new Transaction("inventory", null));
                List<Map.Entry<String, String>> objects = saga.getPastObjects();
                objects.add(new AbstractMap.SimpleEntry<>(transactionJson, productJson));
                // reset status. Tells orchestrator to continue with transactions and send to bid
                saga.setStatus(null);
                saga.setTag("bid");

                // set past tags to now the compensation mechanism target
                saga.getPastTags().add("inventory");
                    log.info("Sending successful saga transaction {} to orchestrator", saga.getUuid());
            } catch (Exception ex) {
                saga.setStatus(SagaStatus.Failed);
                saga.setStatusCode(500);
                saga.setStatusBody(ex.toString());
                log.error("", ex);
                log.info("Sending failed saga transaction {} to orchestrator", saga.getUuid());
            } finally {
                orchestratorProducer.sendResponse(saga);
            }
        } catch (Exception e) {
            log.error("Failed to processed saga request ", e);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga-compensate-inventory-service", durable = "true"),
            exchange = @Exchange(value = "saga_compensate", type = "topic"),
            key = "inventory")
    )
    public void listenCompensation(String in) {
        try {
            //For now only one method should be compensated, so there is no need to check the endpoint
            ProductDto productDto = objectMapper.readValue(in, ProductDto.class);
            productService.compensateProduct(productDto);
            log.error("Successfully compensated product");
        } catch (Exception e) {
            log.error("Failed to processed saga request ", e);
        }
    }
}
