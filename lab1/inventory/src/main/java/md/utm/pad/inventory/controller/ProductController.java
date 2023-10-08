package md.utm.pad.inventory.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.pad.inventory.dtos.ProductDto;
import md.utm.pad.inventory.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("${server.api}/products")
@Slf4j
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/")
    public Mono<?> create(@RequestBody ProductDto productDto) {
        return Mono.fromCallable(() -> {
            return ResponseEntity.ok(productService.add(productDto));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/")
    public Mono<?> getAll() {
        return Mono.fromCallable(() -> {
            return ResponseEntity.ok(productService.getAll());
        }).subscribeOn(Schedulers.boundedElastic());
    }


    @GetMapping("/{id}")
    public Mono<?> get(@PathVariable String id) {
        return Mono.fromCallable(() -> {
            return ResponseEntity.ok(productService.get(id));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/lock")
    public Mono<?> getAndLock(@RequestParam String id,
                              @RequestParam boolean status) {
        return Mono.fromCallable(() -> {
            return productService.getAndUpdateStatus(id, status);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @DeleteMapping("/{id}")
    public Mono<?> delete(@PathVariable String id) {
        return Mono.fromCallable(() -> {
             productService.delete(id);
             return ResponseEntity.status(200);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
