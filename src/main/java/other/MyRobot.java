package other;

import java.awt.*;

public class MyRobot {

    public static void main(String[] args) {
        try {
            double min2Sec = 60;
            double numberOfMin = args.length > 0 ? Double.valueOf(args[0]) : 1;
            double threshold = min2Sec * numberOfMin;
            Robot robot = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            double centerX = screenSize.getWidth() / 2;
            double centerY = screenSize.getHeight() / 2;
            double x = centerX + centerX * (Math.random() - 0.5);
            double vx = centerX * (2 * Math.random() - 1);
            double y = centerY + centerY * (Math.random() - 0.5);
            double vy = centerY + (2 * Math.random() - 1);
            double time = System.currentTimeMillis();
            double totalTime = 0;
            while (totalTime < threshold) {
                robot.mouseMove((int) Math.floor(x), (int) Math.floor(y));
                double dt = (System.currentTimeMillis() - time) * 1E-3;
                final double random = Math.random();
                double ax = -(x - centerX) + (random < 0.5 ? centerX * (2 * Math.random() - 1) : 0);
                double ay = -(y - centerY) + (random < 0.5 ? centerY * (2 * Math.random() - 1) : 0);
                vx = vx + dt * ax;
                vy = vy + dt * ay;
                x = x + vx * dt;
                y = y + vy * dt;
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
