package md.utm.pad.inventory.dtos;

import lombok.Data;

import java.time.Instant;

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
