package md.utm.pad.bid.entity;

import org.springframework.data.annotation.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "bid")
public class Bid {
    @Id
    private String id;
    private String bidder;
    private String auctionId;
    private Double amount;
}
