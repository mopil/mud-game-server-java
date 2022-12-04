package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public static void log(String msg) {
        String timestamp = "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] ";
        String threadId = "[thread:" + Thread.currentThread().getId() + "] ";
        System.out.println(timestamp + threadId + msg);
    }
}
