package core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.Redis;
import model.Field;
import model.User;
import model.dto.RequestDto;
import model.dto.ResponseDto;
import util.Logger;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class ClientRequestHandlerThread extends Thread {
    private final Socket socket;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private User currentUser;

    public ClientRequestHandlerThread(Socket socket) {
        this.socket = socket;
    }

    private RequestDto receive() {
        try {
            byte[] bytes = new byte[1024];
            InputStream is = socket.getInputStream();
            int readSize = is.read(bytes);
            String json = new String(bytes, StandardCharsets.UTF_8)
                    .substring(0, readSize);
            // 역직렬화 까지 수행
            return objectMapper.readValue(json, RequestDto.class);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log("클라이언트로 부터 데이터 수신 실패, receive data == null");
            return null;
        }
    }

    private void send(ResponseDto responseDto) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            String jsonBody = objectMapper.writeValueAsString(responseDto);
            dataOutputStream.writeUTF(jsonBody);
            dataOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log("클라이언트로 데이터 송신 실패");
        }
    }


    @Override
    public void run() {
        try {
            while (true) {
                // TODO : 1024 바이트가 넘어가면 처리할 로직
                RequestDto request = receive();
                // 클라이언트로 부터 연결이 강제 종료 됨
                if (request == null) {
                    Logger.log("클라이언트와 연결 종료");
                    break;
                }
                if (request.type.equals("login")) {
                    // 로그인 처리
                    processLogin(request);
                } else if (request.type.equals("move")) {
                    // 커맨드 처리
                } else {

                }

                Logger.log(request.data); // TODO : 일단 영어만 됨 한글을 짤림
                send(ResponseDto.makeResponse());
            }
        } finally {
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void processLogin(RequestDto request) {
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
        try {
            redis.save(username, objectMapper.writeValueAsString(newUser), 5000);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        field.loginUsers.add(newUser);
        currentUser = newUser;
        Logger.log(username + "님이 로그인 했습니다!");
        field.setObj(username, x, y);
    }
}
