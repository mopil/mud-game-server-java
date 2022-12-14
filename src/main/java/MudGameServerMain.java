import core.TCPServer;
import util.MonsterAttackScheduler;
import util.MonsterRespawnScheduler;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MudGameServerMain {
    public static void main(String[] args) {
        showSignature();
        TCPServer tcpServer = TCPServer.getInstance();
        MonsterRespawnScheduler.start();
        MonsterAttackScheduler.start();
        tcpServer.start();
    }

    private static void showSignature() {
        int height = 500;
        int width = 400;
        BufferedImage bufferedImage = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_RGB
        );
        Graphics graphics = bufferedImage.getGraphics();
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.drawString("BSH MUD SERVER", 3, 24);
        for (int y = 0; y < height; y++) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int x = 0; x < width; x++) {
                stringBuilder.append(bufferedImage.getRGB(x, y) == -16777216 ? " " : "*");
            }
            if (stringBuilder.toString().trim().isEmpty()) continue;
            System.out.println(stringBuilder);
        }
    }
}
