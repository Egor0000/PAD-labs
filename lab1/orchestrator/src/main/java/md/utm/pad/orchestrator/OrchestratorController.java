package md.utm.pad.orchestrator;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orchestrator")
@Controller
@RequiredArgsConstructor
public class OrchestratorController {
    private final OrchestratorService orchestratorService;

    @PostMapping("/auction/")
    public Mono<?> createAuction(@RequestBody AuctionDto auctionDto) throws Exception {
        return orchestratorService.processReq(auctionDto, "inventory");
    }
}
