package com.AmmasPastriesSycn;

import android.os.Bundle;
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
        getLifecycle().addObserver(youTubePlayerView);
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
                    // Initialize and play the new YouTube playlist
                    playYouTubePlaylist(playlistId);
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

    private void playYouTubePlaylist(String playlistId) {
        // Configure YouTube player options
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            // Check if the YouTube player is initialized
            if (youTubePlayer != null) {
                // If the video is currently playing or paused, treat D-Pad center as a back button press
                onBackPressed();
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
        if (youTubePlayer != null && !isPaused) {
            // Allow Ambient Mode when video is not playing
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
