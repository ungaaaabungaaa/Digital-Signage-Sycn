package com.AmmasPastriesSycn;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
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

    YouTubePlayerView  youTubePlayerView;
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
                .ccLoadPolicy(0)
                .ivLoadPolicy(0)
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

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            // Check if the YouTube player is initialized
            if (youTubePlayer != null) {
                if (isPaused) {
                    // If the video is currently paused, resume playback
                    youTubePlayer.play();
                    Toast.makeText(MainActivity.this, "Resumed", Toast.LENGTH_SHORT).show();
                } else {
                    // If the video is currently playing, pause it
                    youTubePlayer.pause();
                    Toast.makeText(MainActivity.this, "Paused", Toast.LENGTH_SHORT).show();
                }
                // Toggle the pause state
                isPaused = !isPaused;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onStop() {
        // App-specific method to stop playback.
        youTubePlayerView.release();
        super.onStop();
    }

}