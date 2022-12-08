package core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.dto.Request;
import model.dto.Response;
import util.JsonConverter;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static core.GlobalConfig.MAX_TEMP_BYTES;
import static model.dto.Response.ErrorResponse;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SocketManager {
    private static final Map<String, Socket> socketList = new ConcurrentHashMap<>();
    private static final JsonConverter jsonConverter = JsonConverter.getInstance();

    public static void setSocket(String username, Socket socket) {
        socketList.put(username, socket);
    }

    public static Socket getSocket(String username) {
        return socketList.get(username);
    }


    public static Request receiveRequest(Socket socket) {
        try {
            byte[] bytes = new byte[MAX_TEMP_BYTES];
            InputStream is = socket.getInputStream();
            int readSize = is.read(bytes);
            String json = new String(bytes, StandardCharsets.UTF_8)
                    .substring(0, readSize);
            log.info("REQUEST <<< {}", json);
            return jsonConverter.toObject(json, Request.class);
        } catch (Exception e) {
            log.warn("클라이언트로 부터 데이터 수신 실패, receive data == null : {}", e.getMessage());
            return null;
        }
    }

    public static boolean sendDirectResponse(String receiverUsername, String message) {
        try {
            Socket socket = SocketManager.getSocket(receiverUsername);
            if (socket == null) return false;
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            String json = jsonConverter.toJson(new Response(message, null));
            dataOutputStream.writeUTF(json);
            dataOutputStream.flush();
        } catch (Exception e) {
            log.warn("sendDirectResponse 실패 : {}", e.getMessage());
        }
        return true;
    }

    public static void sendResponse(Socket receiverSocket, Response response) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(receiverSocket.getOutputStream());
            String jsonBody = jsonConverter.toJson(response);
            log.info("RESPONSE >>> {}", jsonBody);
            dataOutputStream.writeUTF(jsonBody);
            dataOutputStream.flush();
        } catch (Exception e) {
            log.warn("클라이언트로 데이터 송신 실패 : {}", e.getMessage());
        }
    }

    public static void sendError(Socket receiverSocket) {
        sendResponse(receiverSocket, ErrorResponse("알 수 없는 명령어 입니다."));
    }

    public static void disconnect(String username) {
        socketList.remove(username);
    }
}
