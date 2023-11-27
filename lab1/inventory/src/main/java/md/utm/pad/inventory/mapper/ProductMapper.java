package md.utm.pad.inventory.mapper;


import md.utm.pad.inventory.dtos.ProductDto;
import md.utm.pad.inventory.entity.Product;

public class ProductMapper {
    public static ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setOwner(product.getOwner());
        productDto.setAvailableQuantity(product.getAvailableQuantity());
        productDto.setReserved(product.isReserved());
        return productDto;
    }

    public static Product toEntity(ProductDto productDto) {
        if (productDto == null) {
            return null;
        }
        Product product = new Product();
        product.setId(productDto.getId());
        product.setAvailableQuantity(productDto.getAvailableQuantity());
        product.setName(productDto.getName());
        product.setOwner(productDto.getOwner());
        product.setReserved(productDto.isReserved());
        return product;
    }
}
