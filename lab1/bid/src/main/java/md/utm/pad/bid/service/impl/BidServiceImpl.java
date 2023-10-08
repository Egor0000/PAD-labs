package md.utm.pad.bid.service.impl;

import lombok.RequiredArgsConstructor;
import md.utm.pad.bid.dto.BidDto;
import md.utm.pad.bid.entity.Bid;
import md.utm.pad.bid.mappers.BidMapper;
import md.utm.pad.bid.repository.BidRepository;
import md.utm.pad.bid.service.AuctionService;
import md.utm.pad.bid.service.BidService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {
    private final BidRepository bidRepository;
    private final AuctionService auctionService;
    @Override
    public String create(BidDto bidDto) {
        Bid bid = bidRepository.save(BidMapper.toEntity(bidDto));

        auctionService.updateBid(BidMapper.toDto(bid));

        return bid.getId();
    }

    @Override
    public List<BidDto> getAll() {
        return bidRepository.findAll().stream()
                .map(BidMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BidDto get(String id) {
        return BidMapper.toDto(bidRepository.findById(id).orElse(null));
    }

    @Override
    public void deleteAll() {
        bidRepository.deleteAll();
    }
}
