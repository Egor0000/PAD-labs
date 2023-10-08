package md.utm.pad.bid.service;

import md.utm.pad.bid.dto.ProductDto;
import reactor.core.publisher.Mono;

public interface InventoryClientService {
    Mono<ProductDto> getAndChangeStatus(String productId, boolean status);
}
