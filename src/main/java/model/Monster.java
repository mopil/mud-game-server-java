package model;


import core.Redis;
import core.SocketManager;

import java.util.Random;

import static model.Field.FIELD_SIZE;

public class Monster {
    public int x;
    public int y;
    public int hp;
    public int str;
    public int potion; // 0: hp, 1: str

    public Monster(int x, int y) {
        Random random = new Random();
        this.x = x;
        this.y = y;
        this.hp = random.nextInt(5, 10);
        this.str = random.nextInt(3, 5);
        this.potion = random.nextInt(0, 1);
    }

    public void attack() {
        Field field = Field.getInstance();
        Redis redis = Redis.getInstance();
        int[] dx = {0, 0, -1, 1, -1, 1, 1, -1};
        int[] dy = {1, -1, 0, 0, 1, -1, 1, -1};
        for (int i = 0; i < 8; i++) {
            int nx = dx[i] + x;
            int ny = dy[i] + y;
            if (nx < 0 || nx >= FIELD_SIZE || ny < 0 || ny >= FIELD_SIZE) continue;
            if (!field.get(nx, ny).equals("_") && !field.get(nx, ny).equals("S")) {
                User user = field.getUser(nx, ny);
                user.hp -= str;
                Redis.getInstance().updateUserHp(user, "down", str);
                String message = "\n\"슬라임\"이 \"" + user.username + "\"을 공격해서 데미지 " + str + "을/를 가했습니다!";
                String message2 = " (" + user.username + "의 남은 hp:" + user.hp + ")";
                SocketManager.sendDirectResponse(user.username, message + message2);
                if (user.hp <= 0) {
                    SocketManager.sendDirectResponse(user.username, "hp가 0이 되어서 사망했습니다.. 게임을 종료합니다.");
                    SocketManager.disconnect(user.username);
                    redis.deleteUser(user, 0);
                }
            }
        }
    }
}
