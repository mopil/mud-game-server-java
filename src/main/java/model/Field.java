package model;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class Field {
    private static final Field fieldInstance = new Field();
    private final String[][] field = new String[30][30];
    public List<User> loginUsers = new ArrayList<>();
    public List<Monster> monsters = new ArrayList<>();

    public static synchronized Field getInstance() {
        return fieldInstance;
    }
    public synchronized String[][] getField() {
        return this.field;
    }

    private Field() {
        for (int x = 0; x < 30; x++)
            for (int y = 0; y < 30; y++)
                field[x][y] = "_";
    }

    public synchronized void setObj(String mob, int x, int y) {
        field[x][y] = mob;
    }

    public synchronized boolean isEmpty(int x, int y) {
        return field[x][y].equals("_");
    }

    public void show() {
        for (int x = 0; x < 30; x++) {
            for (int y = 0; y < 30; y++) {
                System.out.print(field[x][y]);
            }
            System.out.println();
        }
    }
}
