import core.TCPServer;
import util.MonsterAttackScheduler;
import util.MonsterRespawnScheduler;

public class MudGameServerMain {
    public static void main(String[] args) {
        TCPServer tcpServer = new TCPServer();
        MonsterRespawnScheduler.start();
        MonsterAttackScheduler.start();
        tcpServer.start();
    }
}
