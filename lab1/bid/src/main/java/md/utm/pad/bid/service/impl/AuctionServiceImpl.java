package md.utm.pad.bid.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.pad.bid.dto.AuctionDto;
import md.utm.pad.bid.dto.BidDto;
import md.utm.pad.bid.dto.ProductDto;
import md.utm.pad.bid.entity.Auction;
import md.utm.pad.bid.entity.AuctionSession;
import md.utm.pad.bid.mappers.AuctionMapper;
import md.utm.pad.bid.repository.AuctionRepository;
import md.utm.pad.bid.repository.RedisRepository;
import md.utm.pad.bid.service.AuctionService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionServiceImpl implements AuctionService {
    private final AuctionRepository auctionRepository;
    private final RedisRepository sessionCache;
    private final InventoryClientServiceImpl inventoryClientService;
    private final Map<String, Timer> auctionTimer = new ConcurrentHashMap<>();

    @Override
    public Mono<String> create(AuctionDto auction) {
        //kinda of callback hell, but its fine :-)
        return inventoryClientService.getAndChangeStatus(auction.getProductId(), true).flatMap(product -> {
            Auction savedAuction =  auctionRepository.save(AuctionMapper.toEntity(auction));
            AuctionSession auctionSession = new AuctionSession();

            auctionSession.setAuctionId(savedAuction.getId());

            Timer timer = new Timer();

            TimerTask task = new AuctionSessionTask(savedAuction.getId(), this);
            long endMillis = savedAuction.getEndTime().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

            long startMillis = savedAuction.getStartTime().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

            timer.schedule(task, endMillis - startMillis);

            log.info("Scheduling auction ending after {} ms", endMillis - startMillis);

            auctionTimer.put(savedAuction.getId(), timer);

            sessionCache.save(auctionSession);

            log.info("Started the auction! waiting for new bids");

            return Mono.just(savedAuction.getId());
        });
    }

    @Override
    public List<AuctionDto> getAll() {
        return auctionRepository.findAll().stream()
                .map(AuctionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AuctionDto get(String id) {
        return AuctionMapper.toDto(auctionRepository.findById(id).orElse(null));
    }

    @Override
    public void updateWinnerBid(String auctionId, String winnerBid) {
        Optional<Auction> optionalAuction = auctionRepository.findById(auctionId);

        if (optionalAuction.isEmpty()) {
            log.error("Failed to update auction with winner. Auction not found in DB");
            return;
        }

        Auction auction = optionalAuction.get();
        auction.setWinnerBid(winnerBid);

        auctionRepository.save(auction);

        inventoryClientService.getAndChangeStatus(auction.getProductId(), false).subscribe();
    }

    @Override
    public void updateBid(BidDto bidDto) {

        Optional<AuctionSession> optionalAuctionSession = sessionCache.findFirstBy(bidDto.getAuctionId());

        if (optionalAuctionSession.isEmpty()) {
            log.error("No opened auction session with auction id {} found", bidDto.getAuctionId());
            return;
        }
        AuctionSession auctionSession = optionalAuctionSession.get();

        if (auctionSession.getMaxBidAmount() == null || auctionSession.getMaxBidAmount() < bidDto.getAmount()) {
            auctionSession.setMaxBidAmount(bidDto.getAmount());
            auctionSession.setMaxBidId(bidDto.getId());
        }

        sessionCache.save(auctionSession);
        log.info("Update auctionSession {} with a new bid {}", auctionSession, bidDto);
    }

    @Override
    public void deleteAll() {
        auctionRepository.deleteAll();
    }

    private void stopAuction(String auctionId) {
        Optional<AuctionSession> optionalAuctionSession = sessionCache.findFirstBy(auctionId);

        if (optionalAuctionSession.isEmpty()) {
            log.error("No opened auction session with auction id {} found", auctionId);
            return;
        }
        AuctionSession session = optionalAuctionSession.get();

        sessionCache.deleteById(session.getId());

        updateWinnerBid(auctionId, session.getMaxBidId());
        auctionTimer.remove(auctionId);

        log.info("Auction stopped!!! The winner bid is {}", session.getMaxBidId());
    }



    private static class AuctionSessionTask extends TimerTask {
        private final String auctionId;
        private final AuctionServiceImpl auctionService;

        public AuctionSessionTask(String auctionId, AuctionServiceImpl auctionService) {
            this.auctionId = auctionId;
            this.auctionService = auctionService;
        }
        @Override
        public void run() {
            auctionService.stopAuction(auctionId);
        }
    }
}
