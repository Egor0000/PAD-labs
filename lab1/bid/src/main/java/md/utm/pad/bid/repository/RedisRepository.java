package md.utm.pad.bid.repository;


import md.utm.pad.bid.entity.AuctionSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RedisRepository extends CrudRepository<AuctionSession, String> {
    List<AuctionSession> findAllByAuctionId(String auctionId);
    List<AuctionSession> findByAuctionId(String auctionId);
    Optional<AuctionSession> findFirstBy(String auctionId);


}
