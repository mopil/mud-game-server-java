package model;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

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

}
