package md.utm.pad.bid.repository;

import md.utm.pad.bid.entity.Bid;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BidRepository extends MongoRepository<Bid, String> {
}
