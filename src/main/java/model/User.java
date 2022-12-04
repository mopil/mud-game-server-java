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

    public synchronized void move(int nx, int ny) {
        String[][] field = Field.getInstance().getField();
        if (Math.abs(nx - x) > 3 || Math.abs(ny - y) > 3) {
            System.out.println("[move error] 3이상 이동할 수 없습니다.");
        } else if (field[nx][ny].equals("_")) {
            x += nx;
            y += ny;
        } else {
            System.out.println("[move error] 해당 칸이 비어있지 않습니다.");
        }
    }

    public synchronized void attack() {
        String[][] field = Field.getInstance().getField();

    }




}
