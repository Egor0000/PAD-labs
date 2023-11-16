package md.utm.pad.bid.repository;

import md.utm.pad.bid.entity.AuctionSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SessionRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public SessionRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public AuctionSession addSession(AuctionSession session){
        redisTemplate.opsForValue().set(session.getId(), session, Duration.ofMinutes(10L));
        return session;
    }

    public AuctionSession getSessionById(String id){
        var session = redisTemplate.opsForValue().get(id);
        return (AuctionSession) session;
    }
}
