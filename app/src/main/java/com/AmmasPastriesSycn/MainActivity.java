package com.AmmasPastriesSycn;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import java.io.File;

public class MainActivity extends FragmentActivity {

    YouTubePlayerView youTubePlayerView;
    YouTubePlayer youTubePlayer;
    private boolean isPaused = false; // Track whether the video is paused or not

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize UI elements
        youTubePlayerView = findViewById(R.id.youtube_player);
        // Check for network connectivity and initialize YouTube player if available
        if (NetworkUtils.isNetworkAvailable(MainActivity.this)) {
            initializeYouTubePlayer();
        } else {
            handleNoInternetConnection();
        }
    }

    private void initializeYouTubePlayer() {
        // Set up Firebase database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference();

        // Listen for changes in the "Playlist_id" node in the Firebase database
        reference.child("Playlist_id").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the new playlist ID from Firebase
                    String playlistId = dataSnapshot.getValue(String.class);
                    // Refresh or restart the YouTube player
                    refreshOrRestartYouTubePlayer(playlistId);
                } else {
                    playDefaultPlaylist();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshOrRestartYouTubePlayer(String playlistId) {
        // Release the existing YouTube player, if any
        releaseYouTubePlayer();

        // Initialize and play the new YouTube playlist
        playYouTubePlaylist(playlistId);
    }

    private void releaseYouTubePlayer() {
        if (youTubePlayer != null) {
            youTubePlayerView.release();
            youTubePlayer = null;
        }
    }

    private void playYouTubePlaylist(String playlistId) {
        // Configure YouTube player options
        getLifecycle().addObserver(youTubePlayerView);
        IFramePlayerOptions iFramePlayerOptions = new IFramePlayerOptions.Builder()
                .controls(0)
                .mute(1)
                .fullscreen(1)
                .listType("playlist")
                .list(playlistId)
                .build();
        // Initialize YouTube player and start playback
        youTubePlayerView.setVisibility(View.VISIBLE);
        final AbstractYouTubePlayerListener youTubePlayerListener = new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                MainActivity.this.youTubePlayer = youTubePlayer;
                // autoplay & loop the playlist
                MainActivity.this.youTubePlayer.setLoop(true);
                MainActivity.this.youTubePlayer.play();
            }
        };
        youTubePlayerView.initialize(youTubePlayerListener, true, iFramePlayerOptions);
    }

    private void playDefaultPlaylist() {
        // Display a message indicating that the playlist ID was not found, and play a default playlist
        Toast.makeText(MainActivity.this, "Playlist ID not found. Playing Default.", Toast.LENGTH_SHORT).show();
        String defaultPlaylistId = "PLXhkwMTsebvWOQmtoyIZSk0ODnyD-daPl";
        // Ensure that you set youTubePlayer to null after releasing it
        youTubePlayer = null;
        playYouTubePlaylist(defaultPlaylistId);
    }

    private void handleNoInternetConnection() {
        // Handle UI when there is no internet connection
        youTubePlayerView.setVisibility(View.GONE);
        Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
    }

    // Define a handler to control video pause and play
    private final Handler pausePlayHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) {
                // Toggle between play and pause
                if (youTubePlayer != null) {
                    if (isPaused) {
                        youTubePlayer.play();
                    } else {
                        youTubePlayer.pause();
                    }
                    isPaused = !isPaused;
                }
            }
            return true;
        }
    });

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            // Check if the YouTube player is initialized
            if (youTubePlayer != null) {
                // Toggle between pause and play
                isPaused = !isPaused;
                // Show a message indicating the video state
                Toast.makeText(MainActivity.this, isPaused ? "Video Paused" : "Video Resumed", Toast.LENGTH_SHORT).show();
                // If the video is paused, pause it; if it's playing, play it
                if (isPaused) {
                    youTubePlayer.pause();
                } else {
                    youTubePlayer.play();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            // Rewind -N seconds (Toast)
            Toast.makeText(MainActivity.this, "Rewind -N seconds", Toast.LENGTH_SHORT).show();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            // Fast forward +N seconds (Toast)
            Toast.makeText(MainActivity.this, "Fast Forward +N seconds", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            // Press and hold left: Scrubbing rewind (Toast)
            Toast.makeText(MainActivity.this, "Scrubbing rewind", Toast.LENGTH_SHORT).show();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            // Press and hold right: Scrubbing forward (Toast)
            Toast.makeText(MainActivity.this, "Scrubbing forward", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (youTubePlayer != null && !isPaused) {
            // Prevent Ambient Mode when video is playing
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        clearCache(MainActivity.this);
        if (youTubePlayer != null && !isPaused) {
            // Allow Ambient Mode when video is not playing
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public static void clearCache(Context context) {
        try {
            // Use the context to get the cache directory
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                // List all files in the cache directory
                File[] cacheFiles = cacheDir.listFiles();
                if (cacheFiles != null) {
                    for (File cacheFile : cacheFiles) {
                        // Delete each file
                        cacheFile.delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
