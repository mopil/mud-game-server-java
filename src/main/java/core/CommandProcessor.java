package core;

import lombok.extern.slf4j.Slf4j;
import model.Field;
import model.User;
import model.dto.Request;
import model.dto.Response;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;
import java.util.Random;

import static core.GlobalConfig.FIELD_SIZE;
import static core.GlobalConfig.LOGIN_DURATION_SECS;
import static model.dto.Response.*;

@Slf4j
public class CommandProcessor {
    private User currentUser;
    private final Socket clientSocket;
    private final Field field = Field.getInstance();
    private final Redis redis = Redis.getInstance();

    public CommandProcessor(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public synchronized void processLogin(Request request) {
        String username = request.data;
        User existingUser = redis.getUser(username);
        if (existingUser != null) {
            currentUser = existingUser;
            field.addUser(existingUser.x, existingUser.y, existingUser);
            SocketManager.setSocket(username, clientSocket);
            SocketManager.sendResponse(clientSocket, LoginResponse(existingUser, false, false));
            return;
        }
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
        redis.saveUser(newUser);
        field.addUser(x, y, newUser);
        currentUser = newUser;

        log.info("{}님이 로그인 했습니다!", username);
        log.info("현재 접속 유저 : {}", field.getLoginUsernames());
        SocketManager.setSocket(username, clientSocket);
        SocketManager.sendResponse(clientSocket, LoginResponse(currentUser, true, false));
    }

    public synchronized void processLogout() {
        field.deleteUser(currentUser);
        redis.deleteUser(currentUser, LOGIN_DURATION_SECS);
        SocketManager.disconnect(currentUser.username);
        log.info("{}님이 로그아웃 했습니다.", currentUser.username);
        log.info("현재 접속 유저 : {}", field.getLoginUsernames());
    }
    public synchronized void processCommand(Request request) {
        String cmd = request.data;
        Response response = null;
        if (cmd.equals("users")) {
            response = UserListResponse();
        } else if (cmd.equals("monsters")) {
            response = MonsterListResponse();
        } else if (cmd.equals("map")) {
            response = FieldResponse();
        } else if (cmd.equals("info")) {
            response = UserInfoResponse(currentUser);
        } else if (cmd.startsWith("move")) {
            response = processMove(cmd);
        } else if (cmd.startsWith("item")) {
            processItem(cmd);
            return;
        } else if (cmd.equals("attack")) {
            currentUser.attack();
            return;
        } else {
            SocketManager.sendError(clientSocket);
            return;
        }
        SocketManager.sendResponse(clientSocket, response);
    }

    @NotNull
    private Response processMove(String cmd) {
        Response response;
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
            Redis.getInstance().updateUserPosition(currentUser, nx, ny);
            response = MoveSuccessResponse(currentUser, x, y);
        } else {
            response = ErrorResponse("그 곳으로는 이동할 수 없습니다! (다른 유저가 있거나, 몬스터가 있거나, 필드를 벗어나거나, 움직이고자 하는 칸이 현재 좌표 기준으로 3칸이 초과합니다.)");
        }
        return response;
    }

    public void processChat(String chatCmd) {
        String[] chatTokens = chatCmd.split(" ");
        String receiverUsername;
        String chat;
        try {
            receiverUsername = chatTokens[1];
            chat = chatTokens[2];
        } catch (Exception e) {
            SocketManager.sendError(clientSocket);
            return;
        }
        String message = currentUser.username + "님으로 부터 온 채팅 : " + chat;
        boolean result = SocketManager.sendDirectResponse(receiverUsername, message);
        if (!result) {
            SocketManager.sendResponse(clientSocket, ErrorResponse(receiverUsername + "님을 찾을 수 없습니다!"));
            return;
        }
        log.info("채팅 전송 '{}' -> '{}' [{}]", currentUser.username, receiverUsername, message);
        SocketManager.sendDirectResponse(currentUser.username, receiverUsername + "님에게 채팅 메시지, '" + chat + "'을(를) 보냈습니다!");
    }

    public void processItem(String itemCmd) {
        String item;
        try {
            item = itemCmd.split(" ")[1];
        } catch (Exception e) {
            SocketManager.sendError(clientSocket);
            return;
        }
        switch (item) {
            case "hp":
                if (currentUser.hpPotionCount == 0)
                    SocketManager.sendDirectResponse(currentUser.username, "hp 포션이 없습니다!");
                else {
                    currentUser.hpPotionCount -= 1;
                    currentUser.hp += 10;
                    redis.updateUserPotionCount(currentUser, "hp", currentUser.hpPotionCount);
                    redis.updateUserHp(currentUser, "up", 10);
                    SocketManager.sendDirectResponse(currentUser.username, "hp 포션 1개를 사용했습니다. 체력을 10만큼 회복합니다. (현재 hp:" + currentUser.hp + ")");
                }
                break;
            case "str":
                if (currentUser.strPotionCount == 0)
                    SocketManager.sendDirectResponse(currentUser.username, "str 포션이 없습니다!");
                else {
                    currentUser.strPotionCount -= 1;
                    currentUser.str += 3;
                    redis.updateUserPotionCount(currentUser, "str", currentUser.strPotionCount);
                    redis.updateUserStr(currentUser, "up");
                    SocketManager.sendDirectResponse(currentUser.username, "str 포션 1개를 사용했습니다. 공격력이 3만큼 증가합니다. (현재 str:" + currentUser.str + ")");
                }
                break;
            default:
                SocketManager.sendResponse(clientSocket, ErrorResponse("알 수 없는 명령어 입니다."));
                break;
        }
    }
}
