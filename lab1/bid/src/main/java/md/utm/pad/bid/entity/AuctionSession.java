package md.utm.pad.bid.entity;


import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("AuctionSession")
@Data
public class AuctionSession implements Serializable {
    private String id;
    private String auctionId;
    private String maxBidId;
    private Double maxBidAmount;
}
