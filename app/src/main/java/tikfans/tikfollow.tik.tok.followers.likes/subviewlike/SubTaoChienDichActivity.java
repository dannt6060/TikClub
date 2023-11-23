package tikfans.tikfollow.tik.tok.followers.likes.subviewlike;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.squareup.picasso.Picasso;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;

import tikfans.tikfollow.tik.tok.followers.likes.MuaHangActivity;
import tikfans.tikfollow.tik.tok.followers.likes.MyChannelApplication;
import tikfans.tikfollow.tik.tok.followers.likes.R;
import tikfans.tikfollow.tik.tok.followers.likes.model.LogAdsReward;
import tikfans.tikfollow.tik.tok.followers.likes.util.AppUtil;
import tikfans.tikfollow.tik.tok.followers.likes.util.CircleTransform;
import tikfans.tikfollow.tik.tok.followers.likes.util.CustomTextView;
import tikfans.tikfollow.tik.tok.followers.likes.util.FirebaseUtil;
import tikfans.tikfollow.tik.tok.followers.likes.util.RemoteConfigUtil;
import tikfans.tikfollow.tik.tok.followers.likes.model.ChienDichCuaNguoiDungHienTai;
import tikfans.tikfollow.tik.tok.followers.likes.model.SubCampaign;

public class SubTaoChienDichActivity extends AppCompatActivity implements View.OnClickListener, IUnityAdsInitializationListener {
    private static final int NUMBER_OF_SUB = 1;
    private static final int TIME_REQUIRED = 2;
    private Button mBtnNumberFollow, mBtnNumberHearts, mBtnTimeRequired, mBtnTotalCost, mBtnDone, mBtnVipAccountCoin;
    private ImageView imgToobarBack;
    private CustomTextView mTxtCoin;
    private Dialog mPickerDialog;
    private int mNumberFollowOrder = 10;
    private long mTotalCost = 0;
    private long reduceCoinForVipAccount;
    private int mTimeRequired = 10;
    private ProgressDialog progressDialog;
    private TextView mTxtUserName;
    private ImageView mImageViewUserPhoto;
    private int mCurrentPosSubPicker = 1;
    private int mCurrentPosTimePicker = 2;
    private String mVideoId;
    private String mVideoWebLink;
    private String mUserName;
    private String mUserImg;

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
            Log.v("UnityAdsExample", "onUnityAdsAdLoaded: " + true);
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
            UnityAds.show(SubTaoChienDichActivity.this, placementId, new UnityAdsShowOptions(), showListener);
        } else {
            if (UnityAds.isInitialized()) {
                UnityAds.load(placementId, loadListener);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_sub_campaign);
        Toolbar toolbar = findViewById(R.id
                .toolbar);
        mTxtCoin = findViewById(R.id.coin);
        imgToobarBack = findViewById(R.id.toolbar_back);
        mTxtUserName = findViewById(R.id.txt_user_name);
        mImageViewUserPhoto = findViewById(R.id.unfollow_user_photo);
        setSupportActionBar(toolbar);

        imgToobarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        mVideoWebLink = intent.getStringExtra(AppUtil.SELECTED_VIDEO_WEB_LINK_STRING_EXTRA);
        mVideoId = intent.getStringExtra(AppUtil.SELECTED_VIDEO_ID_STRING_EXTRA);
        mUserName = intent.getStringExtra(AppUtil.SELECTED_USER_NAME_FOR_CAMPAIGN_EXTRA);
        mUserImg = intent.getStringExtra(AppUtil.SELECTED_USER_IMG_FOR_CAMPAIGN_EXTRA);
        mBtnTotalCost = findViewById(R.id.btn_total_cost);
        mBtnVipAccountCoin = findViewById(R.id.btn_vip_account);
        mBtnNumberFollow = findViewById(R.id.btn_number_follow);
        mBtnNumberHearts = findViewById(R.id.btn_number_heart);
        mBtnTimeRequired = (Button) findViewById(R.id.btn_time_required);
        mBtnDone = findViewById(R.id.btn_order_done);
        mBtnNumberFollow.setOnClickListener(this);
        mBtnVipAccountCoin.setOnClickListener(this);
        mBtnTimeRequired.setOnClickListener(this);
        mBtnDone.setOnClickListener(this);
        mPickerDialog = new Dialog(this);
        mPickerDialog.setContentView(R.layout.dialog_picker_layout);
        progressDialog = new ProgressDialog(this);

        mTotalCost = (FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_SUB_CAMPAIGN_COIN_COST_KEY) + mTimeRequired) * mNumberFollowOrder;
        mBtnNumberHearts.setText(String.valueOf(mNumberFollowOrder));
        mBtnTimeRequired.setText(String.valueOf(mTimeRequired));
        mBtnTotalCost.setText(String.valueOf(mTotalCost));

        //unity
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

        mTxtUserName.setText("@" + mUserName);
        try {
            Picasso.get().load(mUserImg).transform(new CircleTransform())
                    .into(mImageViewUserPhoto);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MyChannelApplication.isVipAccount) {
            // reduce 10% cost for Vip Account
            reduceCoinForVipAccount = (FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_SUB_CAMPAIGN_COIN_COST_KEY) + mTimeRequired) * mNumberFollowOrder / 10;
            mTotalCost = (FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_SUB_CAMPAIGN_COIN_COST_KEY) + mTimeRequired) * mNumberFollowOrder * 90 / 100;
            mBtnVipAccountCoin.setText("-" + String.valueOf(reduceCoinForVipAccount));

        } else {
            mTotalCost = (FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_SUB_CAMPAIGN_COIN_COST_KEY) + mTimeRequired) * mNumberFollowOrder;
            ;
            mBtnVipAccountCoin.setText(getString(R.string.upgrade));
            mBtnVipAccountCoin.setClickable(true);
        }
        mBtnTimeRequired.setText(String.valueOf(mTimeRequired));
        mBtnNumberFollow.setText(String.valueOf(mNumberFollowOrder));
        mBtnNumberHearts.setText(String.valueOf(mNumberFollowOrder));
        mBtnTotalCost.setText(String.valueOf(mTotalCost));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_number_follow:
                showPickerDialog(NUMBER_OF_SUB);
                break;
            case R.id.btn_time_required:
                showPickerDialog(TIME_REQUIRED);
                break;
            case R.id.btn_order_done:
                showWarningDialog();
                break;
            case R.id.btn_vip_account:
                Intent buyIntent = new Intent(SubTaoChienDichActivity.this, MuaHangActivity.class);
                buyIntent.putExtra(AppUtil.VIP_ACCOUNT_EXTRA, true);
                startActivity(buyIntent);
                break;
        }
    }

    private void showWarningDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(SubTaoChienDichActivity.this).setTitle(getString(R.string.warning)).setMessage(getString(R.string.tao_chien_dich_canh_bao)).setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createNewCampaign(mNumberFollowOrder);
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

        if (type == NUMBER_OF_SUB) {
            txtTitle.setText(getString(R.string.so_luong_sub));
            txtDetail.setText(getString(R.string.so_luong_sub_chi_tiet));
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(20);
            numberPicker.setDisplayedValues(new String[]{"10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "200", "300", "400", "500", "600", "700", "800", "900", "1000", "5000"});
            numberPicker.setValue(mCurrentPosSubPicker);
        } else {
            txtTitle.setText(getString(R.string.thoi_gian_yeu_cau));
            txtDetail.setText(getString(R.string.thoi_gian_yeu_cau));
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(20);
            numberPicker.setDisplayedValues(new String[]{"5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23","24"});
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
                if (type == NUMBER_OF_SUB) {
                    mCurrentPosSubPicker = numberPicker.getValue();
                    if (mCurrentPosSubPicker < 10) {
                        mNumberFollowOrder = (mCurrentPosSubPicker) * 10;
                    } else {
                        mNumberFollowOrder = (mCurrentPosSubPicker - 9) * 100;
                    }
                    if (mCurrentPosSubPicker == 20) {
                        mNumberFollowOrder = 5000;
                    }
                } else {
                    mCurrentPosTimePicker = numberPicker.getValue();
                    mTimeRequired = numberPicker.getValue() + 4;

                }
                mBtnNumberFollow.setText(String.valueOf(mNumberFollowOrder));
                mBtnNumberHearts.setText(String.valueOf(mNumberFollowOrder));
                mBtnTimeRequired.setText(String.valueOf(mTimeRequired));


                if (MyChannelApplication.isVipAccount) {
                    // reduce 10% cost for Vip Account
                    reduceCoinForVipAccount = (FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_SUB_CAMPAIGN_COIN_COST_KEY) + mTimeRequired) * mNumberFollowOrder / 10;
                    mTotalCost = (FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_SUB_CAMPAIGN_COIN_COST_KEY) + mTimeRequired) * mNumberFollowOrder * 90 / 100;
                    mBtnVipAccountCoin.setText("-" + String.valueOf(reduceCoinForVipAccount));
                    mBtnVipAccountCoin.setClickable(false);

                } else {
                    mTotalCost = (FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_SUB_CAMPAIGN_COIN_COST_KEY) + mTimeRequired) * mNumberFollowOrder;
                    mBtnVipAccountCoin.setText(getString(R.string.upgrade));
                    mBtnVipAccountCoin.setClickable(true);
                }

                mBtnTotalCost.setText(String.valueOf(mTotalCost));
                mPickerDialog.dismiss();
            }
        });
    }

    private void createNewCampaign(final int numberSubOrder) {
        showProgressDialog();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getApplicationContext(), R.string.user_logged_out_error,
                    Toast.LENGTH_SHORT).show();
            hideProgressDialog();
            return;
        }


        final DatabaseReference campaignsRef = FirebaseUtil.getSubCampaignsRef();
        final DatabaseReference campaignCurrentUserRef = FirebaseUtil.getCampaignCurrentUser();
        long coinRate = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_SUB_CAMPAIGN_COIN_COST_KEY);

        FirebaseUtil.getCoinCurrentAccountRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long currentSubCoin = 0;
                try {
                    currentSubCoin = (long) dataSnapshot.getValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (currentSubCoin <= 0 || numberSubOrder < 5) {
                    hideProgressDialog();
                    Toast.makeText(getApplicationContext(), getString(R.string.not_enough_coin), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mTotalCost > currentSubCoin) {
                    hideProgressDialog();
                    Toast.makeText(getApplicationContext(), getString(R.string.not_enough_coin), Toast.LENGTH_LONG).show();
                    Intent buyIntent = new Intent(SubTaoChienDichActivity.this, MuaHangActivity.class);
                    buyIntent.putExtra(AppUtil.VIP_ACCOUNT_EXTRA, false);
                    startActivity(buyIntent);
                    return;
                }
                //create new campaign
                String key = campaignsRef.push().getKey();
                if (key == null || key.length()<=0) return;
                final SubCampaign campaign = new SubCampaign(key, user.getUid(), mUserName, mUserImg, mVideoId, numberSubOrder, coinRate, mTimeRequired, ServerValue.TIMESTAMP, ServerValue.TIMESTAMP, -1);
                campaignsRef.push().setValue(campaign, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, final DatabaseReference firebase) {
                        if (!MyChannelApplication.isVipAccount) {
                            DisplayRewardedAd();
                        }

                        if (error != null) {
                            Toast.makeText(getApplicationContext(), "Error create new campaign: ", Toast.LENGTH_SHORT).show();
                            Log.d("Khang", "Error:" + error.getDetails());

                            hideProgressDialog();
                        } else {
                            ChienDichCuaNguoiDungHienTai chienDichCuaNguoiDungHienTai = new ChienDichCuaNguoiDungHienTai(firebase.getKey(), FirebaseUtil.SUB_CAMPAIGNS);
                            campaignCurrentUserRef.push().setValue(chienDichCuaNguoiDungHienTai);
                            firebase.runTransaction(new Transaction.Handler() {
                                @NonNull
                                @Override
                                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                    SubCampaign subCampaign = mutableData.getValue(SubCampaign.class);
                                    if (subCampaign == null) {
                                        return Transaction.success(mutableData);
                                    }

                                    subCampaign.setKey(firebase.getKey());
                                    mutableData.setValue(subCampaign);
                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                                }
                            });

                            hideProgressDialog();
                            Toast.makeText(getApplicationContext(), getText(R.string
                                    .create_campaign_success), Toast.LENGTH_SHORT).show();
                            setResult(Activity.RESULT_OK);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
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
