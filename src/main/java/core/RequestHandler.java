package core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Field;
import model.User;
import model.dto.Request;
import model.dto.Response;

import java.net.Socket;
import java.util.Random;

import static model.Field.FIELD_SIZE;
import static model.dto.Response.*;

@Slf4j
@RequiredArgsConstructor
public class RequestHandler implements Runnable {
    private final Socket socket;
    private User currentUser;

    @Override
    public void run() {
        try {
            while (true) {
                Request request = SocketManager.receiveRequest(socket);
                if (request == null || request.data.equals("logout")) {
                    processLogout();
                    break;
                } else if (request.type.equals("login")) processLogin(request);
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
        redis.login(newUser);
        field.addUser(x, y, newUser);
        currentUser = newUser;

        log.info("{}님이 로그인 했습니다!", username);
        SocketManager.setSocket(username, socket);
        SocketManager.sendResponse(socket, LoginResponse(currentUser));
    }

    private synchronized void processLogout() {
        Field field = Field.getInstance();
        field.clearUser(currentUser.x, currentUser.y, currentUser);
        log.info("{}님이 로그아웃 했습니다.", currentUser.username);
        SocketManager.removeSocket(currentUser.username);
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
            int x = currentUser.x;
            int y = currentUser.y;
            boolean isMoved = currentUser.move(nx, ny);
            if (isMoved) {
                Field filed = Field.getInstance();
                filed.clear(x, y);
                filed.set(currentUser.username, nx, ny);
                Redis.getInstance().move(currentUser, nx, ny);
                response = MoveSuccessResponse(currentUser, x, y);
            } else {
                response = ErrorResponse("그 곳으로는 이동할 수 없습니다! (다른 유저가 있거나, 몬스터가 있거나, 필드를 벗어나거나, 움직이고자 하는 칸이 현재 좌표 기준으로 3칸이 초과합니다.)");
            }
        } else response = ErrorResponse("알 수 없는 명령어 입니다.");
        SocketManager.sendResponse(socket, response);
    }

    public void processChat(String chatCmd) {
        String[] chatTokens = chatCmd.split(" ");
        String receiverUsername = chatTokens[1];
        String message = receiverUsername + "님으로 부터 온 채팅 : " + chatTokens[2];
        boolean result = SocketManager.sendDirectResponse(receiverUsername, message);
        if (!result) {
            SocketManager.sendResponse(socket, ErrorResponse(receiverUsername + "님을 찾을 수 없습니다!"));
            return;
        }
        log.info("채팅 전송 '{}' -> '{}' [{}]", currentUser.username, receiverUsername, message);
    }
}
