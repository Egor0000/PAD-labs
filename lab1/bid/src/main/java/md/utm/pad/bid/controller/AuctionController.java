package md.utm.pad.bid.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.pad.bid.dto.AuctionDto;
import md.utm.pad.bid.service.AuctionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("${server.api}/auctions")
@RequiredArgsConstructor
@Slf4j
public class AuctionController {
    private final AuctionService auctionService;
    @PostMapping("/")
    public Mono<?> create(@RequestBody AuctionDto auction) {
        return auctionService.create(auction).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/")
    public Mono<?> getAll() {
        return Mono.fromCallable(() -> {
            return ResponseEntity.ok(auctionService.getAll());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/{id}")
    public Mono<?> get(@PathVariable String id) {
        return Mono.fromCallable(() -> {
            return ResponseEntity.ok(auctionService.get(id));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @DeleteMapping("/")
    public Mono<?> deleteAll() {
        return Mono.fromCallable(() -> {
            auctionService.deleteAll();
            return ResponseEntity.ok(200);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(value = "/testCallable")
    @Async
    public Mono<?> echoHelloWorld() {
        return Mono.fromCallable(() -> {
            try {
                log.info("ENTERING");
                Thread.sleep(100); // Sleep for 10 seconds (for demonstration purposes)

            } catch (InterruptedException ex) {
                log.warn("interrupted");
            }
            return ResponseEntity.badRequest();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(value = "/testRedis")
    @Async
    public Mono<?> testRedis() {
        return Mono.fromCallable(() -> {
            return ResponseEntity.ok(auctionService.testRedis());
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
