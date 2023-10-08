package md.utm.pad.bid.service;

import md.utm.pad.bid.dto.BidDto;
import md.utm.pad.bid.entity.Bid;

import java.util.List;

public interface BidService {
    String create(BidDto bidDto);
    List<BidDto> getAll();
    BidDto get(String id);
    void deleteAll();
}
