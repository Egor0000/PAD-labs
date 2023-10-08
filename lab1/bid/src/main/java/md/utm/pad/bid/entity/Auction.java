package md.utm.pad.bid.entity;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.springframework.data.annotation.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Document(collection = "auction")
@Data
public class Auction {
    @Id
    private String id;
    private String seller;
    private String productId;
    private String winnerBid;
    private Double startPrice;
    private Double reservePrice;
    private Instant startTime;
    private Instant endTime;
}
