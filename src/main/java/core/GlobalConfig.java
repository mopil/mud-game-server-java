package core;

public class GlobalConfig {
    // TCPServer
    public static final String SERVER_IP = "127.0.0.1";
    public static final int SERVER_PORT = 7000;
    public static final int MAX_CONCURRENT_PLAYER_NUM = 30;
    public static final int MAX_TEMP_BYTES = 1024;

    // Redis
    public static final String REDIS_IP = "localhost";
    public static final int REDIS_PORT = 6379;
    public static final int LOGIN_DURATION_SECS = 300;

    // Monster
    public static final String MONSTER_MARK = "S";
    public static final int MAX_MONSTER_COUNT = 10;
    public static final int RESPAWN_CYCLE_SECS = 60;
    public static final int ATTACK_CYCLE_SECS = 5;

    // Field
    public static final String EMPTY_FIELD = "_";
    public static final int FIELD_SIZE = 5;

    // 공격을 위한 8방향 배열
    public static final int[] dx = {0, 0, -1, 1, -1, 1, 1, -1};
    public static final int[] dy = {1, -1, 0, 0, 1, -1, 1, -1};
    public static final int ATTACK_RANGE = 8;

    // User
    public static final int USER_DEFAULT_HP = 30;
    public static final int USER_DEFAULT_STR = 3;
    public static final int USER_DEFAULT_POTION_COUNT = 1;
    public static final int USER_MAX_MOVE_AMOUNT = 3;
}
