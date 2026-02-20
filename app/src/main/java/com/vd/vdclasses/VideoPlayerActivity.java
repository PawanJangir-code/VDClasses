package com.vd.vdclasses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class VideoPlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private TextView tvPlayerTitle;
    private ProgressBar progressLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        playerView = findViewById(R.id.playerView);
        tvPlayerTitle = findViewById(R.id.tvPlayerTitle);
        progressLoader = findViewById(R.id.progressLoader);

        String url = getIntent().getStringExtra("videoUrl");
        String title = getIntent().getStringExtra("title");

        if (title != null) {
            tvPlayerTitle.setText(title);
        }

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    progressLoader.setVisibility(View.GONE);
                } else if (playbackState == Player.STATE_BUFFERING) {
                    progressLoader.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_ENDED) {
                    progressLoader.setVisibility(View.GONE);
                }
            }
        });

        if (url != null && !url.isEmpty()) {
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        }

        setupDoubleTapSeek();
        setupSpeedControls();
    }

    private void setupDoubleTapSeek() {
        View leftOverlay = findViewById(R.id.overlayLeft);
        View rightOverlay = findViewById(R.id.overlayRight);

        GestureDetector leftDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (player != null) {
                    long newPos = Math.max(player.getCurrentPosition() - 10000, 0);
                    player.seekTo(newPos);
                    Toast.makeText(VideoPlayerActivity.this, "- 10s", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                playerView.performClick();
                return true;
            }
        });

        GestureDetector rightDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (player != null) {
                    long duration = player.getDuration();
                    long newPos = player.getCurrentPosition() + 10000;
                    if (duration > 0) {
                        newPos = Math.min(newPos, duration);
                    }
                    player.seekTo(newPos);
                    Toast.makeText(VideoPlayerActivity.this, "+ 10s", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                playerView.performClick();
                return true;
            }
        });

        leftOverlay.setOnTouchListener((v, event) -> {
            leftDetector.onTouchEvent(event);
            return true;
        });

        rightOverlay.setOnTouchListener((v, event) -> {
            rightDetector.onTouchEvent(event);
            return true;
        });
    }

    private void setupSpeedControls() {
        Button btnSpeed05 = findViewById(R.id.btnSpeed05);
        Button btnSpeed1 = findViewById(R.id.btnSpeed1);
        Button btnSpeed15 = findViewById(R.id.btnSpeed15);
        Button btnSpeed2 = findViewById(R.id.btnSpeed2);

        btnSpeed05.setOnClickListener(v -> setSpeed(0.5f));
        btnSpeed1.setOnClickListener(v -> setSpeed(1.0f));
        btnSpeed15.setOnClickListener(v -> setSpeed(1.5f));
        btnSpeed2.setOnClickListener(v -> setSpeed(2.0f));
    }

    private void setSpeed(float speed) {
        if (player != null) {
            player.setPlaybackParameters(new PlaybackParameters(speed));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (player != null && !player.isPlaying()) {
            player.play();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
