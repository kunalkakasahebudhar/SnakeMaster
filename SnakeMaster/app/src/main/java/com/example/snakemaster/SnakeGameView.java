package com.example.snakemaster;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.List;

public class SnakeGameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private Thread gameThread;
    private SurfaceHolder surfaceHolder;
    private volatile boolean isRunning;
    private Canvas canvas;
    private Paint paint;

    private int gridWidth, gridHeight;
    private int cellSize;
    private Snake snake;
    private Food food;
    private int score;
    private int speed;
    private static final int INITIAL_SPEED = 200; // in milliseconds
    private static final int MAX_SPEED = 80;

    private GameListener listener;
    private GestureDetector gestureDetector;

    public interface GameListener {
        void onScoreUpdate(int score);
        void onGameOver(int finalScore);
    }

    public SnakeGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        paint = new Paint();
        gestureDetector = new GestureDetector(context, new SwipeGestureListener());
    }

    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        // Dimensions will be set in surfaceChanged or after layout
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        cellSize = width / 20; // 20 units wide
        gridWidth = width / cellSize;
        gridHeight = height / cellSize;

        if (snake == null) {
            initGame();
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        stop();
    }

    private void initGame() {
        snake = new Snake(gridWidth / 2, gridHeight / 2);
        food = new Food();
        food.spawn(gridWidth, gridHeight, snake.getBody());
        score = 0;
        speed = INITIAL_SPEED;
        if (listener != null) listener.onScoreUpdate(score);
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (gameThread != null) {
                gameThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void restart() {
        initGame();
        start();
    }

    @Override
    public void run() {
        while (isRunning) {
            update();
            draw();
            control();
        }
    }

    private void update() {
        // Pass grid dimensions for wrap-around
        snake.move(gridWidth, gridHeight);

        // Check food collision
        Point head = snake.getBody().get(0);
        if (head.equals(food.getPosition())) {
            snake.grow();
            score += 10;
            food.spawn(gridWidth, gridHeight, snake.getBody());
            
            // Speed up
            if (speed > MAX_SPEED) {
                speed -= 2;
            }

            if (listener != null) {
                post(() -> listener.onScoreUpdate(score));
            }
        }

        // Check game over (self-collision only now)
        if (snake.checkCollision()) {
            isRunning = false;
            if (listener != null) {
                post(() -> listener.onGameOver(score));
            }
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            
            // Clear screen
            canvas.drawColor(Color.parseColor("#050813")); // Deep Navy Background

            // Draw grid (subtle)
            paint.reset();
            paint.setColor(Color.parseColor("#0F1A3A"));
            paint.setStrokeWidth(1);
            for (int i = 0; i <= gridWidth; i++) {
                canvas.drawLine(i * cellSize, 0, i * cellSize, getHeight(), paint);
            }
            for (int i = 0; i <= gridHeight; i++) {
                canvas.drawLine(0, i * cellSize, getWidth(), i * cellSize, paint);
            }

            // Draw Snake
            List<Point> body = snake.getBody();
            for (int i = 0; i < body.size(); i++) {
                Point p = body.get(i);
                
                paint.reset();
                paint.setAntiAlias(true);
                
                if (i == 0) {
                    // Snake Head - Glowing Neon Green
                    paint.setColor(Color.parseColor("#39FF14"));
                    paint.setShadowLayer(15, 0, 0, Color.parseColor("#00FF41"));
                    
                    canvas.drawRoundRect(
                        p.x * cellSize + 1, 
                        p.y * cellSize + 1, 
                        (p.x + 1) * cellSize - 1, 
                        (p.y + 1) * cellSize - 1, 
                        12, 12, paint
                    );
                    
                    // Draw Eyes on head
                    paint.reset();
                    paint.setColor(Color.BLACK);
                    float eyeOffset = cellSize / 4.0f;
                    float eyeSize = cellSize / 8.0f;
                    
                    // Draw two small black dots for eyes
                    canvas.drawCircle(p.x * cellSize + eyeOffset, p.y * cellSize + eyeOffset, eyeSize, paint);
                    canvas.drawCircle((p.x + 1) * cellSize - eyeOffset, p.y * cellSize + eyeOffset, eyeSize, paint);
                    
                } else {
                    // Snake Body - Neon Green
                    float alphaFactor = 1.0f - ((float) i / body.size() * 0.5f);
                    int alpha = (int) (255 * alphaFactor);
                    paint.setARGB(alpha, 0, 255, 65);
                    
                    canvas.drawRoundRect(
                        p.x * cellSize + 3, 
                        p.y * cellSize + 3, 
                        (p.x + 1) * cellSize - 3, 
                        (p.y + 1) * cellSize - 3, 
                        10, 10, paint
                    );
                }
            }

            // Draw Food - Glowing Neon Pink
            paint.reset();
            paint.setAntiAlias(true);
            paint.setColor(Color.parseColor("#FF0055"));
            paint.setShadowLayer(20, 0, 0, Color.parseColor("#FF0055"));
            
            Point foodPos = food.getPosition();
            canvas.drawOval(
                foodPos.x * cellSize + 5, 
                foodPos.y * cellSize + 5, 
                (foodPos.x + 1) * cellSize - 5, 
                (foodPos.y + 1) * cellSize - 5, 
                paint
            );

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            Thread.sleep(speed);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 50; // Increased sensitivity
        private static final int SWIPE_VELOCITY_THRESHOLD = 50; 

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        snake.setDirection(Snake.Direction.RIGHT);
                    } else {
                        snake.setDirection(Snake.Direction.LEFT);
                    }
                    return true;
                }
            } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    snake.setDirection(Snake.Direction.DOWN);
                } else {
                    snake.setDirection(Snake.Direction.UP);
                }
                return true;
            }
            return false;
        }
    }
}
