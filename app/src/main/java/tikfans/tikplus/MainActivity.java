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
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
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
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
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
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

import java.util.Date;
import java.util.List;
import java.util.Random;

import tikfans.tikplus.model.LogAdsReward;
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

import static tikfans.tikplus.MyChannelApplication.countToday;
import static tikfans.tikplus.util.AppUtil.SKU_HUGE_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_LARGE_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_LARGE_PROMOTE_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_MAX_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_MAX_PROMOTE_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_MINI_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_MINI_PROMOTE_PACKAGE;
import static tikfans.tikplus.util.AppUtil.SKU_SMALL_PACKAGE;
import static tikfans.tikplus.util.PreferenceUtil.IS_FTU;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ChienDichFragment.OnFragmentInteractionListener, LikecheoFragment.OnFragmentInteractionListener, SubcheoFragment.OnFragmentInteractionListener, ChatFragment.OnFragmentInteractionListener, IUnityAdsInitializationListener {

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

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private SQLiteDatabaseHandler db;

    //unity
    private View rootView;
    private String unityGameID = "5252996";
    private boolean testMode = false;
    private String placementId = "rewardedVideo";
    String topAdUnitId = "banner";
    RelativeLayout topBannerView;
    BannerView topBanner;

    private void onRewarded() {
        Toast.makeText(this, String.format(getString(R.string.nhan_duoc_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_VIDEO_REWARD)), Toast.LENGTH_SHORT).show();
        FirebaseUtil.getLogAdsRewardCurrentUserRef().setValue(new LogAdsReward(FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_VIDEO_REWARD), ServerValue.TIMESTAMP));
    }

    private NavigationBarView.OnItemSelectedListener mOnNavigationItemSelectedListener
            = new NavigationBarView.OnItemSelectedListener() {

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
    CardView mPromoVipAccountLayout;
    ImageView mClosePromoteVipAccountImageView;
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
    boolean isShowPromoPackage = false;
    boolean isShowPromoVipAccount = false;

    //unity
    // Listener for banner events:
    private BannerView.IListener bannerListener = new BannerView.IListener() {
        @Override
        public void onBannerLoaded(BannerView bannerAdView) {
            // Called when the banner is loaded.
            Log.v("UnityAdsExample", "onBannerLoaded: " + bannerAdView.getPlacementId());
            if (topBannerView != null && !MyChannelApplication.isVipAccount)
                topBannerView.setVisibility(View.VISIBLE);
            // Enable the correct button to hide the ad
        }

        @Override
        public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo errorInfo) {
            Log.e("UnityAdsExample", "Unity Ads failed to load banner for " + bannerAdView.getPlacementId() + " with error: [" + errorInfo.errorCode + "] " + errorInfo.errorMessage);
            // Note that the BannerErrorInfo object can indicate a no fill (see API documentation).
            if (topBannerView != null) topBannerView.setVisibility(View.GONE);
        }

        @Override
        public void onBannerClick(BannerView bannerAdView) {
            // Called when a banner is clicked.
            Log.v("UnityAdsExample", "onBannerClick: " + bannerAdView.getPlacementId());
        }

        @Override
        public void onBannerLeftApplication(BannerView bannerAdView) {
            // Called when the banner links out of the application.
            Log.v("UnityAdsExample", "onBannerLeftApplication: " + bannerAdView.getPlacementId());
        }
    };

    @Override
    public void onInitializationComplete() {
        Log.v("UnityAdsExample", "onInitializationComplete: ");
        UnityAds.load(placementId, loadListener);
    }

    @Override
    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
        Log.e("UnityAdsExample", "Unity Ads initialization failed with error: [" + error + "] " + message);
    }

    private IUnityAdsLoadListener loadListener = new IUnityAdsLoadListener() {
        @Override
        public void onUnityAdsAdLoaded(String placementId) {
            Log.v("UnityAdsExample", "onUnityAdsAdLoaded: " + placementId);
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

    boolean isUnityAdsLoaded = false;

    public void DisplayRewardedAd() {
        if (isUnityAdsLoaded) {
            UnityAds.show(MainActivity.this, placementId, new UnityAdsShowOptions(), showListener);
        } else {
            if (UnityAds.isInitialized()) {
                UnityAds.load(placementId, loadListener);
            }
        }
    }

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

        //unity
        UnityAds.initialize(getApplicationContext(), unityGameID, testMode, this);
        topBanner = new BannerView(MainActivity.this, topAdUnitId, new UnityBannerSize(320, 50));
        topBannerView = findViewById(R.id.topBanner);

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
                if (!MyChannelApplication.isVipAccount) {
                    topBannerView.setVisibility(View.VISIBLE);
                }
                PreferenceUtil.saveLongPref(PreferenceUtil.LAST_CLOSE_PROMOTE_TIME, System.currentTimeMillis());
            }
        });

        mPromoVipAccountLayout = findViewById((R.id.promotion_vipaccount_layout));
        mClosePromoteVipAccountImageView = findViewById(R.id.close_promote_vip_account_image_view);
        mPromoVipAccountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent buyIntent = new Intent(MainActivity.this, MuaHangActivity.class);
                buyIntent.putExtra(AppUtil.VIP_ACCOUNT_EXTRA, true);
                startActivity(buyIntent);
            }
        });
        mClosePromoteVipAccountImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPromoVipAccountLayout.setVisibility(View.GONE);
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
            isShowPromoPackage = false;
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
//            if ((day + month) % 3 == 1) {
                isShowPromoPackage = true;
                typeOfPromotePackage = TYPE_LARGE_PROMOTE_PACKAGE;
                txtCoinBuyPromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_LARGE_NEW) * 2));
                txtCoinBasePromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_LARGE_NEW)));
            }
            if ((day + month) % 15 == 8 || (day + month) % 15 == 14) {
//            if ((day + month) % 3 == 2) {
                isShowPromoPackage = true;
                typeOfPromotePackage = TYPE_MAX_PROMOTE_PACKAGE;
                txtCoinBuyPromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MAX_NEW) * 120 / 100));
                txtCoinBasePromoPackage.setText(String.format(getString(R.string.mua_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MAX_NEW)));

            }
            if (isShowPromoPackage) {
                mPromoPackageLayout.setVisibility(View.VISIBLE);
                topBannerView.setVisibility(View.GONE);
                initTimer(remainTime);
            } else {
                mPromoPackageLayout.setVisibility(View.GONE);
                isShowPromoVipAccount = true;
            }
        }

        billingClient = BillingClient.newBuilder(getApplicationContext())
                .setListener(purchaseUpdateListener)
                .enablePendingPurchases()
                .build();
        startBillingConnectTion();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnItemSelectedListener(mOnNavigationItemSelectedListener);
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

        if (!AppUtil.isNetworkAvailable(getApplicationContext())) {
            AppUtil.showAlertDialog(MainActivity.this, getString(R.string.khong_ket_noi), getString(R.string.khong_ket_noi_chi_tiet));
        } else {
            try {
                int versionCode = MainActivity.this.getPackageManager().getPackageInfo(MainActivity.this.getPackageName(), 0).versionCode;
                if (versionCode < FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_VERSION)) {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setTitle(getString(R.string.cap_nhat_app_tieu_deu)).
                            setMessage(getString(R.string.cap_nhat_app_chi_tiet)).
                            setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            }).
                            setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    openAppOrGoToStore(FirebaseRemoteConfig.getInstance().getString(RemoteConfigUtil.TIKFANS_PACKAGE_APP));
                                    finish();
                                }
                            }).create();
                    alertDialog.show();
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    int startBillingCount = 0;
    private void startBillingConnectTion() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                if (startBillingCount < 10) {
                    startBillingCount++;
                    startBillingConnectTion();
                }
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                billingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build(),
                        new PurchasesResponseListener() {
                            public void onQueryPurchasesResponse(BillingResult billingResult, List<com.android.billingclient.api.Purchase> purchases) {
                                // check billingResult
                                // process returned purchase list, e.g. display the plans user owns
                                Log.d("KhangPurchase", "onQueryPurchasesResponse for sub: " + purchases.toString());
                                for (Purchase purchase : purchases) {
                                    handlePurchase(purchase);
                                }
                                if (MyChannelApplication.isVipAccount) {
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            topBannerView.setVisibility(View.GONE);
                                            mPromoVipAccountLayout.setVisibility(View.GONE);
                                        }
                                    });
                                } else {
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                topBanner.setListener(bannerListener);
                                                topBanner.load();
                                                topBannerView.addView(topBanner);
                                                if (isShowPromoVipAccount) {
                                                    mPromoVipAccountLayout.setVisibility(View.VISIBLE);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                );
            }
        });
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
            DisplayRewardedAd();
        } else if (id == R.id.nav_subchat) {
            openAppOrGoToStore(FirebaseRemoteConfig.getInstance().getString(RemoteConfigUtil.SUBCHAT_PACKAGE));
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
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "http://play.google.com/store/apps/details?id=" +
                            packageApp)));
        }
    }

    private void signOut() {
        //MyChannelApplication.savedCountToday = countToday;
        countToday = null;
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
//                        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
//                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
//                        // To count with Play market backstack, After pressing back button,
//                        // to taken back to our application, we need to add following flags to intent.
//                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
//                                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
//                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//                        try {
//                            startActivity(goToMarket);
//                        } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "http://play.google.com/store/apps/details?id=" +
                                        getApplicationContext().getPackageName())));
