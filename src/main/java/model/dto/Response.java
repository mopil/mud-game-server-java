package model.dto;

import core.Redis;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.Field;
import model.User;

import java.util.ArrayList;
import java.util.List;

import static core.GlobalConfig.*;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Response {
    String message;
    List<Coordinate> coords;

    private static List<Coordinate> makeCoords() {
        Field field = Field.getInstance();
        List<Coordinate> coords = new ArrayList<>();
        for (int x = 0; x < FIELD_SIZE; x++) {
            for (int y = 0; y < FIELD_SIZE; y++) {
                if (field.get(x, y).equals(EMPTY_FIELD)) continue;
                coords.add(new Coordinate(field.get(x, y), x, y));
            }
        }
        return coords;
    }

    public static Response LoginResponse(User user, boolean isNew, boolean dupLogin) {
        if (isNew)
            return new Response(user.username + "님이 로그인 했습니다! 현재 좌표 (" + user.x + "," + user.y + ")", makeCoords());
        else
            if (dupLogin) return new Response(user.username + "님은 현재 접속중입니다!", makeCoords());
            else return new Response(user.username + "님이 재로그인 했습니다! 돌아오셨군요! 이전 로그아웃 당시 좌표 및 현재 좌표 (" + user.x + "," + user.y + ")", makeCoords());
    }

    public static Response FieldResponse() {
        return new Response(null, makeCoords());
    }

    public static Response UserListResponse() {
        Field field = Field.getInstance();
        StringBuffer buffer = new StringBuffer();
        buffer.append("접속 유저 좌표 : [ ");
        for (int x = 0; x < FIELD_SIZE; x++) {
            for (int y = 0; y < FIELD_SIZE; y++) {
                if (field.get(x, y).equals(EMPTY_FIELD) || field.get(x, y).equals(MONSTER_MARK)) continue;
                buffer.append(field.get(x, y)).append("(").append(x).append(",").append(y).append(") ");
            }
        }
        buffer.append(" ]");
        return new Response(buffer.toString(), null);
    }

    public static Response UserInfoResponse(User user) {
        String message = Redis.getInstance().getUserInfo(user);
        return new Response(message, null);
    }

    public static Response MonsterListResponse() {
        Field field = Field.getInstance();
        StringBuffer buffer = new StringBuffer();
        buffer.append("슬라임 좌표 : [ ");
        for (int x = 0; x < FIELD_SIZE; x++) {
            for (int y = 0; y < FIELD_SIZE; y++) {
                if (field.get(x, y).equals(MONSTER_MARK))
                    buffer.append("(").append(x).append(",").append(y).append(") ");
            }
        }
        buffer.append(" ]");
        return new Response(buffer.toString(), null);
    }

    public static Response MoveSuccessResponse(User user, int x, int y) {
        String username = user.username;
        int nx = user.x;
        int ny = user.y;
        return new Response(username + "이(가) 이동 했습니다. (" + x + "," + y + ") -> (" + nx + "," + ny + ")", null);
    }

    /**
     * Error
     */
    public static Response ErrorResponse(String errorMessage) {
        return new Response(errorMessage, null);
    }

}

