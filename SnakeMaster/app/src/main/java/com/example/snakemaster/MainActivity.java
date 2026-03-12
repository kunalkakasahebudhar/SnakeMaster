package com.example.snakemaster;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity implements SnakeGameView.GameListener {

    private SnakeGameView snakeGameView;
    private TextView tvScore, tvHighScore;
    private View overlay;
    private MaterialButton btnStart;
    private int score;
    private int highScore;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = getSharedPreferences("SnakeMaster", MODE_PRIVATE);
        highScore = sp.getInt("highScore", 0);

        tvScore = findViewById(R.id.tvScore);
        tvHighScore = findViewById(R.id.tvHighScore);
        snakeGameView = findViewById(R.id.snakeGameView);
        overlay = findViewById(R.id.overlay);
        btnStart = findViewById(R.id.btnStart);

        tvHighScore.setText(String.valueOf(highScore));
        snakeGameView.setListener(this);

        btnStart.setOnClickListener(v -> {
            overlay.setVisibility(View.GONE);
            snakeGameView.restart();
        });
    }

    @Override
    public void onScoreUpdate(final int score) {
        this.score = score;
        tvScore.setText(String.valueOf(score));
        if (score > highScore) {
            highScore = score;
            tvHighScore.setText(String.valueOf(highScore));
            sp.edit().putInt("highScore", highScore).apply();
        }
    }

    @Override
    public void onGameOver(final int finalScore) {
        runOnUiThread(() -> {
            new AlertDialog.Builder(MainActivity.this)
                .setTitle("GAME OVER")
                .setMessage("Your Score: " + finalScore + "\nHigh Score: " + highScore)
                .setCancelable(false)
                .setPositiveButton("RESTART", (dialog, which) -> {
                    snakeGameView.restart();
                })
                .setNegativeButton("MENU", (dialog, which) -> {
                    overlay.setVisibility(View.VISIBLE);
                })
                .show();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeGameView.stop();
    }
}