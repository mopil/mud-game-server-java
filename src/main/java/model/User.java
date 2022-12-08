package model;


import core.Redis;
import core.SocketManager;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Optional;

import static core.GlobalConfig.*;

@Builder
@AllArgsConstructor
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
        this.hp = USER_DEFAULT_HP;
        this.str = USER_DEFAULT_STR;
        this.hpPotionCount = USER_DEFAULT_POTION_COUNT;
        this.strPotionCount = USER_DEFAULT_POTION_COUNT;
    }

    public boolean move(int nx, int ny) {
        Field field = Field.getInstance();
        if (Math.abs(nx - x) > USER_MAX_MOVE_AMOUNT || Math.abs(ny - y) > USER_MAX_MOVE_AMOUNT) {
            return false;
        } else if (field.get(nx, ny).equals("_")) {
            field.clear(x, y);
            x = nx;
            y = ny;
            field.set(username, nx, ny);
            Redis.getInstance().updateUserPosition(this, nx, ny);
            return true;
        } else {
            return false;
        }
    }

    public void attack() {
        SocketManager.sendDirectResponse(username, "주위 슬라임들을 공격했습니다!");
        boolean isAttacked = false;
        Field field = Field.getInstance();
        Redis redis = Redis.getInstance();
        for (int i = 0; i < ATTACK_RANGE; i++) {
            int nx = dx[i] + x;
            int ny = dy[i] + y;
            if (nx < 0 || nx >= FIELD_SIZE || ny < 0 || ny >= FIELD_SIZE) continue;
            if (field.get(nx, ny).equals(MONSTER_MARK)) {
                Optional<Monster> optionalMonster = field.getMonsters()
                        .stream()
                        .filter(it -> it.x == nx && it.y == ny)
                        .findFirst();
                if (optionalMonster.isPresent()) {
                    isAttacked = true;
                    Monster monster = optionalMonster.get();
                    monster.hp -= str;
                    String message = "\n\"" + username + "\"이/가 \"슬라임\"을 공격해서 데미지 " + str + "을/를 가했습니다!";
                    String message2 = " (슬라임의 남은 hp:" + monster.hp + ")";
                    SocketManager.sendDirectResponse(username, message + message2);
                    if (monster.hp <= 0) {
                        String winMessage = "슬라임을 처치했습니다!";
                        field.deleteMonster(monster);
                        if (monster.potion == 0) {
                            winMessage += " HP 포션 1개를 전리품으로 획득하였습니다!";
                            redis.updateUserPotionCount(this, "hp", "up");
                        } else {
                            winMessage += " STR 포션 1개를 전리품으로 획득하였습니다!";
                            redis.updateUserPotionCount(this, "str", "up");
                        }
                        SocketManager.sendDirectResponse(username, winMessage);
                    }
                }
            }
        }
        if (!isAttacked) SocketManager.sendDirectResponse(username, "주위에 슬라임이 없습니다... 헛 공격!");
    }


}
