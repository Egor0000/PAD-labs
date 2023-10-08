package md.utm.pad.inventory.service;

import md.utm.pad.inventory.dtos.ProductDto;

import java.util.List;

public interface ProductService {
    ProductDto add(ProductDto productDto);
    List<ProductDto> getAll();
    ProductDto get(String id);
    ProductDto getAndUpdateStatus(String id, boolean status);
    void delete(String id);
}
