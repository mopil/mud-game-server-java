package core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import model.User;
import redis.clients.jedis.Jedis;

import static core.GlobalConfig.REDIS_IP;
import static core.GlobalConfig.REDIS_PORT;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Redis {
    private static final Jedis jedis = new Jedis(REDIS_IP, REDIS_PORT);
    private static final String USER_PREFIX = "USER:";
    private static final String STATUS_PREFIX = "STATUS:";

    private static class RedisSingletonHelper {
        private static final Redis INSTANCE = new Redis();
    }

    public static Redis getInstance() {
        return RedisSingletonHelper.INSTANCE;
    }

    public boolean existsUser(String username) {
        String key = USER_PREFIX + username + ":hp";
        return jedis.exists(key);
    }

    public void saveUser(User user) {
        String key = USER_PREFIX + user.username;
        jedis.set(key + ":hp", Integer.toString(user.hp));
        jedis.set(key + ":str", Integer.toString(user.str));
        jedis.set(key + ":x", Integer.toString(user.x));
        jedis.set(key + ":y", Integer.toString(user.y));
        jedis.set(key + ":hpPotionCount", Integer.toString(user.hpPotionCount));
        jedis.set(key + ":strPotionCount", Integer.toString(user.strPotionCount));
    }

    public void deleteUser(User user, int liveSeconds) {
        String key = USER_PREFIX + user.username;
        jedis.expire(key + ":hp", liveSeconds);
        jedis.expire(key + ":str", liveSeconds);
        jedis.expire(key + ":x", liveSeconds);
        jedis.expire(key + ":y", liveSeconds);
        jedis.expire(key + ":hpPotionCount", liveSeconds);
        jedis.expire(key + ":strPotionCount", liveSeconds);
    }

    public String getUserInfo(User user) {
        String key = USER_PREFIX + user.username;
        String hp = jedis.get(key + ":hp");
        String str = jedis.get(key + ":str");
        String x = jedis.get(key + ":x");
        String y = jedis.get(key + ":y");
        String hpPotionCount = jedis.get(key + ":hpPotionCount");
        String strPotionCount = jedis.get(key + ":strPotionCount");
        return "이름:" + user.username +
                " / hp:" + hp +
                " / str:" + str +
                " / 현위치:(" + x + "," + y + ")" +
                " / HP포션:" + hpPotionCount + "개" +
                " / STR포션:" + strPotionCount + "개";
    }

    public void updateUserPosition(User user, int x, int y) {
        String key = USER_PREFIX + user.username;
        jedis.del(key + ":x");
        jedis.del(key + ":y");
        jedis.set(key + ":x", Integer.toString(x));
        jedis.set(key + ":y", Integer.toString(y));
    }

    public void updateUserHp(User user, String type, int amount) {
        String key = USER_PREFIX + user.username + ":hp";
        if (type.equals("up")) jedis.incrBy(key, amount);
        else if (type.equals("down")) jedis.decrBy(key, amount);
    }

    public void updateUserPotionCount(User user, String potionType, String upDownType) {
        String key = USER_PREFIX + user.username;
        switch (potionType) {
            case "hp": key += ":hpPotionCount";
            break;
            case "str": key += ":strPotionCount";
            break;
        }
        if (upDownType.equals("up")) jedis.incrBy(key, 1);
        else if (upDownType.equals("down")) jedis.decrBy(key, 1);
    }

    public void updateUserStr(User user, String upDownType) {
        String key = USER_PREFIX + user.username + ":str";
        if (upDownType.equals("up")) jedis.incrBy(key, 3);
        else if (upDownType.equals("down")) jedis.decrBy(key, 3);
    }

    public void setStatStrPotion(String username) {
        String key = USER_PREFIX + username;
        String statKey = USER_PREFIX + STATUS_PREFIX + username;
        if (jedis.exists(statKey)) {
            jedis.del(statKey);
        }
        jedis.setex(statKey, 60, "str");
    }

    public void deleteAllKeys() {
        jedis.flushAll();
    }
}
