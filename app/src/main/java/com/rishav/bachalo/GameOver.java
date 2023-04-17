package com.rishav.bachalo;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class GameOver extends AppCompatActivity {

    String GAME_ID = "5248637";
    String AD_ID = "InterstitialAds";

    Boolean TESTMODE = false;


    InterstitialAd mInterstitialAd;
    private static final String TAG = "GameOver";

    TextView tvPoints;
    TextView tvHighest;
    SharedPreferences sharedPreferences;
    ImageView ivNewHighest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over);

        GameView.stopMusic();


        MediaPlayer mediaPlayer = MediaPlayer.create(GameOver.this, R.raw.gameover);
        mediaPlayer.start();


        tvPoints = findViewById(R.id.tvPoints);
        tvHighest = findViewById(R.id.tvHighest);
        ivNewHighest = findViewById(R.id.ivNewHighest);
        int points = getIntent().getExtras().getInt("points");
        tvPoints.setText("" + points);
        sharedPreferences = getSharedPreferences("my_pref", 0);
        int highest = sharedPreferences.getInt("highest", 0);
        if (points > highest) {
            ivNewHighest.setVisibility(View.VISIBLE);
            highest = points;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("highest", highest);
            editor.apply();
        }
        tvHighest.setText("" + highest);


        UnityAds.initialize(getApplicationContext(), GAME_ID, TESTMODE, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                Log.i("Unity", "Unity ads Initialized");
                DisplayInterstitialAd();

            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                Log.i("Unity", "Unity ads Initializeation FAILED");

            }
        });

         IUnityAdsLoadListener loadListener = new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                UnityAds.show((Activity)getApplicationContext(), AD_ID, new UnityAdsShowOptions());
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                Log.e("UnityAdsExample", "Unity Ads failed to load ad for " + placementId + " with error: [" + error + "] " + message);
            }
        };


        IUnityAdsShowListener iUnityAdsShowListener = new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                UnityAds.load(AD_ID);
                UnityAds.show(GameOver.this, AD_ID);
            }

            @Override
            public void onUnityAdsShowStart(String placementId) {

            }

            @Override
            public void onUnityAdsShowClick(String placementId) {

            }

            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {

            }
        };

        UnityAds.load(AD_ID);
        UnityAds.show(GameOver.this, AD_ID);


        initAdmob();
        loadAd();
        display();


    }

    private void DisplayInterstitialAd() {
        UnityAds.load(AD_ID);
    }

    public void restart(View view) {
        Intent intent = new Intent(GameOver.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void exit(View view) {
        finish();
    }

    private void initAdmob() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.i("Admob", "onInitializationCompleted");
            }
        });
    }

    private void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(GameOver.this, getResources().getString(R.string.admob_interstitial_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.i("Admob", "onAdfailedToLoad: " + loadAdError);
                        mInterstitialAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        super.onAdLoaded(interstitialAd);
                        mInterstitialAd = interstitialAd;
                        Log.i("Admob", "Interstitial ad loaded");

                    }
                });

    }

    private void display() {
        if (mInterstitialAd != null) {

            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    Log.i("Admob", "Ad dismiss: ");
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);

                    Log.i("Admob", "Ad failed to show: " + adError);
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    mInterstitialAd.show(GameOver.this);
                    super.onAdShowedFullScreenContent();

                    Log.i("Admob", "Ad displayed");
                }
            });


        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
    }


}