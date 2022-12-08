package core;

import lombok.extern.slf4j.Slf4j;
import model.dto.Request;

import java.net.Socket;

@Slf4j
public class RequestHandler extends Thread {
    private final Socket clientSocket;
    private final CommandProcessor commandProcessor;

    public RequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.commandProcessor = new CommandProcessor(clientSocket);
    }

    @Override
    public void run() {
        try {
            while (true) {
                Request request = SocketManager.receiveRequest(clientSocket);
                if (request == null || request.data.equals("logout")) {
                    commandProcessor.processLogout();
                    break;
                } else if (request.type.equals("login")) {
                    commandProcessor.processLogin(request);
                } else if (request.type.equals("cmd")) {
                    if (request.data.startsWith("chat")) {
                        commandProcessor.processChat(request.data);
                    } else {
                        commandProcessor.processCommand(request);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("요청 처리중 예외 발생. 요청 처리 스레드를 종료합니다. : {}", e.getMessage());
        }
    }
}
