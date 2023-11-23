package tikfans.tikfollow.tik.tok.followers.likes.subviewlike;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.squareup.picasso.Picasso;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;

import java.util.ArrayList;

import tikfans.tikfollow.tik.tok.followers.likes.MuaHangActivity;
import tikfans.tikfollow.tik.tok.followers.likes.MyChannelApplication;
import tikfans.tikfollow.tik.tok.followers.likes.R;
import tikfans.tikfollow.tik.tok.followers.likes.model.ChienDichCuaNguoiDungHienTai;
import tikfans.tikfollow.tik.tok.followers.likes.model.LikeCampaign;
import tikfans.tikfollow.tik.tok.followers.likes.model.LogAdsReward;
import tikfans.tikfollow.tik.tok.followers.likes.util.AppUtil;
import tikfans.tikfollow.tik.tok.followers.likes.util.CustomTextView;
import tikfans.tikfollow.tik.tok.followers.likes.util.FirebaseUtil;
import tikfans.tikfollow.tik.tok.followers.likes.util.RemoteConfigUtil;

import static tikfans.tikfollow.tik.tok.followers.likes.util.AppUtil.SELECTED_VIDEO_ID_STRING_EXTRA;
import static tikfans.tikfollow.tik.tok.followers.likes.util.AppUtil.SELECTED_VIDEO_THUMBNAIL_STRING_EXTRA;
import static tikfans.tikfollow.tik.tok.followers.likes.util.AppUtil.SELECTED_VIDEO_WEB_LINK_STRING_EXTRA;

public class LikeTaoChienDichActivity extends AppCompatActivity implements View.OnClickListener, IUnityAdsInitializationListener {
    private static final int NUMBER_OF_LIKE = 1;
    private static final int TIME_REQUIRED = 2;
    private Button mBtnNumberSub, mBtnTimeRequired, mBtnTotalCost, mBtnDone, mBtnVipAccountCoin;
    private ImageView imgToobarBack;
    private CustomTextView mTxtCoin;
    private Dialog mPickerDialog;
    private int mNumberLikeOrder = 10;
    private long mTotalCost = 0;
    private long reduceCoinForVipAccount;
    private int mTimeRequired = 10;
    private ProgressDialog progressDialog;
    private ImageView mImageVideoPhoto;
    private ArrayList<String> mVideoIdRunningCampaignList;
    private int mCurrentPosLikePicker = 1;
    private int mCurrentPosTimePicker = 2;
    private String mVideoWebLink;
    private String mVideoThumbnail;
    private String mVideoId;
    private String mUserName;

    //unity
    private View rootView;
    private String unityGameID = "5252996";
    private boolean testMode = false;
    private String placementId = "rewardedVideo";

