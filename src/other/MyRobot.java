package other;

import java.awt.*;

public class MyRobot {

    public static void main(String[] args) {
        try {
            double min2Sec = 60;
            double numberOfMin = 1;
            double threshold = min2Sec * numberOfMin;
            Robot robot = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            double centerX = screenSize.getWidth() / 2;
            double centerY = screenSize.getHeight() / 2;
            double x = centerX + centerX / 2;
            double vx = Math.random() * centerX;
            double y = centerY;
            double vy = Math.random() * centerY;
            double time = System.currentTimeMillis();
            double totalTime = 0;
            while (totalTime < threshold) {
                double dt = (System.currentTimeMillis() - time) * 1E-3;
                double ax = -(x - centerX) + centerX * (2 * Math.random() - 1);
                double ay = -(y - centerY) + centerY * (2 * Math.random() - 1);
                vx = vx + dt * ax;
                vy = vy + dt * ay;
                x = x + vx * dt;
                y = y + vy * dt;
                robot.mouseMove((int) Math.floor(x), (int) Math.floor(y));
                time = System.currentTimeMillis();
                robot.delay(1);
                totalTime += dt;
                System.out.println(totalTime / min2Sec);
            }
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}
