package core;

import database.Redis;
import model.Field;
import model.User;
import model.dto.RequestDto;
import model.dto.ResponseDto;
import util.JsonSerializer;
import util.Logger;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class RequestHandleTask implements Runnable {
    private final Socket socket;
//    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonSerializer serializer = JsonSerializer.getInstance();
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
            return serializer.toObject(json, RequestDto.class);
        } catch (Exception e) {
            Logger.log("클라이언트로 부터 데이터 수신 실패, receive data == null");
            return null;
        }
    }

    private void send(ResponseDto responseDto) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            String jsonBody = serializer.toJson(responseDto);
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
            int randX = random.nextInt(29);
            int randY = random.nextInt(29);
            if (field.isEmpty(randX, randY)) {
                x = randX;
                y = randY;
                break;
            }
        }
        User newUser = new User(username, x, y);
        redis.save(username, serializer.toJson(newUser), 300);
        field.addUser(x, y, newUser);
        currentUser = newUser;
        Logger.log(username + "님이 로그인 했습니다!");
        return ResponseDto.makeResponse();
    }

    private synchronized void processLogout() {
        Field field = Field.getInstance();
        field.clearUser(currentUser.x, currentUser.y, currentUser);
        Logger.log(currentUser.username + " 로그아웃");
    }

//    private synchronized ResponseDto processCommand(RequestDto request) {
//        Field field = Field.getInstance();
//        if (request.data.equals("users")) {
//            return ResponseDto.makeUserListResponse();
//        } else if (request.data.equals("monsters")) {
//            return ResponseDto.makeMonsterListResponse();
//        } else if (request.data.equals("attack")) {
//            int userStr = currentUser.str;
//            int curUserX = currentUser.x;
//            int curUserY = currentUser.y;
//            // 현재 좌표 기준 9칸 공격
//
//        }
//
//    }
}
