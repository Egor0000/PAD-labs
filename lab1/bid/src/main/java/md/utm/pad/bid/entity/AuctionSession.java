package md.utm.pad.bid.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("AuctionSession")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuctionSession implements Serializable {
    private String id;
    private String auctionId;
    private String maxBidId;
    private Double maxBidAmount;
}
