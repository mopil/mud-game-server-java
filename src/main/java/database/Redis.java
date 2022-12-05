package database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import model.User;
import redis.clients.jedis.JedisPooled;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Redis {
    private static final Redis redis = new Redis();
    private static final JedisPooled jedis = new JedisPooled("localhost", 6379);

    public static Redis getInstance() {
        return redis;
    }

    private static final String USER_PREFIX = "USER:";
    private static final String STATUS_PREFIX = "STATUS:";

    public void saveLoginUser(User user, String userDataJson, long seconds) {
        String key = USER_PREFIX + user.username;
        jedis.setex(key, seconds, userDataJson);
    }

    public void setStatStrPotion(String username) {
        String key = USER_PREFIX + username;
        String statKey = USER_PREFIX + STATUS_PREFIX + username;
        if (jedis.exists(statKey)) {
            jedis.del(statKey);
        }
        jedis.setex(statKey, 60, "str");
    }
}
