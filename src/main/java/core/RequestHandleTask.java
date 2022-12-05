package core;

import database.ChatSocketStore;
import database.Redis;
import lombok.extern.slf4j.Slf4j;
import model.Field;
import model.User;
import model.dto.Request;
import model.dto.Response;
import util.JsonConverter;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static model.Field.FIELD_SIZE;
import static model.dto.Response.*;

@Slf4j
public class RequestHandleTask implements Runnable {
    private final Socket socket;
    private final JsonConverter jsonConverter = JsonConverter.getInstance();
    private User currentUser;

    public RequestHandleTask(Socket socket) {
        this.socket = socket;
    }

    private Request receiveRequest() {
        try {
            byte[] bytes = new byte[1024];
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

    private void sendResponse(Response response) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            String jsonBody = jsonConverter.toJson(response);
            log.info("RESPONSE >>> {}", jsonBody);
            dataOutputStream.writeUTF(jsonBody);
            dataOutputStream.flush();
        } catch (Exception e) {
            log.warn("클라이언트로 데이터 송신 실패 : {}", e.getMessage());
        }
    }


    @Override
    public void run() {
        try {
            while (true) {
                Request request = receiveRequest();
                if (request == null || request.data.equals("logout")) {
                    processLogout();
                    break;
                }
                else if (request.type.equals("login")) processLogin(request);
                else if (request.type.equals("cmd")) {
                    if (request.data.startsWith("chat")) processChat(request.data);
                    else processCommand(request);
                }
            }
        } catch (Exception e) {
            log.warn("요청 처리중 예외 발생. 요청 처리 스레드를 종료합니다. : {}", e.getMessage());
        } finally {
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void processLogin(Request request) {
        Field field = Field.getInstance();
        Redis redis = Redis.getInstance();
        String username = request.data;
        Random random = new Random();
        int x;
        int y;
        while (true) {
            int randX = random.nextInt(FIELD_SIZE - 1);
            int randY = random.nextInt(FIELD_SIZE - 1);
            if (field.isEmpty(randX, randY)) {
                x = randX;
                y = randY;
                break;
            }
        }
        User newUser = new User(username, x, y);
        redis.saveLoginUser(newUser, jsonConverter.toJson(newUser), 300);
        field.addUser(x, y, newUser);
        currentUser = newUser;

        log.info("{}님이 로그인 했습니다!", username);
        ChatSocketStore.setSocket(username, socket);
        sendResponse(LoginResponse(currentUser));
    }

    private synchronized void processLogout() {
        Field field = Field.getInstance();
        field.clearUser(currentUser.x, currentUser.y, currentUser);
        log.info("{}님이 로그아웃 했습니다.", currentUser.username);
        ChatSocketStore.removeSocket(currentUser.username);
    }

    private synchronized void processCommand(Request request) {
        String cmd = request.data;

        Response response = null;

        if (cmd.equals("users")) response = UserListResponse();
        else if (cmd.equals("monsters")) response = MonsterListResponse();
        else if (cmd.equals("map")) response = FieldResponse();
        else if (cmd.equals("attack")) {
            int userStr = currentUser.str;
            int curUserX = currentUser.x;
            int curUserY = currentUser.y;
            // 현재 좌표 기준 9칸 공격
        } else if (cmd.startsWith("move")) {
            String[] moveTokens = cmd.split(" ");
            int nx = Integer.parseInt(moveTokens[1]);
            int ny = Integer.parseInt(moveTokens[2]);
            boolean isMoved = currentUser.move(nx, ny);
            if (isMoved) {
                response = MoveSuccessResponse(currentUser, nx, ny);
            } else {
                response = ErrorResponse("현재 좌표 기준 3칸 이상 움직일 수 없습니다.");
            }
        } else response = ErrorResponse("알 수 없는 명령어 입니다.");
        sendResponse(response);
    }

    public void processChat(String chatCmd) {
        String[] chatTokens = chatCmd.split(" ");
        String receiverUsername = chatTokens[1];
        String message = chatTokens[2];
        try {
            Socket socket = ChatSocketStore.getSocket(receiverUsername);

            if (socket == null) {
                sendResponse(ErrorResponse(receiverUsername + "님을 찾을 수 없습니다!"));
                return;
            }

            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            log.info("채팅 전송 '{}' -> '{}' [{}]", currentUser.username, receiverUsername, message);
            message = currentUser.username + "님으로 부터 온 채팅 : " + message;
            String json = jsonConverter.toJson(new Response(message, null));
            dataOutputStream.writeUTF(json);
            dataOutputStream.flush();
        } catch (Exception e) {
            log.warn("processChat 예외 발생 : {}", e.getMessage());
        }
    }
}
