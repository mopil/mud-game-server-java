package core;

import database.Redis;
import model.Field;
import model.User;
import model.dto.RequestDto;
import model.dto.ResponseDto;
import util.JsonConverter;
import util.Logger;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static model.Field.FIELD_SIZE;
import static model.dto.ResponseDto.*;

public class RequestHandleTask implements Runnable {
    private final Socket socket;
    private final JsonConverter jsonConverter = JsonConverter.getInstance();
    private User currentUser;

    public RequestHandleTask(Socket socket) {
        this.socket = socket;
    }

    private RequestDto receive() {
        try {
            byte[] bytes = new byte[1024];
            InputStream is = socket.getInputStream();
            int readSize = is.read(bytes);
            String json = new String(bytes, StandardCharsets.UTF_8)
                    .substring(0, readSize);
            Logger.log("REQUEST <<< " + json);
            return jsonConverter.toObject(json, RequestDto.class);
        } catch (Exception e) {
            Logger.log("클라이언트로 부터 데이터 수신 실패, receive data == null");
            return null;
        }
    }

    private void send(ResponseDto responseDto) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            String jsonBody = jsonConverter.toJson(responseDto);
            Logger.log("RESPONSE >>> " + jsonBody);
            dataOutputStream.writeUTF(jsonBody);
            dataOutputStream.flush();
        } catch (Exception e) {
            Logger.log("클라이언트로 데이터 송신 실패");
        }
    }


    @Override
    public void run() {
        try {
            while (true) {
                RequestDto request = receive();
                if (request == null || request.data.equals("logout")) {
                    processLogout(); break;
                } else if (request.type.equals("login")) {
                    ResponseDto response = processLogin(request);
                    send(response);
                } else if (request.type.equals("cmd")) {
                    ResponseDto response = processCommand(request);
                    send(response);
                } else {

                }
            }
        } catch (Exception e) {
            Logger.log("요청 처리중 예외 발생. 요청 스레드를 종료합니다.");
        } finally {
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized ResponseDto processLogin(RequestDto request) {
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
        redis.save(username, jsonConverter.toJson(newUser), 300);
        field.addUser(x, y, newUser);
        currentUser = newUser;
        Logger.log(username + "님이 로그인 했습니다!");
        return LoginResponse(currentUser);
    }

    private synchronized void processLogout() {
        Field field = Field.getInstance();
        field.clearUser(currentUser.x, currentUser.y, currentUser);
        Logger.log(currentUser.username + " 로그아웃");
    }

    private synchronized ResponseDto processCommand(RequestDto request) {
        String cmd = request.data;
        if (cmd.equals("users")) return UserListResponse();
        else if (cmd.equals("monsters")) return MonsterListResponse();
        else if (cmd.equals("map")) return FieldResponse();
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
                return MoveSuccessResponse(currentUser, nx, ny);
            } else {
                return ErrorResponse("현재 좌표 기준 3칸 이상 움직일 수 없습니다.");
            }
        }
        return ErrorResponse("알 수 없는 명령어 입니다.");
    }
}
