package model;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ToString
public class Field {
    private static final Field fieldInstance = new Field();
    private final String[][] field = new String[30][30];
    private List<User> loginUsers = new ArrayList<>();
    private List<Monster> monsters = new ArrayList<>();

    public static synchronized Field getInstance() {
        return fieldInstance;
    }

    private Field() {
        for (int x = 0; x < 30; x++)
            for (int y = 0; y < 30; y++)
                field[x][y] = "_";
    }

    public synchronized int countMonsters() {
        return monsters.size();
    }

    public synchronized Monster generateMonster() {
        Random random = new Random();
        while (true) {
            int x = random.nextInt(29);
            int y = random.nextInt(29);
            if (field[x][y].equals("_")) {
                field[x][y] = "S";
                Monster monster = new Monster(x, y);
                monsters.add(monster);
                return monster;
            }
        }
    }

    public synchronized String get(int x, int y) {
        return field[x][y];
    }

    public synchronized void set(String who, int x, int y) {
        field[x][y] = who;
    }

    public synchronized void clear(int x, int y) {
        field[x][y] = "_";
    }

    public synchronized void addUser(int x, int y, User user) {
        field[x][y] = user.username;
        loginUsers.add(user);
    }

    public synchronized void clearUser(int x, int y, User user) {
        field[x][y] = "_";
        loginUsers.remove(user);
    }

    public synchronized boolean isEmpty(int x, int y) {
        return field[x][y].equals("_");
    }
    public void show() {
        for (int x = 0; x < 30; x++) {
            for (int y = 0; y < 30; y++) {
                System.out.printf("%s\t", field[x][y]);
            }
            System.out.println();
        }
    }
}
