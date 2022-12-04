import core.TCPServer;
import util.MonsterRespawnScheduler;

public class MudGameServerMain {
    public static void main(String[] args) {
        TCPServer tcpServer = new TCPServer();
        MonsterRespawnScheduler.start();
        tcpServer.start();
    }
}
