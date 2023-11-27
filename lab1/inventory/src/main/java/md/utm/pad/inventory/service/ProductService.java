package md.utm.pad.inventory.service;

import md.utm.pad.inventory.dtos.AuctionDto;
import md.utm.pad.inventory.dtos.ProductDto;

import java.util.List;

public interface ProductService {
    ProductDto add(ProductDto productDto);
    List<ProductDto> getAll();
    ProductDto get(String id);
    ProductDto getAndUpdateStatus(String id, boolean status);
    ProductDto getAndUpdateStatus(AuctionDto auctionDto);
    void delete(String id);

    void compensateProduct(ProductDto productDto);
}
