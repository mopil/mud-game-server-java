package model;

import util.Logger;

import java.util.Random;

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
        int[] dx = {0, 0, -1, 1, -1, 1, 1, -1};
        int[] dy = {1, -1, 0, 0, 1, -1, 1, -1};
        for (int i = 0; i < 8; i++) {
            int nx = dx[i] + x;
            int ny = dy[i] + y;
            if (!field.get(nx, ny).equals("_") && !field.get(nx, ny).equals("S")) {
                User user = field.getUser(nx, ny);
                user.hp -= str;
                Logger.log(user.username + "이 공격 받았습니다! 체력 -" + str);
            }
        }
    }
}
