package tikfans.tikplus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
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
import java.util.Date;
import java.util.List;
import java.util.Random;

import tikfans.tikplus.model.ItemVideo;
import tikfans.tikplus.model.LogAdsReward;
import tikfans.tikplus.model.ResultUser;
import tikfans.tikplus.model.ResultVideo;
import tikfans.tikplus.subviewlike.LikecheoFragment;
import tikfans.tikplus.subviewlike.SubcheoFragment;
import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.CircleTransform;
import tikfans.tikplus.util.CustomTextView;
import tikfans.tikplus.util.FirebaseUtil;
import tikfans.tikplus.util.PreferenceUtil;
import tikfans.tikplus.util.RemoteConfigUtil;
import tikfans.tikplus.model.LogPurchase;
import tikfans.tikplus.util.SQLiteDatabaseHandler;
import tikfans.tikplus.util.SecureDate;

import static tikfans.tikplus.util.AppUtil.SKU_HUGE_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_LARGE_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_LARGE_PROMOTE_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_MAX_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_MAX_PROMOTE_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_MINI_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_MINI_PROMOTE_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_SMALL_PACKAGE;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ChienDichFragment.OnFragmentInteractionListener, LikecheoFragment.OnFragmentInteractionListener, SubcheoFragment.OnFragmentInteractionListener, ChatFragment.OnFragmentInteractionListener {

    private ChienDichFragment mChienDichFragment;
    private FragmentManager fragmentMgr;
    private SubcheoFragment mSubcheoFragment;
    private LikecheoFragment mLikecheoFragment;
    private ChatFragment mChatFragment;
    private CustomTextView mTxtCoin;
    private FirebaseAuth mFirebaseAuth;
    private TextView mTxtUserName;
    private ImageView mImageViewPhoto;
    private Dialog mRatingDialog;
    private long coinToUpdate = 0;

    //unity
    private View rootView;
    private String unityGameID = "3737693";
    private boolean testMode = false;
    private String placementId = "rewardedVideo";
    final UnityAdsListener myAdsListener = new UnityAdsListener();

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private SQLiteDatabaseHandler db;

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
                onRewarded();
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
        Toast.makeText(this, String.format(getString(R.string.nhan_duoc_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_VIDEO_REWARD)), Toast.LENGTH_SHORT).show();
        FirebaseUtil.getLogAdsRewardCurrentUserRef().setValue(new LogAdsReward(FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_VIDEO_REWARD), ServerValue.TIMESTAMP));
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            try {
                switch (item.getItemId()) {
                    case R.id.navigation_campaign:
                        replaceContent(1);
                        return true;
                    case R.id.navigation_sub:
                        replaceContent(2);
                        return true;
                    case R.id.navigation_like:
                        replaceContent(3);
                        return true;
                    case R.id.navigation_chat:
                        replaceContent(4);
                        return true;
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            return false;
        }
    };

    //for promote pacakge
    CardView mPromoPackageLayout;
    ImageView mClosePromotPackageImageView;
    TextView txtCoinBuyPromoPackage, mTxtPricePromoPackage, mTxtCountDown, txtCoinBasePromoPackage;
    private CountDownTimer mCountDownTimer;
    int typeOfPromotePackage = 0;
    private static final int TYPE_NO_PROMOTE_PACKAGE = 0;
    private static final int TYPE_MINI_PROMOTE_PACKAGE = 1;
    private static final int TYPE_LARGE_PROMOTE_PACKAGE = 2;
    private static final int TYPE_MAX_PROMOTE_PACKAGE = 3;
    boolean isEnablePromotePackage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Khang", "MainActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mTxtCoin = findViewById(R.id.coin);
        setSupportActionBar(toolbar);
        db = new SQLiteDatabaseHandler(this);

        MobileAds.initialize(this);
        mLikecheoFragment = new LikecheoFragment();
        mChienDichFragment = new ChienDichFragment();
        mSubcheoFragment = new SubcheoFragment();
        mChatFragment = new ChatFragment();
        fragmentMgr = getSupportFragmentManager();

        billingClient = BillingClient.newBuilder(getApplicationContext())
                .setListener(purchaseUpdateListener)
                .enablePendingPurchases()
                .build();

        //for promotion package
        mClosePromotPackageImageView = findViewById(R.id.close_promote_package_image_view);
        mPromoPackageLayout = findViewById(R.id.promotion_package_layout);
        mTxtPricePromoPackage = findViewById(R.id.price_promotion_package);
        txtCoinBuyPromoPackage = findViewById(R.id.txtCoinPromotionPackage);
        txtCoinBasePromoPackage = findViewById(R.id.txtCoinBasePackage);
        mTxtCountDown = findViewById(R.id.txt_promotion_package_countdown);
        isEnablePromotePackage = FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigUtil.TIKFANS_ENABLE_PROMOTE_PACKAGE);
        mPromoPackageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent buyIntent = new Intent(MainActivity.this, MuaHangActivity.class);
                buyIntent.putExtra(AppUtil.VIP_ACCOUNT_EXTRA, false);
                startActivity(buyIntent);
            }
        });
        mClosePromotPackageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPromoPackageLayout.setVisibility(View.GONE);
                PreferenceUtil.saveLongPref(PreferenceUtil.LAST_CLOSE_PROMOTE_TIME, System.currentTimeMillis());
            }
        });
        long lastClosePromoteTime = PreferenceUtil.getLongPref(PreferenceUtil.LAST_CLOSE_PROMOTE_TIME, 0);
        if (isEnablePromotePackage && System.currentTimeMillis() - lastClosePromoteTime > 60 * 60 * 1000) {
            Date currentDate = SecureDate.getInstance().getDate();
            int month = currentDate.getMonth();
            int day = currentDate.getDay();
            int hour = currentDate.getHours();
            int minute = currentDate.getMinutes();
            int second = currentDate.getSeconds();
            long remainTime = 24 * 60 * 60 - ((long) hour * 60 * 60 + minute * 60L + second);
            Log.d("khang", "" + day + " / " + month + " : " + hour + ":" + minute + ":" + second);
            boolean isShowPromoPackage = false;
            if (FirebaseAuth.getInstance().getCurrentUser() == null) signOut();
            long firstSignInTime = PreferenceUtil.getLongPref(PreferenceUtil.FIRST_SIGN_IN_TIME + FirebaseAuth.getInstance().getCurrentUser().getUid(), 0);

            if ((day + month) % 15 == 1 || (day + month) % 15 == 6 || firstSignInTime > SecureDate.getInstance().getDate().getTime() - remainTime * 1000) {
//            if (true) {
                isShowPromoPackage = true;
                typeOfPromotePackage = TYPE_MINI_PROMOTE_PACKAGE;
                txtCoinBuyPromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MINI_NEW) * 2));
                txtCoinBasePromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MINI_NEW)));
            }
            if ((day + month) % 15 == 3 || (day + month) % 15 == 11) {
//            if ((day + month) % 3 == ) {
                isShowPromoPackage = true;
                typeOfPromotePackage = TYPE_LARGE_PROMOTE_PACKAGE;
                txtCoinBuyPromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_LARGE_NEW) * 2));
                txtCoinBasePromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_LARGE_NEW)));
            }
            if ((day + month) % 15 == 8 || (day + month) % 15 == 14) {
//            if ((day + month) % 3 == 2) {
                isShowPromoPackage = true;
                typeOfPromotePackage = TYPE_MAX_PROMOTE_PACKAGE;
                txtCoinBuyPromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MAX_NEW) * 150 / 100));
                txtCoinBasePromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MAX_NEW)));

            }
            if (isShowPromoPackage) {
                mPromoPackageLayout.setVisibility(View.VISIBLE);
                initTimer(remainTime);
            } else {
                mPromoPackageLayout.setVisibility(View.GONE);
            }
        }
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                List<String> skuInAppList = new ArrayList<>();
                //for promotion package
                skuInAppList.add(SKU_MINI_PROMOTE_PACKAGE);
                skuInAppList.add(SKU_LARGE_PROMOTE_PACKAGE);
                skuInAppList.add(SKU_MAX_PROMOTE_PACKAGE);
                SkuDetailsParams.Builder params2 = SkuDetailsParams.newBuilder();
                params2.setSkusList(skuInAppList).setType(BillingClient.SkuType.INAPP);
                billingClient.querySkuDetailsAsync(params2.build(),
                        new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                                try {
                                    for (int i = 0; i < list.size(); i++) {
                                        SkuDetails skuDetail = list.get(i);
                                        String textPrice = skuDetail.getPrice();
                                        if (skuDetail.getSku().equals(SKU_MINI_PROMOTE_PACKAGE) && typeOfPromotePackage == TYPE_MINI_PROMOTE_PACKAGE) {
                                            mTxtPricePromoPackage.setText(textPrice);
                                        }
                                        if (skuDetail.getSku().equals(SKU_LARGE_PROMOTE_PACKAGE) && typeOfPromotePackage == TYPE_LARGE_PROMOTE_PACKAGE) {
                                            mTxtPricePromoPackage.setText(textPrice);
                                        }
                                        if (skuDetail.getSku().equals(SKU_MAX_PROMOTE_PACKAGE) && typeOfPromotePackage == TYPE_MAX_PROMOTE_PACKAGE) {
                                            mTxtPricePromoPackage.setText(textPrice);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                Purchase.PurchasesResult purchasesResultSubs = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
                if (purchasesResultSubs != null && purchasesResultSubs.getPurchasesList() != null) {
                    for (Purchase purchase : purchasesResultSubs.getPurchasesList()) {
                        handlePurchase(purchase);
                    }
                }

                Purchase.PurchasesResult purchasesResultInApp = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
                if (purchasesResultInApp != null && purchasesResultInApp.getPurchasesList() != null) {
                    for (Purchase purchase : purchasesResultInApp.getPurchasesList()) {
                        handlePurchase(purchase);
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_sub);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mFirebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        mTxtCoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent buyIntent = new Intent(MainActivity.this, MuaHangActivity.class);
                buyIntent.putExtra(AppUtil.VIP_ACCOUNT_EXTRA, false);
                startActivity(buyIntent);
            }
        });

        final DatabaseReference coinRef = FirebaseUtil.getCoinCurrentAccountRef();
        coinRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mTxtCoin.setText("" + dataSnapshot.getValue());
                    try {
                        coinToUpdate = dataSnapshot.getValue(long.class);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                } else {
                    coinRef.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            try {
                                mutableData.setValue(0);
                            } catch (ClassCastException e) {
                                Log.d("Khang", "" + getString(R.string.account_error));
                            }
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                try {
                    if (databaseError.getMessage().equals("Permission denied")) {
                        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(MainActivity.this)
                                .setTitle(getString(R.string.app_under_maintain))
                                .setMessage(getString(R.string.app_under_maintain_message))
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                }).create();
                        alertDialog.setCancelable(false);
                        alertDialog.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        final DatabaseReference logAdsRef = FirebaseUtil.getLogAdsRewardCurrentUserRef();
        logAdsRef.removeValue();

        if (FirebaseUtil.getCurrentUserRef() != null) {
            DatabaseReference lastSignInRef = FirebaseUtil.getCurrentUserRef().child(FirebaseUtil.USUB_LAST_SIGN_IN_AT);
            lastSignInRef.setValue(ServerValue.TIMESTAMP);
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        mTxtUserName = headerView.findViewById(R.id.drawer_header_textview_username);
        mImageViewPhoto = headerView.findViewById(R.id.drawer_header_user_icon);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String mUserName = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME, "NONE");
            String mUserImg = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_PHOTO, "NONE");
            mTxtUserName.setText("@" + mUserName);
            Picasso.get()
                    .load(mUserImg)
                    .transform(new CircleTransform())
                    .into(mImageViewPhoto);
        }
    }

    private void initTimer(long timerCount) {
        mCountDownTimer =
                new CountDownTimer(timerCount * 1000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        mTxtCountDown.setText(
                                String.valueOf(((int) (millisUntilFinished / 1000))));
                        long hour = millisUntilFinished / 1000 / 3600;
                        long minute = (millisUntilFinished / 1000 % (60 * 60)) / 60;
                        long second = (millisUntilFinished / 1000 % (60 * 60)) % 60;
                        mTxtCountDown.setText(String.format(getString(R.string.end_in), (int) hour, (int) minute, (int) second));
                    }

                    public void onFinish() {
                        mPromoPackageLayout.setVisibility(View.GONE);
                    }
                };
        mCountDownTimer.start();
    }

    private long lastReplaceContent = 0l;

    private void replaceContent(int pos) {
        if (System.currentTimeMillis() - lastReplaceContent < 600) {
            return;
        }
        lastReplaceContent = System.currentTimeMillis();

        if (this.fragmentMgr == null) {
            this.fragmentMgr = getSupportFragmentManager();
        }
        switch (pos) {
            case 1:
                if (fragmentMgr.findFragmentByTag("one") != null) {
                    //if the fragment exists, show it.
                    fragmentMgr.beginTransaction().show(fragmentMgr.findFragmentByTag("one")).commit();
                } else {
                    //if the fragment does not exist, add it to fragment manager.
                    fragmentMgr.beginTransaction().add(R.id.fragment_container, mChienDichFragment, "one").commit();
                }
                if (fragmentMgr.findFragmentByTag("two") != null) {
                    //if the other fragment is visible, hide it.
                    fragmentMgr.beginTransaction().hide(fragmentMgr.findFragmentByTag("two")).commit();
                }
                if (fragmentMgr.findFragmentByTag("three") != null) {
                    //if the other fragment is visible, hide it.
                    fragmentMgr.beginTransaction().hide(fragmentMgr.findFragmentByTag("three")).commit();
                }
                if (fragmentMgr.findFragmentByTag("four") != null) {
                    //if the other fragment is visible, hide it.
                    fragmentMgr.beginTransaction().hide(fragmentMgr.findFragmentByTag("four")).commit();
                }
                break;
            case 2:
                if (fragmentMgr.findFragmentByTag("two") != null) {
                    //if the fragment exists, show it.
                    fragmentMgr.beginTransaction().show(fragmentMgr.findFragmentByTag("two")).commit();
                } else {
                    //if the fragment does not exist, add it to fragment manager.
                    fragmentMgr.beginTransaction().add(R.id.fragment_container, mSubcheoFragment, "two").commit();
                }
                if (fragmentMgr.findFragmentByTag("one") != null) {
                    //if the other fragment is visible, hide it.
                    fragmentMgr.beginTransaction().hide(fragmentMgr.findFragmentByTag("one")).commit();
                }
                if (fragmentMgr.findFragmentByTag("three") != null) {
                    //if the other fragment is visible, hide it.
                    fragmentMgr.beginTransaction().hide(fragmentMgr.findFragmentByTag("three")).commit();
                }
                if (fragmentMgr.findFragmentByTag("four") != null) {
                    //if the other fragment is visible, hide it.
                    fragmentMgr.beginTransaction().hide(fragmentMgr.findFragmentByTag("four")).commit();
                }
                break;
            case 3:
                if (fragmentMgr.findFragmentByTag("three") != null) {
                    //if the fragment exists, show it.
                    fragmentMgr.beginTransaction().show(fragmentMgr.findFragmentByTag("three")).commit();
                } else {
                    //if the fragment does not exist, add it to fragment manager.
                    fragmentMgr.beginTransaction().add(R.id.fragment_container, mLikecheoFragment, "three").commit();
                }
                if (fragmentMgr.findFragmentByTag("one") != null) {
                    //if the other fragment is visible, hide it.
                    fragmentMgr.beginTransaction().hide(fragmentMgr.findFragmentByTag("one")).commit();
                }
                if (fragmentMgr.findFragmentByTag("two") != null) {
                    //if the other fragment is visible, hide it.
                    fragmentMgr.beginTransaction().hide(fragmentMgr.findFragmentByTag("two")).commit();
                }
                if (fragmentMgr.findFragmentByTag("four") != null) {
                    //if the other fragment is visible, hide it.
                    fragmentMgr.beginTransaction().hide(fragmentMgr.findFragmentByTag("four")).commit();
                }
                break;
            case 4:
                if (fragmentMgr.findFragmentByTag("four") != null) {
                    //if the fragment exists, show it.
                    fragmentMgr.beginTransaction().show(fragmentMgr.findFragmentByTag("four")).commit();
                } else {
                    //if the fragment does not exist, add it to fragment manager.
                    fragmentMgr.beginTransaction().add(R.id.fragment_container, mChatFragment, "four").commit();
                }
                if (fragmentMgr.findFragmentByTag("one") != null) {
                    //if the other fragment is visible, hide it.
                    fragmentMgr.beginTransaction().hide(fragmentMgr.findFragmentByTag("one")).commit();
                }
                if (fragmentMgr.findFragmentByTag("two") != null) {
                    //if the other fragment is visible, hide it.
                    fragmentMgr.beginTransaction().hide(fragmentMgr.findFragmentByTag("two")).commit();
                }
                if (fragmentMgr.findFragmentByTag("three") != null) {
                    //if the other fragment is visible, hide it.
                    fragmentMgr.beginTransaction().hide(fragmentMgr.findFragmentByTag("three")).commit();
                }
                break;
        }
    }


    @Override
    public void forceLogout(Uri uri) {
        signOut();
    }

    @Override
    public void updateCoin(long coin) {
        if (!BuildConfig.DEBUG) {
            try {
                coinToUpdate = coinToUpdate + coin;
            } catch (Exception e) {
                coinToUpdate = 0;
            }
            mTxtCoin.setText("" + coinToUpdate);
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            try {
                super.onBackPressed();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_vip_account) {
            Intent buyIntent = new Intent(MainActivity.this, MuaHangActivity.class);
            buyIntent.putExtra(AppUtil.VIP_ACCOUNT_EXTRA, true);
            startActivity(buyIntent);
        } else if (id == R.id.nav_buy_coin) {
            Intent buyIntent = new Intent(MainActivity.this, MuaHangActivity.class);
            buyIntent.putExtra(AppUtil.VIP_ACCOUNT_EXTRA, false);
            startActivity(buyIntent);
        } else if (id == R.id.nav_xem_quang_cao) {
            Log.d("Khang", "showads");
            if (UnityAds.isReady(placementId)) {
                UnityAds.show(this, placementId);
            } else {
                Toast.makeText(this, getString(R.string.tai_ads_loi), Toast.LENGTH_SHORT).show();
                UnityAds.initialize(this, unityGameID, myAdsListener, testMode);
            }
        } else if (id == R.id.nav_subchat) {
            Uri uri = Uri.parse("market://details?id=" + FirebaseRemoteConfig.getInstance().getString(RemoteConfigUtil.SUBCHAT_PACKAGE));
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                openAppOrGoToStore(FirebaseRemoteConfig.getInstance().getString(RemoteConfigUtil.SUBCHAT_PACKAGE));
            }
        } else if (id == R.id.nav_faq) {
            Intent faqIntent = new Intent(MainActivity.this, CauHoiThuongGapActivity.class);
            startActivity(faqIntent);
        } else if (id == R.id.nav_share) {
            ShareApp();
        } else if (id == R.id.nav_send) {
            feedback();
        } else if (id == R.id.nav_rate) {
            rateApp();
        } else if (id == R.id.nav_logout) {
            signOut();
        } else if (id == R.id.private_policy) {
            try {
                String url = "https://sites.google.com/site/lkstudioprivatepolicy/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openAppOrGoToStore(String packageApp) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageApp);
        if (intent != null) {
            // We found the activity now start the activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Uri uri = Uri.parse("market://details?id=" + packageApp);
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "http://play.google.com/store/apps/details?id=" +
                                packageApp)));
            }
        }
    }

    private void signOut() {
        mFirebaseAuth.signOut();
        db.removeAll();
        startActivity(new Intent(getApplication(), ManHinhDangNhapActivity.class));
        finish();
    }

    private void rateApp() {
        try {
            mRatingDialog = new Dialog(this);
            mRatingDialog.setContentView(R.layout.dialog_rate_layout);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(mRatingDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mRatingDialog.show();
            mRatingDialog.getWindow().setAttributes(lp);
            mRatingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            RatingBar ratingBar = mRatingDialog.findViewById(R.id.rating_bar);
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if (rating > 4.5) {
                        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        // To count with Play market backstack, After pressing back button,
                        // to taken back to our application, we need to add following flags to intent.
                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        try {
                            startActivity(goToMarket);
                        } catch (ActivityNotFoundException e) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    "http://play.google.com/store/apps/details?id=" +
                                            getApplicationContext().getPackageName())));
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.thank_for_rating), Toast.LENGTH_SHORT).show();
                    }
                    PreferenceUtil.saveBooleanPref(PreferenceUtil.IS_RATED, true);
                    mRatingDialog.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ShareApp() {
        Intent i = new Intent(MainActivity.this, MoiBanBeActivity.class);
        startActivity(i);
    }

    private void feedback() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + getString(R.string.email_phai_hoi)));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.tieu_de_phan_hoi));
        emailIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.feedback_text), FirebaseUtil.getCurrentUserId()));
