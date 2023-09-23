package com.AmmasPastriesSycn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    private ImageView imageView;
    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer youTubePlayer;
    private FirebaseAuth mAuth;
    private DatabaseReference playlistReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Initialize UI elements
        imageView = findViewById(R.id.imageView);
        youTubePlayerView = findViewById(R.id.youtube_player);

        // Check for network connectivity and user authentication
        if (!NetworkUtils.isNetworkAvailable(this)) {
            handleNoInternetConnection();
        } else if (currentUser == null) {
            navigateToLogin();
        } else {
            initializeYouTubePlayer();
        }
    }

    private void initializeYouTubePlayer() {
        // Set up Firebase database reference for the playlist
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        playlistReference = database.getReference("Playlist_id");

        // Listen for changes in the playlist ID node
        playlistReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String playlistId = dataSnapshot.getValue(String.class);
                    playYouTubePlaylist(playlistId);
                } else {
                    playDefaultPlaylist();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playYouTubePlaylist(String playlistId) {
        // Configure YouTube player options
        IFramePlayerOptions iFramePlayerOptions = new IFramePlayerOptions.Builder()
                .controls(0)
                .mute(1)
                .listType("playlist")
                .list(playlistId)
                .build();

        // Initialize YouTube player and start playback
        getLifecycle().addObserver(youTubePlayerView);
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
        playYouTubePlaylist(defaultPlaylistId);
    }

    private void handleNoInternetConnection() {
        // Handle UI when there is no internet connection
        youTubePlayerView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
    }

    private void navigateToLogin() {
        // Navigate to the LoginActivity if no user is logged in
        Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Release the YouTube player when the activity is destroyed
        if (youTubePlayer != null) {
            youTubePlayerView.release();
        }
    }
}
