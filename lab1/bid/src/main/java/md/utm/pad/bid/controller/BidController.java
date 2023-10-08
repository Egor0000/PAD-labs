package md.utm.pad.bid.controller;

import lombok.RequiredArgsConstructor;
import md.utm.pad.bid.dto.BidDto;
import md.utm.pad.bid.service.BidService;
import org.aspectj.weaver.ast.Call;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.Callable;

@RestController
@RequestMapping("${server.api}/bids")
@RequiredArgsConstructor
public class BidController {
    private final BidService bidService;

    @PostMapping("/")
    public Mono<?> create(@RequestBody BidDto bid) {
        return Mono.fromCallable(() -> {
            return ResponseEntity.ok(bidService.create(bid));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/")
    public Mono<?>  getAll() {
        return Mono.fromCallable(() -> {
            return ResponseEntity.ok(bidService.getAll());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/{id}")
    public Mono<?>  get(@PathVariable String id) {
        return Mono.fromCallable(() -> {
            return ResponseEntity.ok(bidService.get(id));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @DeleteMapping("/")
    public Mono<?>  deleteAll() {
        return Mono.fromCallable(() -> {
            bidService.deleteAll();
            return ResponseEntity.ok(200);
        }).subscribeOn(Schedulers.boundedElastic());
    }


}
