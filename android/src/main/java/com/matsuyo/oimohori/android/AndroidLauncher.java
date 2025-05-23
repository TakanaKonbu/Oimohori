package com.matsuyo.oimohori.android;

import android.app.AlertDialog;
import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.matsuyo.oimohori.GameMain;
import com.matsuyo.oimohori.AdHandler;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication implements AdHandler {
    private RewardedAd rewardedAd;
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"; // テスト用リワード広告ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        GameMain game = new GameMain();
        game.setAdHandler(this);
        initialize(game, configuration);

        MobileAds.initialize(this, initializationStatus -> {
            loadRewardedAd();
        });
    }

    public void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, AD_UNIT_ID, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(RewardedAd ad) {
                rewardedAd = ad;
                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        rewardedAd = null;
                        loadRewardedAd();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        rewardedAd = null;
                        loadRewardedAd();
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                rewardedAd = null;
            }
        });
    }

    @Override
    public void showAdDialog(String message, Runnable onRewardGranted) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(message)
                .setPositiveButton("はい", (dialog, id) -> {
                    if (rewardedAd != null) {
                        rewardedAd.show(this, rewardItem -> {
                            if (onRewardGranted != null) {
                                runOnUiThread(onRewardGranted);
                            }
                        });
                    } else {
                        loadRewardedAd();
                    }
                })
                .setNegativeButton("いいえ", (dialog, id) -> {
                });
            builder.create().show();
        });
    }

    @Override
    public void showMessageDialog(String message) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(message)
                .setPositiveButton("OK", (dialog, id) -> {
                });
            builder.create().show();
        });
    }
}
