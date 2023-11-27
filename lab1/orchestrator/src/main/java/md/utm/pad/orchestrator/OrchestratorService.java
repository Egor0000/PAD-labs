package md.utm.pad.orchestrator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrchestratorService {

    private final ObjectMapper objectMapper;
    private final Map<UUID, CompletableFuture<ResponseEntity<?>>> futuresMap = new ConcurrentHashMap<>();
    private final RabbitTemplate rabbitTemplate;
    private final String EXCHANGE = "saga_request";
    private final String COMPENSATE_EXCHANGE = "saga_compensate";


    public Mono<?> processReq(Object incomingObj, String tag) throws Exception {
        // Generate a unique ID for this processing request
        UUID requestId = UUID.randomUUID();

        // Create a CompletableFuture for the processing result and associate it with the unique ID
        CompletableFuture<ResponseEntity<?>> processingResult = new CompletableFuture<>();
        processingResult.orTimeout(10, TimeUnit.SECONDS);
        futuresMap.put(requestId, processingResult);

        // Trigger the RabbitMQ processing with the unique ID
        String incomingObjJson = objectMapper.writeValueAsString(incomingObj);
        String incomingTransactionJson = objectMapper.writeValueAsString(new Transaction());
        Saga saga = new Saga();
        saga.setTag(tag);
        saga.setUuid(requestId);
        saga.setObject(incomingObjJson);
        List<Map.Entry<String, String>> list = new ArrayList<>();
        list.add(new AbstractMap.SimpleEntry<>(incomingTransactionJson, incomingObjJson));
        saga.setPastObjects(list);
        String auctionJson = objectMapper.writeValueAsString(saga);

        processResp(auctionJson);

        // Use Mono.fromFuture to convert the CompletableFuture to Mono
        Mono<ResponseEntity<?>> resultMono = Mono.fromFuture(() -> processingResult);

        // Use reactive operators as needed
        return resultMono.map(responseEntity -> responseEntity)
                .onErrorResume(throwable ->
                        Mono.just(ResponseEntity.badRequest().build()))
                .doFinally(signalType -> futuresMap.remove(requestId));
    }

    public void processResp(String wrapperObj) throws JsonProcessingException {
        Saga saga = objectMapper.readValue(wrapperObj, Saga.class);

        try {
            // based on status, proceed with next saga transaction or record the last result
            if (saga.getStatus() != null) {
                if (saga.getStatus().equals(SagaStatus.Failed)) {
                    compensateTransactions(saga);
                }
                CompletableFuture<ResponseEntity<?>> feature = futuresMap.get(saga.getUuid());
                ResponseEntity<?> responseEntity = ResponseEntity.status(saga.getStatusCode()).body(saga.getStatusBody());
                feature.complete(responseEntity);
            } else {
                saga.setNextStep();
                String nextJson = objectMapper.writeValueAsString(saga);
                rabbitTemplate.convertAndSend(EXCHANGE, saga.getTag(), nextJson);
            }
        } catch (Exception ex) {
            CompletableFuture<ResponseEntity<?>> feature = futuresMap.get(saga.getUuid());
            feature.complete(ResponseEntity.internalServerError().body(ex.toString()));
        }
    }

    public void compensateTransactions(Saga saga) {
        saga.getPastObjects().forEach(stringStringEntry -> {
            String jsonKey = stringStringEntry.getKey();
            try {
                Transaction transaction = objectMapper.readValue(jsonKey, Transaction.class);
                if (transaction.getService() != null) {
                    rabbitTemplate.convertAndSend(COMPENSATE_EXCHANGE, transaction.getService(), stringStringEntry.getValue());
                }
            } catch (JsonProcessingException e) {
                log.error("", e);
            }
        });
    }
}


