package md.utm.pad.inventory.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Saga {
    private UUID uuid;
    // determine which saga should be built
    private String tag;
    private List<String> pastTags = new ArrayList<>();

    // get the step of saga
    private int current;
    // get current object. It defines the next object to be processed.
    private List<Map.Entry<String, String>> pastObjects;
    private String object;
    // status of saga transaction
    private SagaStatus status;
    // response to be sent by orchestrator to client (if needed)
    private int statusCode;
    private String statusBody;

    public void setNextStep() {
        current ++;
    }
}
