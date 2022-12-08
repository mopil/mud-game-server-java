package core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import model.User;
import redis.clients.jedis.Jedis;

import java.util.Map;

import static core.GlobalConfig.REDIS_IP;
import static core.GlobalConfig.REDIS_PORT;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Redis {
    private static final Jedis jedis = new Jedis(REDIS_IP, REDIS_PORT);
    private static final String USER_PREFIX = "USER:";

    private static class RedisSingletonHelper {
        private static final Redis INSTANCE = new Redis();
    }

    public static Redis getInstance() {
        return RedisSingletonHelper.INSTANCE;
    }

    public void saveUser(User user) {
        String key = USER_PREFIX + user.username;
        jedis.hset(key, "username", user.username);
        jedis.hset(key, "hp", Integer.toString(user.hp));
        jedis.hset(key, "str", Integer.toString(user.str));
        jedis.hset(key, "x", Integer.toString(user.x));
        jedis.hset(key, "y", Integer.toString(user.y));
        jedis.hset(key, "hpPotionCount", Integer.toString(user.hpPotionCount));
        jedis.hset(key, "strPotionCount", Integer.toString(user.strPotionCount));
    }

    public void deleteUser(User user, int liveSeconds) {
        String key = USER_PREFIX + user.username;
        jedis.expire(key, liveSeconds);
    }

    public User getUser(String username) {
        String key = USER_PREFIX + username;
        Map<String, String> properties = jedis.hgetAll(key);
        try {
            String hp = properties.get("hp");
            String str = properties.get("str");
            String x = properties.get("x");
            String y = properties.get("y");
            String hpPotionCount = properties.get("hpPotionCount");
            String strPotionCount = properties.get("strPotionCount");
            return User.builder()
                    .username(username)
                    .hp(Integer.parseInt(hp))
                    .str(Integer.parseInt(str))
                    .x(Integer.parseInt(x))
                    .y(Integer.parseInt(y))
                    .hpPotionCount(Integer.parseInt(hpPotionCount))
                    .strPotionCount(Integer.parseInt(strPotionCount))
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    public String getUserInfo(User user) {
        User findUser = getUser(user.username);
        return "이름:" + user.username +
                " / hp:" + findUser.hp +
                " / str:" + findUser.str +
                " / 현위치:(" + findUser.x + "," + findUser.y + ")" +
                " / HP 포션:" + findUser.hpPotionCount + "개" +
                " / STR 포션:" + findUser.strPotionCount + "개";
    }

    public void updateUserPosition(User user, int x, int y) {
        String key = USER_PREFIX + user.username;
        jedis.hset(key, "x", Integer.toString(x));
        jedis.hset(key, "y", Integer.toString(y));
    }

    public void updateUserHp(User user, String type, int amount) {
        String key = USER_PREFIX + user.username;
        if (type.equals("down")) amount = amount * -1;
        jedis.hincrBy(key, "hp", amount);
    }

    public void updateUserPotionCount(User user, String potionType, int amount) {
        String key = USER_PREFIX + user.username;
        jedis.hset(key, potionType + "PotionCount", Integer.toString(amount));
    }

    public void updateUserStr(User user, String upDownType) {
        String key = USER_PREFIX + user.username;
        if (upDownType.equals("up")) jedis.hincrBy(key, "str", 3);
        else if (upDownType.equals("down")) jedis.hincrBy(key, "str",-3);
    }

    public void deleteAllKeys() {
        jedis.flushAll();
    }
}
