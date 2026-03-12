package com.example.snakemaster;

import android.graphics.Point;
import java.util.Random;

public class Food {
    private Point position;
    private Random random;

    public Food() {
        position = new Point();
        random = new Random();
    }

    public Point getPosition() {
        return position;
    }

    public void spawn(int gridWidth, int gridHeight, java.util.List<Point> snakeBody) {
        boolean valid = false;
        while (!valid) {
            position.x = random.nextInt(gridWidth);
            position.y = random.nextInt(gridHeight);
            
            valid = true;
            for (Point p : snakeBody) {
                if (p.equals(position)) {
                    valid = false;
                    break;
                }
            }
        }
    }
}
