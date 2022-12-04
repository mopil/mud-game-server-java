package model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.Field;
import model.User;

import java.util.ArrayList;
import java.util.List;

import static model.Field.FIELD_SIZE;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponseDto {
    String message;
    List<Coordinate> coords;

    private static List<Coordinate> makeCoords() {
        Field field = Field.getInstance();
        List<Coordinate> coords = new ArrayList<>();
        for (int x = 0; x < 30; x++) {
            for (int y = 0; y < 30; y++) {
                if (field.get(x, y).equals("_")) continue;
                coords.add(new Coordinate(field.get(x, y), x, y));
            }
        }
        return coords;
    }

    public static ResponseDto LoginResponse(User user) {
        return new ResponseDto(user.username + "님이 로그인 했습니다! 현재 좌표 (" + user.x + "," + user.y + ")", makeCoords());
    }

    public static ResponseDto FieldResponse() {
        return new ResponseDto(null, makeCoords());
    }

    public static ResponseDto UserListResponse() {
        Field field = Field.getInstance();
        StringBuffer buffer = new StringBuffer();
        buffer.append("접속 유저 좌표 : [ ");
        for (int x = 0; x < FIELD_SIZE; x++) {
            for (int y = 0; y < FIELD_SIZE; y++) {
                if (field.get(x, y).equals("_") || field.get(x, y).equals("S")) continue;
                buffer.append(field.get(x, y)).append("(").append(x).append(",").append(y).append(") ");
            }
        }
        buffer.append(" ]");
        return new ResponseDto(buffer.toString(), null);
    }

    public static ResponseDto MonsterListResponse() {
        Field field = Field.getInstance();
        StringBuffer buffer = new StringBuffer();
        buffer.append("슬라임 좌표 : [ ");
        for (int x = 0; x < FIELD_SIZE; x++) {
            for (int y = 0; y < FIELD_SIZE; y++) {
                if (field.get(x, y).equals("S"))
                    buffer.append("(").append(x).append(",").append(y).append(") ");
            }
        }
        buffer.append(" ]");
        return new ResponseDto(buffer.toString(), null);
    }

    public static ResponseDto MoveSuccessResponse(User user, int nx, int ny) {
        String username = user.username;
        int x = user.x;
        int y = user.y;
        return new ResponseDto(username + "이(가) 이동 했습니다. (" + x + "," + y + ") -> (" + nx + "," + ")", null);
    }

    /**
     * Error
     */
    public static ResponseDto ErrorResponse(String errorMessage) {
        return new ResponseDto(errorMessage, null);
    }

}

