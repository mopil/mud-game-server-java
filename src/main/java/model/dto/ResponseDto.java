package model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.Field;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponseDto {
    List<Coordinate> coords;

    public static ResponseDto makeResponse() {
        Field field = Field.getInstance();
        List<Coordinate> coords = new ArrayList<>();
        for (int x = 0 ; x < 30 ; x++) {
            for (int y = 0 ; y <30 ; y++) {
                if (field.get(x, y).equals("_")) continue;
                coords.add(new Coordinate(field.get(x, y), x, y));
            }
        }
        return new ResponseDto(coords);
    }

    public static ResponseDto makeUserListResponse() {
        Field field = Field.getInstance();
        List<Coordinate> coords = new ArrayList<>();
        for (int x = 0 ; x < 30 ; x++) {
            for (int y = 0 ; y <30 ; y++) {
                if (field.get(x, y).equals("_") || field.get(x, y).equals("S")) continue;
                coords.add(new Coordinate(field.get(x, y), x, y));
            }
        }
        return new ResponseDto(coords);
    }

    public static ResponseDto makeMonsterListResponse() {
        Field field = Field.getInstance();
        List<Coordinate> coords = new ArrayList<>();
        for (int x = 0 ; x < 30 ; x++) {
            for (int y = 0 ; y <30 ; y++) {
                if (field.get(x, y).equals("S"))
                    coords.add(new Coordinate(field.get(x, y), x, y));
            }
        }
        return new ResponseDto(coords);
    }
}

