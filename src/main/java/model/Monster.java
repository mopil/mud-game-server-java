package model;

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

    }
}
