package database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatSocketStore {
    private static final Map<String, Socket> socketList = new ConcurrentHashMap<>();

    public static void setSocket(String username, Socket socket) {
        socketList.put(username, socket);
    }

    public static Socket getSocket(String username) {
        return socketList.get(username);
    }

    public static void removeSocket(String username) {
        socketList.remove(username);
    }
}
