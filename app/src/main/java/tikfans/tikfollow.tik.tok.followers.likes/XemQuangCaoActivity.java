package tikfans.tikfollow.tik.tok.followers.likes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ServerValue;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;

import tikfans.tikfollow.tik.tok.followers.likes.util.FirebaseUtil;
import tikfans.tikfollow.tik.tok.followers.likes.util.RemoteConfigUtil;
import tikfans.tikfollow.tik.tok.followers.likes.model.LogAdsReward;


public class XemQuangCaoActivity extends AppCompatActivity implements IUnityAdsInitializationListener {
    ProgressDialog progressDialog;
    private boolean isShowed = false;
    private String unityGameID = "5252996";
    private boolean testMode = false;
    private String placementId = "rewardedVideo";


    @Override
    public void onInitializationComplete() {
        DisplayRewardedAd();
    }

    @Override
    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
        Log.e("UnityAdsExample", "Unity Ads initialization failed with error: [" + error + "] " + message);
    }

    private IUnityAdsLoadListener loadListener = new IUnityAdsLoadListener() {
        @Override
        public void onUnityAdsAdLoaded(String placementId) {
            UnityAds.show((Activity)getApplicationContext(), placementId, new UnityAdsShowOptions(), showListener);
        }

        @Override
        public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
            Log.e("UnityAdsExample", "Unity Ads failed to load ad for " + placementId + " with error: [" + error + "] " + message);
        }
    };

    private IUnityAdsShowListener showListener = new IUnityAdsShowListener() {
        @Override
        public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
            Log.e("UnityAdsExample", "Unity Ads failed to show ad for " + placementId + " with error: [" + error + "] " + message);
        }

        @Override
        public void onUnityAdsShowStart(String placementId) {
            Log.v("UnityAdsExample", "onUnityAdsShowStart: " + placementId);
        }

        @Override
        public void onUnityAdsShowClick(String placementId) {
            Log.v("UnityAdsExample", "onUnityAdsShowClick: " + placementId);
        }

        @Override
        public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
            Log.v("UnityAdsExample", "onUnityAdsShowComplete: " + placementId);
            if (state.equals(UnityAds.UnityAdsShowCompletionState.COMPLETED)) {
                // Reward the user for watching the ad to completion
                if (getApplicationContext()!= null) {
                    Toast.makeText(getApplicationContext(), String.format(getString(R.string.nhan_duoc_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_VIDEO_REWARD)), Toast.LENGTH_SHORT).show();
                }
                FirebaseUtil.getLogAdsRewardCurrentUserRef().setValue(new LogAdsReward(2, ServerValue.TIMESTAMP));
                finish();
            } else {
                // Do not reward the user for skipping the ad
            }
        }
    };
    public void DisplayRewardedAd () {
        if (UnityAds.isInitialized()) {
            UnityAds.load(placementId, loadListener);
        }
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_reward_video_ads);
        this.progressDialog = new ProgressDialog(this);
        this.progressDialog.setMessage(getString(R.string.doi_tai_video));
        this.progressDialog.show();
        UnityAds.initialize(getApplicationContext(), unityGameID, testMode, this);
    }


    public void onRewardedVideoAdLoaded() {
        if (this.progressDialog != null && this.progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }
        isShowed = true;
    }

    public void onRewardedVideoAdClosed() {
        finish();
    }


    public void onRewardedVideoAdFailedToLoad(int i) {
        isShowed = false;
        if (progressDialog != null && progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }
        Log.d("Khang", "show unity ads");
        if (UnityAds.isInitialized()) {
            UnityAds.load(placementId, loadListener);
        } else {
            Toast.makeText(this, getString(R.string.tai_ads_loi), Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
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

}

