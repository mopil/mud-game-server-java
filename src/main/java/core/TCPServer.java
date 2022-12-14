package core;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static core.GlobalConfig.*;

@Slf4j
public class TCPServer {
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    private static class SingletonHelper {
        private static final TCPServer INSTANCE = new TCPServer();
    }

    public static TCPServer getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private TCPServer() {
        try {
            Redis.getInstance().deleteAllKeys();
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(SERVER_IP, SERVER_PORT));
            executorService = Executors.newFixedThreadPool(MAX_CONCURRENT_PLAYER_NUM);
        } catch (Exception e) {
            log.error("TCPServer 생성중 예외 발생, 서버를 강제 종료합니다. : {}", e.getMessage());
            System.exit(0);
        }
    }

    public void start() {
        log.info("최대 동시 접속 유지 가능 수 : {}", MAX_CONCURRENT_PLAYER_NUM);
        log.info("MUD 게임 서버 ON - 요청 대기 중");
        log.info("All copyrights reserved by 배성흥");
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();

                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;
                int curUserNum = threadPoolExecutor.getPoolSize();

                if (curUserNum >= MAX_CONCURRENT_PLAYER_NUM) {
                    log.info("MUD 게임 서버는 최대 {}명의 유저만 동시 접속 가능합니다. 클라이언트 연결을 종료합니다.", MAX_CONCURRENT_PLAYER_NUM);
                    clientSocket.close();
                }

                InetSocketAddress remoteSocketAddress =
                        (InetSocketAddress) clientSocket.getRemoteSocketAddress();
                String remoteHostName = remoteSocketAddress.getAddress().getHostAddress();
                int remoteHostPort = remoteSocketAddress.getPort();
                log.info("새로운 클라이언트 연결 감지 {}:{}", remoteHostName, remoteHostPort);

                // 요청 처리 핸들러를 쓰레드 풀에 넘겨줌
                RequestHandler thread = new RequestHandler(clientSocket);
                executorService.execute(thread);
            }
        } catch (Exception e) {
            log.warn("요청 처리 스레드 생성중 예외 발생 : {}", e.getMessage());
        } finally {
            executorService.shutdownNow();
            try {
                if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            } catch (IOException e) {
                log.warn("서버 소켓 정리중 예외 발생 : {}", e.getMessage());
            }
        }
    }
}
