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
        String[][] field = Field.getInstance().getField();
        List<Coordinate> coords = new ArrayList<>();
        for (int x = 0 ; x < 30 ; x++) {
            for (int y = 0 ; y <30 ; y++) {
                if (field[x][y].equals("_")) continue;
                coords.add(new Coordinate(field[x][y], x, y));
            }
        }
        return new ResponseDto(coords);
    }
}
