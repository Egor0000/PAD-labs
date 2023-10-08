package md.utm.pad.bid.entity;


import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("AuctionSession")
@Data
public class AuctionSession {
    private String id;
    private String auctionId;
    private String maxBidId;
    private Double maxBidAmount;
}
