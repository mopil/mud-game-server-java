package database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import redis.clients.jedis.JedisPooled;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Redis {
    private static final Redis redis = new Redis();
    private static final JedisPooled jedis = new JedisPooled("localhost", 6379);
    public static Redis getInstance() {
        return redis;
    }

    public void save(String key, String data, long seconds) {
        jedis.set(key, data);
        jedis.expire(key, seconds);
    }


}
