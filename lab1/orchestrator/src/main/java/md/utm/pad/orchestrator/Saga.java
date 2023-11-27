package md.utm.pad.orchestrator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
public class Saga {
    private UUID uuid;
    // determine which saga should be built
    private String tag;
    private List<String> pastTags = new ArrayList<>();

    // get the step of saga
    private int current;
    // get current object. It defines the next object to be processed.
    private String object;
    private List<Map.Entry<String, String>> pastObjects;
    // status of saga transaction
    private SagaStatus status;
    // response to be sent by orchestrator to client (if needed)
    private int statusCode;
    private String statusBody;

    public void setNextStep() {
        current ++;
    }
}
