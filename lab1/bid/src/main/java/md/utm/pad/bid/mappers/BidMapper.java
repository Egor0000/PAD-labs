package md.utm.pad.bid.mappers;

import md.utm.pad.bid.dto.BidDto;
import md.utm.pad.bid.entity.Bid;

public class BidMapper {

    public static BidDto toDto(Bid bid) {
        if (bid == null) {
            return null;
        }
        BidDto bidDto = new BidDto();
        bidDto.setId(bid.getId());
        bidDto.setAmount(bid.getAmount());
        bidDto.setAuctionId(bid.getAuctionId());
        bidDto.setBidder(bid.getBidder());
        return bidDto;
    }

    public static Bid toEntity(BidDto bidDto) {
        if (bidDto == null) {
            return null;
        }
        Bid bid = new Bid();
        bid.setId(bidDto.getId());
        bid.setAuctionId(bidDto.getAuctionId());
        bid.setAmount(bidDto.getAmount());
        bid.setBidder(bidDto.getBidder());
        return bid;
    }
}
