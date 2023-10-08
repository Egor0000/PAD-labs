package md.utm.pad.bid.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ProductDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private Long availableQuantity;
    private String owner;
    private boolean reserved;
}
