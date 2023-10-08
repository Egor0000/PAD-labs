package md.utm.pad.bid.repository;

import md.utm.pad.bid.entity.Auction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends MongoRepository<Auction, String> {
}
