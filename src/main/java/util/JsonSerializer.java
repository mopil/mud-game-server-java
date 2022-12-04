package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonSerializer {
    private static final JsonSerializer jsonSerializer = new JsonSerializer();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonSerializer getInstance() {
        return jsonSerializer;
    }

    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            Logger.log("JSON 직렬화 오류.");
            return null;
        }
    }

    public <T> T toObject(String json, Class<T> target) {
        try {
            return objectMapper.readValue(json, target);
        } catch (Exception e) {
            Logger.log("JSON 역직렬화 오류.");
            return null;
        }
    }

}