//                        }
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
        int i1 = r.nextInt(99 - 0);
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
        String sku = purchase.getProducts().get(0);
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
                            Log.d("Khang", "consume: " + purchase.getProducts().get(0) + ": " + purchase.getOrderId());

                            if (purchase.getProducts().get(0).equals(SKU_MINI_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MINI_NEW));
                            } else if (purchase.getProducts().get(0).equals(SKU_SMALL_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_SMALL_NEW));
                            } else if (purchase.getProducts().get(0).equals(SKU_LARGE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_LARGE_NEW));
                            } else if (purchase.getProducts().get(0).equals(SKU_HUGE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_HUGE_NEW));
                            } else if (purchase.getProducts().get(0).equals(SKU_MAX_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MAX_NEW));
                            } else if (purchase.getProducts().get(0).equals(SKU_MINI_PROMOTE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MINI_NEW) * 2);
                            } else if (purchase.getProducts().get(0).equals(SKU_LARGE_PROMOTE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_LARGE_NEW) * 2);
                            } else if (purchase.getProducts().get(0).equals(SKU_MAX_PROMOTE_PACKAGE)) {
                                BuySuccessToFireBase(purchase, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_PURCHASE_MAX_NEW) * 120 / 100);
                            }
                            PreferenceUtil.saveLongPref(PreferenceUtil.LAST_CLOSE_PROMOTE_TIME, System.currentTimeMillis());
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
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                MyChannelApplication.isVipAccount = true;
                if (user == null) {
                    return;
                }
                FirebaseUtil.getSubscriptionInfoRef().setValue(new LogPurchase(user.getUid(), purchase.getOrderId(), "tikfans.tikplus", purchase.getProducts().get(0), purchase.getPurchaseTime(), purchase.getPurchaseToken(), 0));
                Log.d("Khang", "subscription:" + purchase.toString());
                FirebaseUtil.getVipAccountRef().setValue(MyChannelApplication.isVipAccount);
            }
        }
    }

    private BillingClient billingClient;


    private void BuySuccessToFireBase(Purchase purchase, final long buyCoin) {
        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser == null) {
            return;
        }
        final DatabaseReference purchaseRef = FirebaseUtil.getUSubPurchaseRef();
        try {
            String purchaseTime = "" + purchase.getPurchaseTime();// de tranh 1 purchase duoc cong coin nhieu lan, thay vi push key len firebase, thi dung purchaseTime lam key de tranh tao lieu logPurchase cho 1 purchase dan den tang coin nhieu lan.
            purchaseRef.child(mFirebaseUser.getUid()).child(purchaseTime).setValue(new LogPurchase(mFirebaseUser.getUid(), purchase.getOrderId(), "tikfans.tikplus", purchase.getProducts().get(0), purchase.getPurchaseTime(), purchase.getPurchaseToken(), buyCoin));
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), String.format(getString(R.string.mua_thanh_cong), buyCoin), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


