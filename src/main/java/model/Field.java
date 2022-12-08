package model;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static core.GlobalConfig.*;

@ToString
@Data
public class Field {
    private final String[][] field = new String[FIELD_SIZE][FIELD_SIZE];
    private List<User> loginUsers = new ArrayList<>();
    private List<Monster> monsters = new ArrayList<>();

    private static class FieldSingletonHelper {
        private static final Field INSTANCE = new Field();
    }

    public static Field getInstance() {
        return FieldSingletonHelper.INSTANCE;
    }

    private Field() {
        for (int x = 0; x < FIELD_SIZE; x++)
            for (int y = 0; y < FIELD_SIZE; y++)
                field[x][y] = EMPTY_FIELD;
    }

    /**
     * Monster
     */
    public synchronized int countMonsters() {
        return monsters.size();
    }

    public synchronized Monster generateMonster() {
        Random random = new Random();
        while (true) {
            int x = random.nextInt(FIELD_SIZE - 1);
            int y = random.nextInt(FIELD_SIZE - 1);
            if (field[x][y].equals(EMPTY_FIELD)) {
                field[x][y] = MONSTER_MARK;
                Monster monster = new Monster(x, y);
                monsters.add(monster);
                return monster;
            }
        }
    }

    public synchronized void deleteMonster(Monster monster) {
        monsters.remove(monster);
        clear(monster.x, monster.y);
    }

    public synchronized String get(int x, int y) {
        return field[x][y];
    }

    public synchronized void set(String who, int x, int y) {
        field[x][y] = who;
    }

    public synchronized void clear(int x, int y) {
        field[x][y] = EMPTY_FIELD;
    }

    /**
     * User
     */
    public synchronized void addUser(int x, int y, User user) {
        field[x][y] = user.username;
        loginUsers.add(user);
    }

    public void deleteUser(User user) {
        field[user.x][user.y] = EMPTY_FIELD;
        loginUsers.remove(user);
    }

    public User getUser(int x, int y) {
        return loginUsers.stream()
                .filter(user -> (user.x == x && user.y == y))
                .findFirst()
                .orElse(null);
    }

    public User getUser(String username) {
        return loginUsers.stream()
                .filter(user -> (user.username.equals(username)))
                .findFirst()
                .orElse(null);
    }

    public List<String> getLoginUsernames() {
        return getLoginUsers()
                .stream()
                .map(it -> it.username)
                .collect(Collectors.toList());
    }

    public synchronized boolean isEmpty(int x, int y) {
        return field[x][y].equals(EMPTY_FIELD);
    }

    // for test
    public void show() {
        for (int x = 0; x < FIELD_SIZE; x++) {
            for (int y = 0; y < FIELD_SIZE; y++) {
                System.out.printf("%s\t", field[x][y]);
            }
            System.out.println();
        }
    }
}
