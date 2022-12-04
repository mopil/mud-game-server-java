import core.TCPServer;

public class MudGameServerMain {
    public static void main(String[] args) {
        TCPServer tcpServer = new TCPServer();
        tcpServer.start();
    }
}
