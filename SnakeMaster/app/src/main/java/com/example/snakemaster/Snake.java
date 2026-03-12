package com.example.snakemaster;

import android.graphics.Point;
import java.util.ArrayList;
import java.util.List;

public class Snake {
    private List<Point> body;
    private Direction direction;
    private boolean grow;

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public Snake(int startX, int startY) {
        body = new ArrayList<>();
        body.add(new Point(startX, startY));
        body.add(new Point(startX - 1, startY));
        body.add(new Point(startX - 2, startY));
        direction = Direction.RIGHT;
        grow = false;
    }

    public List<Point> getBody() {
        return body;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        // Prevent 180-degree turns
        if ((this.direction == Direction.UP && direction != Direction.DOWN) ||
            (this.direction == Direction.DOWN && direction != Direction.UP) ||
            (this.direction == Direction.LEFT && direction != Direction.RIGHT) ||
            (this.direction == Direction.RIGHT && direction != Direction.LEFT)) {
            this.direction = direction;
        }
    }

    public void move() {
        Point head = body.get(0);
        Point newHead = new Point(head.x, head.y);

        switch (direction) {
            case UP: newHead.y--; break;
            case DOWN: newHead.y++; break;
            case LEFT: newHead.x--; break;
            case RIGHT: newHead.x++; break;
        }

        body.add(0, newHead);
        if (!grow) {
            body.remove(body.size() - 1);
        } else {
            grow = false;
        }
    }

    public void grow() {
        grow = true;
    }

    public boolean checkCollision(int gridWidth, int gridHeight) {
        Point head = body.get(0);

        // Wall collision
        if (head.x < 0 || head.x >= gridWidth || head.y < 0 || head.y >= gridHeight) {
            return true;
        }

        // Self collision
        for (int i = 1; i < body.size(); i++) {
            if (head.equals(body.get(i))) {
                return true;
            }
        }

        return false;
    }

    public void reset(int startX, int startY) {
        body.clear();
        body.add(new Point(startX, startY));
        body.add(new Point(startX - 1, startY));
        body.add(new Point(startX - 2, startY));
        direction = Direction.RIGHT;
        grow = false;
    }
}
