package md.utm.pad.bid.mappers;

import md.utm.pad.bid.dto.AuctionDto;
import md.utm.pad.bid.entity.Auction;

public class AuctionMapper {
    public static AuctionDto toDto(Auction auction) {
        if (auction == null) {
            return null;
        }
        AuctionDto dto = new AuctionDto();
        dto.setId(auction.getId());
        dto.setSeller(auction.getSeller());
        dto.setWinnerBid(auction.getWinnerBid());
        dto.setProductId(auction.getProductId());
        dto.setStartPrice(auction.getStartPrice());
        dto.setReservePrice(auction.getReservePrice());
        dto.setStartTime(auction.getStartTime());
        dto.setEndTime(auction.getEndTime());
        return dto;
    }
    
    public static Auction toEntity(AuctionDto auctionDto) {
        if (auctionDto == null) {
            return null;
        }
        Auction entity = new Auction();
        entity.setId(auctionDto.getId());
        entity.setWinnerBid(auctionDto.getWinnerBid());

        entity.setSeller(auctionDto.getSeller());
        entity.setProductId(auctionDto.getProductId());
        entity.setStartPrice(auctionDto.getStartPrice());
        entity.setReservePrice(auctionDto.getReservePrice());
        entity.setStartTime(auctionDto.getStartTime());
        entity.setEndTime(auctionDto.getEndTime());
        return entity;
    }
}
