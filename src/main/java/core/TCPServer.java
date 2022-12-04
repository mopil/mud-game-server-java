package core;

import util.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class TCPServer {
    private ServerSocket serverSocket;
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 7000;

    public TCPServer() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(SERVER_IP, PORT));

            Logger.log("MUD 게임 서버 ON - 요청 대기 중");

            while (true) {
                Socket socket = serverSocket.accept();

                InetSocketAddress remoteSocketAddress =
                        (InetSocketAddress) socket.getRemoteSocketAddress();
                String remoteHostName = remoteSocketAddress.getAddress().getHostAddress();
                int remoteHostPort = remoteSocketAddress.getPort();
                Logger.log("새로운 소켓 연결 성공! " + remoteHostName + ":" + remoteHostPort);

                // 핸들러 실행
                new ClientRequestHandlerThread(socket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log("TCP Server 생성 실패. 서버를 강제 종료합니다.");
            System.exit(0);
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
