package md.utm.pad.inventory.entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "product")
@Data
public class Product {
    @Id
    private String id;
    private String name;
    private Long availableQuantity;
    private String owner;
    private boolean reserved;
}
