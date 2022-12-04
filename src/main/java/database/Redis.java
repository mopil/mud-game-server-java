package database;

import redis.clients.jedis.JedisPooled;

public class Redis {
    private static final Redis redis = new Redis();
    private static final JedisPooled jedis = new JedisPooled("localhost", 6379);
    private Redis() {
    }

    public static Redis getInstance() {
        return redis;
    }

    public void save(String key, String data, long seconds) {
        jedis.set(key, data);
        jedis.expire(key, seconds);
    }


}
