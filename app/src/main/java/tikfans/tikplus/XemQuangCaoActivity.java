package tikfans.tikplus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.database.ServerValue;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;

import tikfans.tikplus.util.FirebaseUtil;
import tikfans.tikplus.util.RemoteConfigUtil;
import tikfans.tikplus.model.LogAdsReward;


public class XemQuangCaoActivity extends AppCompatActivity implements RewardedVideoAdListener, IUnityAdsListener {
    private RewardedVideoAd mRewardedVideoAd;
    ProgressDialog progressDialog;
    private boolean isShowed = false;
    private String unityGameID = "3737693";
    private boolean testMode = false;
    private String placementId = "rewardedVideo";
    final UnityAdsListener myAdsListener = new UnityAdsListener();

    public void onRewardedVideoAdLeftApplication() {
    }

    public void onRewardedVideoAdOpened() {
    }

    public void onRewardedVideoCompleted() {
    }

    public void onRewardedVideoStarted() {
    }


    private class UnityAdsListener implements IUnityAdsListener {

        public void onUnityAdsReady(String placementId) {
            // Implement functionality for an ad being ready to show.
            Log.d("Khang", "onUnityAdsReady: " + placementId);
        }

        @Override
        public void onUnityAdsStart(String placementId) {
            // Implement functionality for a user starting to watch an ad.
            Log.d("Khang", "onUnityAdsStart: " + placementId);
        }

        @Override
        public void onUnityAdsFinish(String placementId, UnityAds.FinishState finishState) {
            // Implement conditional logic for each ad completion status:
            Log.d("Khang", "onUnityAdsFinish : " + placementId + " / " + finishState);
            if (finishState == UnityAds.FinishState.COMPLETED) {
                if (getApplicationContext()!= null) {
                    Toast.makeText(getApplicationContext(), String.format(getString(R.string.nhan_duoc_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_VIDEO_REWARD)), Toast.LENGTH_SHORT).show();
                }
                FirebaseUtil.getLogAdsRewardCurrentUserRef().setValue(new LogAdsReward(2, ServerValue.TIMESTAMP));
                finish();
                // Reward the user for watching the ad to completion.
            } else if (finishState == UnityAds.FinishState.SKIPPED) {
                // Do not reward the user for skipping the ad.
            } else if (finishState == UnityAds.FinishState.ERROR) {
                // Log an error.
            }
        }

        @Override
        public void onUnityAdsError(UnityAds.UnityAdsError error, String message) {
            // Implement functionality for a Unity Ads service error occurring.
            Log.d("Khang", "onUnityAdsError: " + message + " / " + error.toString());
        }
    }

    private void onRewarded() {
        if (getApplicationContext()!= null) {
            Toast.makeText(getApplicationContext(), String.format(getString(R.string.nhan_duoc_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_VIDEO_REWARD)), Toast.LENGTH_SHORT).show();
        }
        FirebaseUtil.getLogAdsRewardCurrentUserRef().setValue(new LogAdsReward(FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_VIDEO_REWARD), ServerValue.TIMESTAMP));
        finish();
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_reward_video_ads);
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        this.mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        this.mRewardedVideoAd.setRewardedVideoAdListener(this);
        this.progressDialog = new ProgressDialog(this);
        this.progressDialog.setMessage(getString(R.string.doi_tai_video));
        this.progressDialog.show();
        loadRewardedVideoAd();
        UnityAds.initialize(this, unityGameID, myAdsListener, testMode);
    }

    private void loadRewardedVideoAd() {
        this.mRewardedVideoAd.loadAd(getString(R.string.admob_reward_ad_unit_id), new AdRequest.Builder().build());
    }

    public void onRewardedVideoAdLoaded() {
        if (this.progressDialog != null && this.progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }
        this.mRewardedVideoAd.show();
        isShowed = true;
    }

    public void onRewardedVideoAdClosed() {
        finish();
    }

    public void onRewarded(RewardItem rewardItem) {
        onRewarded();
    }

    public void onRewardedVideoAdFailedToLoad(int i) {
        isShowed = false;
        if (progressDialog != null && progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }
        Log.d("Khang", "show unity ads");
        if (UnityAds.isReady(placementId)) {
            UnityAds.show(this, placementId);
        } else {
            Toast.makeText(this, getString(R.string.tai_ads_loi), Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    public void onResume() {
        this.mRewardedVideoAd.resume(this);
        super.onResume();
    }

    public void onPause() {
        this.mRewardedVideoAd.pause(this);
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mRewardedVideoAd.destroy(this);
        if (this.progressDialog != null && this.progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }
    }

    @Override
    public void finish() {
        if (isShowed) {
            setResult(Activity.RESULT_OK);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
        super.finish();
    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onUnityAdsReady(String s) {

    }

    @Override
    public void onUnityAdsStart(String s) {

    }

    @Override
    public void onUnityAdsFinish(String s, UnityAds.FinishState finishState) {

    }

    @Override
    public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String s) {

    }
}

