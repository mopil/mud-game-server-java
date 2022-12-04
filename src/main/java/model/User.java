package model;

import util.Logger;

public class User {
    public String username;
    public int x;
    public int y;
    public int hp;
    public int str;
    public int hpPotionCount;
    public int strPotionCount;

    public User(String username, int x, int y) {
        this.username = username;
        this.x = x;
        this.y = y;
        this.hp = 30;
        this.str = 3;
        this.hpPotionCount = 1;
        this.strPotionCount = 1;
    }

    public synchronized boolean move(int nx, int ny) {
        Field field = Field.getInstance();
        if (Math.abs(nx - x) > 3 || Math.abs(ny - y) > 3) {
            Logger.log("[move error] 3이상 이동할 수 없습니다.");
            return false;
        } else if (field.get(nx, ny).equals("_")) {
            x += nx;
            y += ny;
            return true;
        } else {
            Logger.log("[move error] 해당 칸이 비어있지 않습니다.");
            return false;
        }
    }




}
