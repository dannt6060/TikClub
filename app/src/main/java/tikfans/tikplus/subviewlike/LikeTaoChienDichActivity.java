package tikfans.tikplus.subviewlike;

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
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;

import java.util.ArrayList;

import tikfans.tikplus.MuaHangActivity;
import tikfans.tikplus.MyChannelApplication;
import tikfans.tikplus.R;
import tikfans.tikplus.model.ChienDichCuaNguoiDungHienTai;
import tikfans.tikplus.model.LikeCampaign;
import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.CustomTextView;
import tikfans.tikplus.util.FirebaseUtil;
import tikfans.tikplus.util.PreferenceUtil;
import tikfans.tikplus.util.RemoteConfigUtil;

import static tikfans.tikplus.util.AppUtil.SELECTED_VIDEO_ID_STRING_EXTRA;
import static tikfans.tikplus.util.AppUtil.SELECTED_VIDEO_THUMBNAIL_STRING_EXTRA;
import static tikfans.tikplus.util.AppUtil.SELECTED_VIDEO_WEB_LINK_STRING_EXTRA;

public class LikeTaoChienDichActivity extends AppCompatActivity implements View.OnClickListener {
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
        mUserName = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME, "NONE");

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

    private String unityGameID = "3737693";
    private boolean testMode = false;
    private String placementId = "rewardedVideo";
    final UnityAdsListener myAdsListener = new UnityAdsListener();

    private void showAds() {
        if (MyChannelApplication.isVipAccount) return;
        Log.d("Khang", "showads");
        Log.d("Khang", "show unity ads");
        if (UnityAds.isReady(placementId)) {
            UnityAds.show(this, placementId);
        } else {
            UnityAds.initialize(this, unityGameID, myAdsListener, testMode);
        }
    }

    private class UnityAdsListener implements IUnityAdsListener {

        public void onUnityAdsReady(String placementId) {
            // Implement functionality for an ad being ready to show.
//            Log.d("Khang", "onUnityAdsReady: " + placementId);
        }

        @Override
        public void onUnityAdsStart(String placementId) {
            // Implement functionality for a user starting to watch an ad.
//            Log.d("Khang", "onUnityAdsStart: " + placementId);
        }

        @Override
        public void onUnityAdsFinish(String placementId, UnityAds.FinishState finishState) {
            // Implement conditional logic for each ad completion status:
//            Log.d("Khang", "onUnityAdsFinish : " + placementId + " / " + finishState);
            if (finishState == UnityAds.FinishState.COMPLETED) {
//                onRewarded();
                // Reward the user for watching the ad to completion.
            } else if (finishState == UnityAds.FinishState.SKIPPED) {
                // Do not reward the user for skipping the ad.
            } else if (finishState == UnityAds.FinishState.ERROR) {
                // Log an error.
            }
        }

        @Override
        public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String s) {

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
            numberPicker.setMaxValue(19);
            numberPicker.setDisplayedValues(new String[]{"10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "200", "300", "400", "500", "600", "700", "800", "900", "1000"});
            numberPicker.setValue(mCurrentPosLikePicker);
        } else {
            txtTitle.setText(getString(R.string.thoi_gian_yeu_cau));
            txtDetail.setText(getString(R.string.thoi_gian_yeu_cau));
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(19);
            numberPicker.setDisplayedValues(new String[]{"5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"});

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

        final LikeCampaign campaign = new LikeCampaign(user.getUid(), mUserName, mVideoId, mVideoWebLink, mVideoThumbnail, numberLikeOrder, coinRate, mTimeRequired, ServerValue.TIMESTAMP, ServerValue.TIMESTAMP, -1);

        FirebaseUtil.getCoinCurrentAccountRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long currentSubCoin = (long) dataSnapshot.getValue();
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
                campaignsRef.push().setValue(campaign, new DatabaseReference.CompletionListener() {
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
                            firebase.runTransaction(new Transaction.Handler() {
                                @NonNull
                                @Override
                                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                    LikeCampaign likeCampaign = mutableData.getValue(LikeCampaign.class);
                                    if (likeCampaign == null) {
                                        return Transaction.success(mutableData);
                                    }

                                    likeCampaign.setKey(firebase.getKey());
                                    mutableData.setValue(likeCampaign);
                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                                }
                            });

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
                                    showAds();
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
