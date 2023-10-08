package md.utm.pad.bid.dto;

import jakarta.persistence.Id;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class AuctionDto {
    private String id;
    private String seller;
    private String productId;
    private String winnerBid;
    private Double startPrice;
    private Double reservePrice;
    private Instant startTime;
    private Instant endTime;
}
