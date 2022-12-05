package core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import model.User;
import redis.clients.jedis.Jedis;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Redis {
    private static final Redis redis = new Redis();
    private static final Jedis jedis = new Jedis("localhost", 6379);

    public static Redis getInstance() {
        return redis;
    }

    private static final String USER_PREFIX = "USER:";
    private static final String STATUS_PREFIX = "STATUS:";

    public void login(User user) {
        String key = USER_PREFIX + user.username;
        jedis.set(key + ":hp",  Integer.toString(user.hp));
        jedis.set(key + ":str",  Integer.toString(user.str));
        jedis.set(key + ":x",  Integer.toString(user.x));
        jedis.set(key + ":y",  Integer.toString(user.y));
        jedis.set(key + ":hpPotionCount",  Integer.toString(user.hpPotionCount));
        jedis.set(key + ":strPotionCount",  Integer.toString(user.strPotionCount));
    }

    public void logout(User user) {
        String key = USER_PREFIX + user.username;
        int liveSecs = 300;
        jedis.expire(key + ":hp", liveSecs);
        jedis.expire(key + ":str",  liveSecs);
        jedis.expire(key + ":x",  liveSecs);
        jedis.expire(key + ":y",  liveSecs);
        jedis.expire(key + ":hpPotionCount",  liveSecs);
        jedis.expire(key + ":strPotionCount",  liveSecs);
    }

    public void move(User user, int x, int y) {
        String key = USER_PREFIX + user.username;
        jedis.del(key + ":x");
        jedis.del(key + ":y");
        jedis.set(key + ":x", Integer.toString(x));
        jedis.set(key + ":y", Integer.toString(y));
    }

    public void minusHp(User user, int amount) {
        String key = USER_PREFIX + user.username + ":hp";
        jedis.decrBy(key, amount);
    }

    public void setStatStrPotion(String username) {
        String key = USER_PREFIX + username;
        String statKey = USER_PREFIX + STATUS_PREFIX + username;
        if (jedis.exists(statKey)) {
            jedis.del(statKey);
        }
        jedis.setex(statKey, 60, "str");
    }

    public void clear() {
        jedis.flushAll();
    }
}
