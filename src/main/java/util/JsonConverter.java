package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonConverter {
    private static final JsonConverter JSON_CONVERTOR = new JsonConverter();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonConverter getInstance() {
        return JSON_CONVERTOR;
    }

    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("JSON 직렬화 오류 : {}", e.getMessage());
            return null;
        }
    }

    public <T> T toObject(String json, Class<T> target) {
        try {
            return objectMapper.readValue(json, target);
        } catch (Exception e) {
            log.warn("JSON 역직렬화 오류 : {}", e.getMessage());
            return null;
        }
    }

}
