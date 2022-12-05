package model;


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
            return false;
        } else if (field.get(nx, ny).equals("_")) {
            x += nx;
            y += ny;
            return true;
        } else {
            return false;
        }
    }




}
