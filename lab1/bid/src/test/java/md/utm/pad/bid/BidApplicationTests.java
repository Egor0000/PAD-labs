package md.utm.pad.bid;

import md.utm.pad.bid.entity.AuctionSession;
import md.utm.pad.bid.repository.AuctionRepository;
import md.utm.pad.bid.repository.RedisRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BidApplicationTests {
	@Autowired
	private RedisRepository repository;

	@Test
	void contextLoads() {
	}

	@Test
	void tryRedisCluster() {
		AuctionSession auctionSession = new AuctionSession();
		auctionSession.setAuctionId("1");
		auctionSession.setId("100");
		auctionSession.setMaxBidId("1");
		auctionSession.setMaxBidAmount(10.0);

		AuctionSession auctionSession2 = new AuctionSession();
		auctionSession2.setAuctionId("2");
		auctionSession2.setId("200");
		auctionSession2.setMaxBidId("2");
		auctionSession2.setMaxBidAmount(30.0);

		AuctionSession auctionSession3 = new AuctionSession();
		auctionSession3.setAuctionId("3");
		auctionSession3.setId("300");
		auctionSession3.setMaxBidId("3");
		auctionSession3.setMaxBidAmount(40.0);

		repository.save(auctionSession);
		repository.save(auctionSession2);
		repository.save(auctionSession3);
	}

	@Test
	void tryRedisCluster2() {
		AuctionSession auctionSession = new AuctionSession();
		auctionSession.setAuctionId("1");
		auctionSession.setId("600");
		auctionSession.setMaxBidId("1");
		auctionSession.setMaxBidAmount(10.0);

		AuctionSession auctionSession2 = new AuctionSession();
		auctionSession2.setAuctionId("2");
		auctionSession2.setId("700");
		auctionSession2.setMaxBidId("2");
		auctionSession2.setMaxBidAmount(30.0);

		AuctionSession auctionSession3 = new AuctionSession();
		auctionSession3.setAuctionId("3");
		auctionSession3.setId("800");
		auctionSession3.setMaxBidId("3");
		auctionSession3.setMaxBidAmount(40.0);

		repository.save(auctionSession);
		repository.save(auctionSession2);
		repository.save(auctionSession3);
	}


}