//emailIntent.putExtra(Intent.EXTRA_HTML_TEXT, body); //If you are using HTML in your body text

        startActivity(Intent.createChooser(emailIntent, getString(R.string.email_phai_hoi)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        showDialog();
    }

    private ProgressDialog progressDialog;

    private void hideProgressDialog() {
//        progressDialog.dismiss();
    }

    private void showProgressDialog() {
//        progressDialog.setTitle("MainActivity");
//        progressDialog.show();
    }

    private void showDialog() {
        Random r = new Random();
        int i1 = r.nextInt(99 - 0) + 0;
        i1 = i1 % 8;
        Log.d("Khang", "case: " + i1);
        boolean isRated = PreferenceUtil.getBooleanPref(PreferenceUtil.IS_RATED, false);
        PackageManager pm = getApplicationContext().getPackageManager();
        switch (i1) {
            case 0:
                if (isRated)
                    break;
                rateApp();
                break;
//            case 1:
//                if (!AppUtil.isPackageInstalled(FirebaseRemoteConfig.getInstance().getString(RemoteConfigUtil.ULIKE_PACKAGE_APP), pm)) {
//                    AlertDialog alertDialog = new AlertDialog.Builder(this)
//                            .setTitle(getString(R.string.get_more_like_title))
//                            .setMessage(getString(R.string.get_more_likes_title_message))
//                            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            })
//                            .setPositiveButton(getString(R.string.install), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                    openAppOrGoToStore(FirebaseRemoteConfig.getInstance().getString(RemoteConfigUtil.ULIKE_PACKAGE_APP));
//                                }
//                            }).create();
//                    alertDialog.show();
//                }
//                break;
//            case 3:
//                if (!AppUtil.isPackageInstalled(FirebaseRemoteConfig.getInstance().getString(RemoteConfigUtil.UVIEW_PACKAGE_APP), pm)) {
//                    AlertDialog alertDialog = new AlertDialog.Builder(this)
//                            .setTitle(getString(R.string.get_more_view))
//                            .setMessage(getString(R.string.get_more_view_message))
//                            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            })
//                            .setPositiveButton(getString(R.string.install), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                    openAppOrGoToStore(FirebaseRemoteConfig.getInstance().getString(RemoteConfigUtil.UVIEW_PACKAGE_APP));
//                                }
//                            }).create();
//                    alertDialog.show();
//                }
//                break;
            case 3:

                if (!MainActivity.this.getPackageName().equals(FirebaseRemoteConfig.getInstance().getString(RemoteConfigUtil.TIKFANS_LOCAL_PACKAGE_APP))) {
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.ten_app))
                            .setMessage(getString(R.string.usub_local_app))
                            .setNegativeButton(getString(R.string.huy_bo), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton(getString(R.string.install), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    openAppOrGoToStore(FirebaseRemoteConfig.getInstance().getString(RemoteConfigUtil.TIKFANS_LOCAL_PACKAGE_APP));
                                }
                            }).create();
                    alertDialog.show();
                }
                break;

        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //Check subscription
    private PurchasesUpdatedListener purchaseUpdateListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<com.android.billingclient.api.Purchase> purchaseList) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchaseList != null) {
                for (Purchase purchase : purchaseList) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }
    };
    private AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {

        }
    };

    private boolean isConsumePurchase(Purchase purchase) {
        String sku = purchase.getSku();
        if (sku.equals(SKU_HUGE_PACKAGE) || sku.equals(SKU_LARGE_PACKAGE) || sku.equals(SKU_MAX_PACKAGE) || sku.equals(SKU_MINI_PACKAGE) || sku.equals(SKU_SMALL_PACKAGE)) {
            return true;
        }
        return false;
    }

    void handlePurchase(final Purchase purchase) {
        // Purchase retrieved from BillingClient#queryPurchases or your PurchasesUpdatedListener.

        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (isConsumePurchase(purchase)) {
                ConsumeParams consumeParams =
                        ConsumeParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();

                ConsumeResponseListener listener = new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            // Handle the success of the consume operation.
                            hideProgressDialog();
                            Log.d("Khang", "consume: " + purchase.getSku() + ": " + purchase.getOrderId());

                            if (purchase.getSku().equals(SKU_MINI_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MINI_NEW));
                            } else if (purchase.getSku().equals(SKU_SMALL_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_SMALL_NEW));
                            } else if (purchase.getSku().equals(SKU_LARGE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_LARGE_NEW));
                            } else if (purchase.getSku().equals(SKU_HUGE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_HUGE_NEW));
                            } else if (purchase.getSku().equals(SKU_MAX_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MAX_NEW));
                            }

                        }
                    }
                };
                billingClient.consumeAsync(consumeParams, listener);
            } else {
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
                }
                Toast.makeText(getApplicationContext(), getString(R.string.update_vip_account_success), Toast.LENGTH_SHORT).show();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                MyChannelApplication.isVipAccount = true;
                if (user == null) {
                    return;
                }
                FirebaseUtil.getSubscriptionInfoRef().setValue(new LogPurchase(user.getUid(), purchase.getOrderId(), "tikfans.tikplus", purchase.getSku(), purchase.getPurchaseTime(), purchase.getPurchaseToken(), 0));
                Log.d("Khang", "subscription:" + purchase.toString());
                FirebaseUtil.getVipAccountRef().setValue(MyChannelApplication.isVipAccount);
            }
        }
    }

    private BillingClient billingClient;

    private List<SkuDetails> skuDetailsList;

    private void BuySuccessToFireBase(Purchase purchase, final long buyCoin) {
        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser == null) {
            return;
        }
        final DatabaseReference purchaseRef = FirebaseUtil.getUSubPurchaseRef();
        purchaseRef.child(mFirebaseUser.getUid()).child(purchase.getPurchaseToken()).setValue(new LogPurchase(mFirebaseUser.getUid(), purchase.getOrderId(), "tikfans.tikplus", purchase.getSku(), purchase.getPurchaseTime(), purchase.getPurchaseToken(), buyCoin));
        if (getApplicationContext() != null) {
            Toast.makeText(getApplicationContext(), String.format(getString(R.string.mua_thanh_cong), buyCoin), Toast.LENGTH_SHORT).show();
        }
    }

}


