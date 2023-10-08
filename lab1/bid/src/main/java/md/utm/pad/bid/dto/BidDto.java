package md.utm.pad.bid.dto;

import lombok.Data;

@Data
public class BidDto {
    private String id;
    private String bidder;
    private String auctionId;
    private Double amount;
}
