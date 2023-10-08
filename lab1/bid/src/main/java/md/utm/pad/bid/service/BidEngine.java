package md.utm.pad.bid.service;

import md.utm.pad.bid.dto.AuctionDto;
import md.utm.pad.bid.dto.BidDto;

public interface BidEngine {

    void start(AuctionDto auctionDto);
    void updateBid(BidDto bidDto);
}