    private void onRewarded() {
        Toast.makeText(this, String.format(getString(R.string.nhan_duoc_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_VIDEO_REWARD)), Toast.LENGTH_SHORT).show();
        FirebaseUtil.getLogAdsRewardCurrentUserRef().setValue(new LogAdsReward(FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_VIDEO_REWARD), ServerValue.TIMESTAMP));
    }

    //unity
    @Override
    public void onInitializationComplete() {
        Log.v("UnityAdsExample", "onInitializationComplete: ");
        UnityAds.load(placementId, loadListener);
    }

    @Override
    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
        Log.e("UnityAdsExample", "Unity Ads initialization failed with error: [" + error + "] " + message);
    }

    boolean isUnityAdsLoaded = false;
    private IUnityAdsLoadListener loadListener = new IUnityAdsLoadListener() {
        @Override
        public void onUnityAdsAdLoaded(String placementId) {
            Log.v("UnityAdsExample", "onUnityAdsLoaded: ");
            isUnityAdsLoaded = true;
        }


        @Override
        public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
            Log.e("UnityAdsExample", "Unity Ads failed to load ad for " + placementId + " with error: [" + error + "] " + message);
        }
    };

    private IUnityAdsShowListener showListener = new IUnityAdsShowListener() {
        @Override
        public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
            UnityAds.load(placementId, loadListener);
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
            UnityAds.load(placementId, loadListener);
            Log.v("UnityAdsExample", "onUnityAdsShowComplete: " + placementId);
            if (state.equals(UnityAds.UnityAdsShowCompletionState.COMPLETED)) {
                onRewarded();
            } else {
                // Do not reward the user for skipping the ad
            }
        }
    };
    public void DisplayRewardedAd () {
        if (isUnityAdsLoaded) {
            UnityAds.show(LikeTaoChienDichActivity.this, placementId, new UnityAdsShowOptions(), showListener);
        } else {
            if (UnityAds.isInitialized()) {
                UnityAds.load(placementId, loadListener);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_like_campaign);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mTxtCoin = findViewById(R.id.coin);
        imgToobarBack = findViewById(R.id.toolbar_back);
        mImageVideoPhoto = findViewById(R.id.video_thumb);
        setSupportActionBar(toolbar);

        imgToobarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        mVideoThumbnail = intent.getStringExtra(SELECTED_VIDEO_THUMBNAIL_STRING_EXTRA);
        mVideoWebLink = intent.getStringExtra(SELECTED_VIDEO_WEB_LINK_STRING_EXTRA);
        mVideoId = intent.getStringExtra(SELECTED_VIDEO_ID_STRING_EXTRA);
        mUserName = intent.getStringExtra(AppUtil.SELECTED_USER_NAME_FOR_CAMPAIGN_EXTRA);

        mVideoIdRunningCampaignList = intent.getStringArrayListExtra(AppUtil.RUNNING_CAMPAIGN_PATH_STRING_EXTRA);
        mBtnTotalCost = findViewById(R.id.btn_total_cost);
        mBtnVipAccountCoin = findViewById(R.id.btn_vip_account);
        mBtnNumberSub = findViewById(R.id.btn_number_heart);
        mBtnTimeRequired = (Button) findViewById(R.id.btn_time_required);
        mBtnDone = findViewById(R.id.btn_order_done);
        mBtnNumberSub.setOnClickListener(this);
        mBtnVipAccountCoin.setOnClickListener(this);
        mBtnTimeRequired.setOnClickListener(this);
        mBtnDone.setOnClickListener(this);
        mPickerDialog = new Dialog(this);
        mPickerDialog.setContentView(R.layout.dialog_picker_layout);
        progressDialog = new ProgressDialog(this);

        mTotalCost = (FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_LIKE_CAMPAIGN_COIN_COST_KEY) + mTimeRequired) * mNumberLikeOrder;
        mBtnTimeRequired.setText(String.valueOf(mTimeRequired));
        mBtnTotalCost.setText(String.valueOf(mTotalCost));

        // unity
        UnityAds.initialize(getApplicationContext(), unityGameID, testMode, this);

        DatabaseReference coinRef = FirebaseUtil.getCoinCurrentAccountRef();
        coinRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        long currentCoin = (long) dataSnapshot.getValue();
                        mTxtCoin.setText("" + currentCoin);
                    } catch (ClassCastException e) {
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Picasso.get().load(mVideoThumbnail).into(mImageVideoPhoto);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MyChannelApplication.isVipAccount) {
            // reduce 10% cost for Vip Account
            reduceCoinForVipAccount = (FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_LIKE_CAMPAIGN_COIN_COST_KEY) + mTimeRequired) * mNumberLikeOrder / 10;
            mTotalCost = (FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_LIKE_CAMPAIGN_COIN_COST_KEY) + mTimeRequired) * mNumberLikeOrder * 90 / 100;
            mBtnVipAccountCoin.setText("-" + String.valueOf(reduceCoinForVipAccount));

        } else {
            mTotalCost = (FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_LIKE_CAMPAIGN_COIN_COST_KEY) + mTimeRequired) * mNumberLikeOrder;
            ;
            mBtnVipAccountCoin.setText(getString(R.string.upgrade));
            mBtnVipAccountCoin.setClickable(true);
        }

        mBtnTimeRequired.setText(String.valueOf(mTimeRequired));
        mBtnNumberSub.setText(String.valueOf(mNumberLikeOrder));
        mBtnTotalCost.setText(String.valueOf(mTotalCost));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_number_heart:
                showPickerDialog(NUMBER_OF_LIKE);
                break;
            case R.id.btn_time_required:
                showPickerDialog(TIME_REQUIRED);
                break;
            case R.id.btn_order_done:
                showWarningDialog();
                break;
            case R.id.btn_vip_account:
                Intent buyIntent = new Intent(LikeTaoChienDichActivity.this, MuaHangActivity.class);
                buyIntent.putExtra(AppUtil.VIP_ACCOUNT_EXTRA, true);
                startActivity(buyIntent);
                break;
        }
    }

    private void showWarningDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(LikeTaoChienDichActivity.this).setTitle(getString(R.string.warning)).setMessage(getString(R.string.tao_chien_dich_canh_bao)).setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createNewCampaign(mNumberLikeOrder);
                dialog.dismiss();
            }
        }).setNegativeButton(getString(R.string.huy_bo), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create();
        alertDialog.show();
    }

    private void showPickerDialog(final int type) {

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(mPickerDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mPickerDialog.show();
        mPickerDialog.getWindow().setAttributes(lp);
        mPickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView txtTitle = mPickerDialog.findViewById(R.id.txtPickerTitle);
        TextView txtDetail = mPickerDialog.findViewById(R.id.txtPickerDetail);
        final NumberPicker numberPicker = mPickerDialog.findViewById(R.id.number_picker);
        Button btnCancel = mPickerDialog.findViewById(R.id.btnCancel);
        Button btnSelect = mPickerDialog.findViewById(R.id.btnSelect);

        if (type == NUMBER_OF_LIKE) {
            txtTitle.setText(getString(R.string.so_luot_thich));
            txtDetail.setText(getString(R.string.so_luot_thich_chi_tiet));
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(20);
            numberPicker.setDisplayedValues(new String[]{"10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "200", "300", "400", "500", "600", "700", "800", "900", "1000", "5000"});
            numberPicker.setValue(mCurrentPosLikePicker);
        } else {
            txtTitle.setText(getString(R.string.thoi_gian_yeu_cau));
            txtDetail.setText(getString(R.string.thoi_gian_yeu_cau));
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(20);
            numberPicker.setDisplayedValues(new String[]{"5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24"});

            numberPicker.setValue(mCurrentPosTimePicker);
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPickerDialog.dismiss();
            }
        });
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == NUMBER_OF_LIKE) {
                    mCurrentPosLikePicker = numberPicker.getValue();
                    if (mCurrentPosLikePicker < 10) {
                        mNumberLikeOrder = (mCurrentPosLikePicker) * 10;
                    } else {
                        mNumberLikeOrder = (mCurrentPosLikePicker - 9) * 100;
                    }
                    if (mCurrentPosLikePicker == 20) {
                        mNumberLikeOrder = 5000;
                    }
                } else {
                    mCurrentPosTimePicker = numberPicker.getValue();
                    mTimeRequired = numberPicker.getValue() + 4;

                }
                mBtnNumberSub.setText(String.valueOf(mNumberLikeOrder));
                mBtnTimeRequired.setText(String.valueOf(mTimeRequired));


                if (MyChannelApplication.isVipAccount) {
                    // reduce 10% cost for Vip Account
                    reduceCoinForVipAccount = (FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_LIKE_CAMPAIGN_COIN_COST_KEY) + mTimeRequired) * mNumberLikeOrder / 10;
                    mTotalCost = (FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_LIKE_CAMPAIGN_COIN_COST_KEY) + mTimeRequired) * mNumberLikeOrder * 90 / 100;
                    mBtnVipAccountCoin.setText("-" + String.valueOf(reduceCoinForVipAccount));
                    mBtnVipAccountCoin.setClickable(false);

                } else {
                    mTotalCost = (FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_LIKE_CAMPAIGN_COIN_COST_KEY) + mTimeRequired) * mNumberLikeOrder;
                    mBtnVipAccountCoin.setText(getString(R.string.upgrade));
                    mBtnVipAccountCoin.setClickable(true);
                }

                mBtnTotalCost.setText(String.valueOf(mTotalCost));
                mPickerDialog.dismiss();
            }
        });
    }

    private void createNewCampaign(final int numberLikeOrder) {
        showProgressDialog();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getApplicationContext(), R.string.user_logged_out_error,
                    Toast.LENGTH_SHORT).show();
            hideProgressDialog();
            return;
        }

        final DatabaseReference campaignsRef = FirebaseUtil.getLikeCampaignsRef();
        final DatabaseReference campaignCurrentUserRef = FirebaseUtil.getCampaignCurrentUser();
        long coinRate = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_LIKE_CAMPAIGN_COIN_COST_KEY);

        FirebaseUtil.getCoinCurrentAccountRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long currentSubCoin = 0;
                try {
                    currentSubCoin = (long) dataSnapshot.getValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (currentSubCoin <= 0 || numberLikeOrder < 10) {
                    hideProgressDialog();
                    Toast.makeText(getApplicationContext(), getString(R.string.not_enough_coin), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mTotalCost > currentSubCoin) {
                    hideProgressDialog();
                    Toast.makeText(getApplicationContext(), getString(R.string.not_enough_coin), Toast.LENGTH_LONG).show();
                    Intent buyIntent = new Intent(LikeTaoChienDichActivity.this, MuaHangActivity.class);
                    buyIntent.putExtra(AppUtil.VIP_ACCOUNT_EXTRA, false);
                    startActivity(buyIntent);
                    return;
                }
                //create new campaign
                String key = campaignsRef.push().getKey();
                if (key == null || key.length() <=0) return;
                final LikeCampaign campaign = new LikeCampaign(key, user.getUid(), mUserName, mVideoId, mVideoThumbnail, numberLikeOrder, coinRate, mTimeRequired, ServerValue.TIMESTAMP, ServerValue.TIMESTAMP, -1);

                campaignsRef.child(key).setValue(campaign, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, final DatabaseReference firebase) {
                        if (error != null) {
                            try {
                                Toast.makeText(getApplicationContext(), "Error create new campaign: ", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            hideProgressDialog();
                        } else {
                            ChienDichCuaNguoiDungHienTai chienDichCuaNguoiDungHienTai = new ChienDichCuaNguoiDungHienTai(firebase.getKey(), FirebaseUtil.LIKE_CAMPAIGNS);
                            campaignCurrentUserRef.push().setValue(chienDichCuaNguoiDungHienTai);
                            hideProgressDialog();
                            try {
                                Toast.makeText(getApplicationContext(), getText(R.string.create_campaign_success), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            setResult(Activity.RESULT_OK);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!MyChannelApplication.isVipAccount) {
                                        DisplayRewardedAd();
                                    }
                                    finish();
                                }
                            }, 500);
                        }

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressDialog();
            }
        });
    }


    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        try {
            if (progressDialog != null && !progressDialog.isShowing()) {
                progressDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
