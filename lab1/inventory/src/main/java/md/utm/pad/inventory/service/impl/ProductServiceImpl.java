package md.utm.pad.inventory.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.pad.inventory.dtos.AuctionDto;
import md.utm.pad.inventory.dtos.ProductDto;
import md.utm.pad.inventory.entity.Product;
import md.utm.pad.inventory.mapper.ProductMapper;
import md.utm.pad.inventory.repository.ProductRepository;
import md.utm.pad.inventory.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public ProductDto add(ProductDto productDto) {
        if (productDto == null) {
            return null;
        }
        return ProductMapper.toDto(productRepository.save(ProductMapper.toEntity(productDto)));
    }

    @Override
    public List<ProductDto> getAll() {
        log.debug("Entered service");
        return productRepository.findAll().stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDto get(String id) {
        return ProductMapper.toDto(productRepository.findById(id).orElse(null));
    }

    @Override
    public ProductDto getAndUpdateStatus(String id, boolean status) {
        Optional<Product> optionalProduct = productRepository.findById(id);

        if (optionalProduct.isEmpty()) {
            return null;
        }


        Product product = optionalProduct.get();

        if (product.isReserved() && status) {
            log.error("WARNING !!! Product {} is reserved", product.getId());
        }

        product.setReserved(status);

        return ProductMapper.toDto(productRepository.save(product));
    }


    // No logic, just to show working saga. For more logic need to refactor, as by adding saga some flows are changed
    @Override
    public ProductDto getAndUpdateStatus(AuctionDto auctionDto) {
        return getAndUpdateStatus(auctionDto.getProductId(), true);
    }

    @Override
    public void delete(String id) {
         productRepository.deleteById(id);
    }

    @Override
    public void compensateProduct(ProductDto productDto) {
        if (productDto == null) {
            log.error("Null product");
            return;
        }
        getAndUpdateStatus(productDto.getId(), false);
    }
}
