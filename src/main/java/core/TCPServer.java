package core;

import util.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class TCPServer {
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 7000;
    private static final int MAX_CONCURRENT_PLAYER_NUM = 30;

    public TCPServer() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(SERVER_IP, PORT));
            executorService = Executors.newFixedThreadPool(MAX_CONCURRENT_PLAYER_NUM);
            Logger.log("최대 동시 접속 유지 가능 수 : " + MAX_CONCURRENT_PLAYER_NUM);
            Logger.log("MUD 게임 서버 ON - 요청 대기 중");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log("TCP Server 생성 실패. 서버를 강제 종료합니다.");
            System.exit(0);
        }
    }

    public void start() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();

                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;
                int curUserNum = threadPoolExecutor.getPoolSize();

                if (curUserNum >= MAX_CONCURRENT_PLAYER_NUM) {
                    Logger.log("MUD 게임 서버는 최대 " + MAX_CONCURRENT_PLAYER_NUM + "명의 유저만 동시 접속 가능합니다. 클라이언트 연결을 종료합니다.");
                    socket.close();
                }

                InetSocketAddress remoteSocketAddress =
                        (InetSocketAddress) socket.getRemoteSocketAddress();
                String remoteHostName = remoteSocketAddress.getAddress().getHostAddress();
                int remoteHostPort = remoteSocketAddress.getPort();
                Logger.log("새로운 클라이언트 연결 감지 " + remoteHostName + ":" + remoteHostPort);

                // 요청 처리 핸들러를 쓰레드 풀에 넘겨줌
                executorService.execute(new RequestHandleTask(socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log("서버 listen 오류");
        } finally {
            executorService.shutdownNow();
            try {
                if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
