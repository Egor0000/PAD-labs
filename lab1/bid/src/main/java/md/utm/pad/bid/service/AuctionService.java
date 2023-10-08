package md.utm.pad.bid.service;

import md.utm.pad.bid.dto.AuctionDto;
import md.utm.pad.bid.dto.BidDto;
import md.utm.pad.bid.entity.Auction;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AuctionService {
    Mono<String> create(AuctionDto auction);

    List<AuctionDto> getAll();

    AuctionDto get(String id);

    void updateWinnerBid(String auctionId, String winnerBid);

    void updateBid(BidDto bidDto);
    void deleteAll();

}
