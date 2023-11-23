package tikfans.tikfollow.tik.tok.followers.likes.subviewlike;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.api.services.youtube.model.VideoGetRatingResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
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
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

import tikfans.tikfollow.tik.tok.followers.likes.MuaHangActivity;
import tikfans.tikfollow.tik.tok.followers.likes.MyChannelApplication;
import tikfans.tikfollow.tik.tok.followers.likes.R;
import tikfans.tikfollow.tik.tok.followers.likes.UnFollowUserActivity;
import tikfans.tikfollow.tik.tok.followers.likes.model.ItemVideo;
import tikfans.tikfollow.tik.tok.followers.likes.model.UnFollowUSer;
import tikfans.tikfollow.tik.tok.followers.likes.util.AppUtil;
import tikfans.tikfollow.tik.tok.followers.likes.util.CircleTransform;
import tikfans.tikfollow.tik.tok.followers.likes.util.FirebaseUtil;
import tikfans.tikfollow.tik.tok.followers.likes.util.PreferenceUtil;
import tikfans.tikfollow.tik.tok.followers.likes.util.RemoteConfigUtil;
import tikfans.tikfollow.tik.tok.followers.likes.util.SQLiteDatabaseHandler;
import tikfans.tikfollow.tik.tok.followers.likes.util.SecureDate;
import tikfans.tikfollow.tik.tok.followers.likes.model.LogAdsReward;
import tikfans.tikfollow.tik.tok.followers.likes.model.LogSub;
import tikfans.tikfollow.tik.tok.followers.likes.model.SubCampaign;
import tikfans.tikfollow.tik.tok.followers.likes.model.CountToday;
import tikfans.tikfollow.tik.tok.followers.likes.service.DemNguocThoiGianServices;

import static android.app.Activity.RESULT_OK;
import static com.unity3d.scar.adapter.common.Utils.runOnUiThread;
import static tikfans.tikfollow.tik.tok.followers.likes.MyChannelApplication.countToday;
import static tikfans.tikfollow.tik.tok.followers.likes.util.AppUtil.SUB_CAMPAIGN_TYPE;
import static tikfans.tikfollow.tik.tok.followers.likes.util.AppUtil.convertStringToInteger;
import static tikfans.tikfollow.tik.tok.followers.likes.util.AppUtil.isAdminVer;
import static tikfans.tikfollow.tik.tok.followers.likes.util.FirebaseUtil.CAMPAIGN_LAST_CHANGE_TIME_STAMP;
import static tikfans.tikfollow.tik.tok.followers.likes.util.FirebaseUtil.getCurrentUserRef;
import static tikfans.tikfollow.tik.tok.followers.likes.util.FirebaseUtil.getSubCampaignsRef;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class SubcheoFragment extends Fragment
        implements View.OnClickListener, IUnityAdsInitializationListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MAX_QUERRY_FAIL = 8;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //for overlay
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private static final int CODE_REWARD_VIDEO_ACTIVITY = 3000;
    private static final int CODE_REQUEST_CHECK_UNFOLLOW = 4000;

    private OnFragmentInteractionListener mListener;

    private ImageView mUserImg;
    private TextView mUserNameTextView;
    private Button mBtnLike, mBtnEarnCoin, mBtnReload;
    private Button mBtnSeeOther;
    private Button mBtnEarnCoinNoChannel;
    //    private ArrayList<SubCampaign> mCampaignsList;
    private SubCampaign mSubCampaign;
    private int numberQueryFail = 0;
    private ArrayList<String> mChannelSubList;
    private ArrayList<UnFollowUSer> mUnFollowUSerArrayList;
    //    private int mCurrentPosCampaign = 0;
    private ProgressDialog progressDialog;
    private ProgressDialog checkingFollowDialog;
    private boolean isWaitingForSub = false;

    private RelativeLayout mPageContentLayout;
    private RelativeLayout mNoPageLayout;
    private TextView mTxtChannel, mTxtChannelExtra, mTxtInstruction;
    private RelativeLayout mInstructionLayout, mCountDownLayout;
    private TextView mTxtCountdown;
    private LinearLayout mLikePageButtonLayout;
    private CardView mCampaignHeaderLayout;

    private CountDownTimer mCountDownTimer;
    private int timerCount = 10;
    private Context mContext;
    private AdView mAdView;
    private String myUserName;
    private long mLastTimeQuery = 0;
    private boolean isGotAllSubscribedList = false;
    private ProgressDialog dummyCheckingSubscriberDialog;
    private Long mGotoYTTime = 0L;

    private View rootView;
    private String unityGameID = "5252996";
    private boolean testMode = false;
    private String placementId = "rewardedVideo";
    private View bannerView;
    private int loadBannerViewTryCount = 0;
    private SQLiteDatabaseHandler db;
    private ArrayList<ItemVideo> itemVideoArrayList = new ArrayList<>();

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
            UnityAds.show(getActivity(), placementId, new UnityAdsShowOptions(), showListener);
        } else {
            if (UnityAds.isInitialized()) {
                UnityAds.load(placementId, loadListener);
            }
        }
    }

    //for banner
    // Listener for banner events:
    private BannerView.IListener bannerListener = new BannerView.IListener() {
        @Override
        public void onBannerLoaded(BannerView bannerAdView) {
            // Called when the banner is loaded.
            Log.v("UnityAdsExample", "onBannerLoaded: " + bannerAdView.getPlacementId());
            // Enable the correct button to hide the ad
            BannerView bottomBanner = new BannerView(getActivity(), "banner", new UnityBannerSize(320, 50));
            // Set the listener for banner lifecycle events:
            bottomBanner.setListener(bannerListener);
            ViewGroup bottomBannerView = ((ViewGroup) rootView.findViewById(R.id.adView));

            if (bottomBanner.getParent() != null) {
                ((ViewGroup) bottomBanner.getParent()).removeView(bottomBanner);
            }
            ((ViewGroup) rootView.findViewById(R.id.adView)).addView(bottomBanner);
            Log.d("Khang", "onUnityBannerLoaded: " + placementId);
        }

        @Override
        public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo errorInfo) {
            Log.e("UnityAdsExample", "Unity Ads failed to load banner for " + bannerAdView.getPlacementId() + " with error: [" + errorInfo.errorCode + "] " + errorInfo.errorMessage);
            // Note that the BannerErrorInfo object can indicate a no fill (see API documentation).
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

    public SubcheoFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mContext = getContext();
//        mCampaignsList = new ArrayList<>();

        myUserName = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME, "");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("Khang", "sub4subFragment oncreate");
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_sub4sub, container, false);
        mUserImg = rootView.findViewById(R.id.unfollow_user_photo);
        mUserNameTextView = rootView.findViewById(R.id.txt_user_name);
        mBtnLike = rootView.findViewById(R.id.btnLike);
        mBtnSeeOther = rootView.findViewById(R.id.btnSeeOther);
        mPageContentLayout = rootView.findViewById(R.id.page_like_content_layout);
        mNoPageLayout = rootView.findViewById(R.id.no_campaign_layout);
        mTxtChannel = rootView.findViewById(R.id.txtNoPage);
        mTxtChannelExtra = rootView.findViewById(R.id.txt_no_channel_extra);
        mTxtInstruction = rootView.findViewById(R.id.txt_instruction);


        mInstructionLayout = rootView.findViewById(R.id.instruction_layout);
        mCountDownLayout = rootView.findViewById(R.id.countdown_layout);
        mTxtCountdown = rootView.findViewById(R.id.textview_countdown);
        mLikePageButtonLayout = rootView.findViewById(R.id.like_page_button_layout);
        mCampaignHeaderLayout = rootView.findViewById(R.id.channelInfoLayout);
        mBtnEarnCoin = rootView.findViewById(R.id.btn_earn_coin);
        mBtnEarnCoinNoChannel = rootView.findViewById(R.id.btn_earn_coin_no_channel);
        mBtnReload = rootView.findViewById(R.id.btn_reload);

        //for checking follow by webview
        getUserInfoByWebView(false);

        mAdView = rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();

        db = new SQLiteDatabaseHandler(getContext());
        itemVideoArrayList = db.getAllItemVideo();

        //unity
//        if (bannerView == null && !MyChannelApplication.isVipAccount) {
//            UnityBanners.setBannerPosition(BannerPosition.BOTTOM_CENTER);
//            UnityBanners.loadBanner(getActivity(), "banner");
//        }
        // Initialize the SDK:
//        final IUnityBannerListener myBannerListener = new UnityBannerListener();
//        UnityBanners.setBannerListener(myBannerListener);
        UnityAds.initialize(getContext(), unityGameID, testMode, this);

        mCampaignHeaderLayout.setOnClickListener(this);
        mBtnLike.setOnClickListener(this);
        mBtnSeeOther.setOnClickListener(this);
        mBtnEarnCoin.setOnClickListener(this);
        mBtnEarnCoinNoChannel.setOnClickListener(this);
        mBtnReload.setOnClickListener(this);
        progressDialog = new ProgressDialog(getContext());
        checkingFollowDialog = new ProgressDialog(getContext());
        mChannelSubList = new ArrayList<>();

        //dummy check subscribed
        dummyCheckingSubscriberDialog = new ProgressDialog(getContext());
        dummyCheckingSubscriberDialog.setTitle(getString(R.string.checking_subscriber));
        dummyCheckingSubscriberDialog.setMessage(getString(R.string.checking_subscriber_message));
        dummyCheckingSubscriberDialog.setCancelable(false);
        dummyCheckingSubscriberDialog.show();

        mUnFollowUSerArrayList = new ArrayList<>();
        final DatabaseReference unSubListRef = FirebaseUtil.getUnSubscribedListRef();
        unSubListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    mUnFollowUSerArrayList.add(dataSnapshot.getValue(UnFollowUSer.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mUnFollowUSerArrayList.size() > 0) {
                        Intent i = new Intent(getActivity(), UnFollowUserActivity.class);
                        i.putExtra(AppUtil.UNFOLLOW_USER_LIST_EXTRA, mUnFollowUSerArrayList);
                        i.putExtra(AppUtil.UNFOLLOW_CHECK_ALL_LIST_USER_NAME, false);
                        startActivity(i);
                    }
                    if (dummyCheckingSubscriberDialog != null && dummyCheckingSubscriberDialog.isShowing()) {
                        dummyCheckingSubscriberDialog.dismiss();
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }, 2500);

        initTimer();
        getTodaySubCount();
        getAllSubscribedList();


//        if (mNumberSubToday < FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.SUBCHAT_LIMIT_SUB)) {
//            retryQueryDatabase();
//        }

        return rootView;
    }

    private void getTodaySubCount() {
        if (FirebaseUtil.getCountTodayRef() == null) {
            return;
        }
        if (countToday == null) {
            FirebaseUtil.getCountTodayRef().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        countToday = dataSnapshot.getValue(CountToday.class);
                        Log.d("khang", "subCheo countToday: " + countToday);
                        if (SecureDate.getInstance().getDate().getTime() - (Long) countToday.getTime() > 20 * 60 * 60 * 1000) {
                            countToday = new CountToday(0, 0, SecureDate.getInstance().getDate().getTime(), 0L);
                            Log.d("khang", "refresh countToday: " + countToday);
                        }
                    } else {
                        countToday = new CountToday(0, 0, 0L, 0L);
                    }
//                    if (savedCountToday != null) {
//                        Log.d("khang", "check savedCountToday: " + savedCountToday + " / countToday: " + countToday);
//                        countToday.setLike(Math.max(countToday.like, savedCountToday.getLike()));
//                        countToday.setSub(Math.max(countToday.getSub(), savedCountToday.getSub()));
//                        Log.d("khang", "updated CountToday: " + countToday);
//                        savedCountToday = null;
//                    }
                    Log.d("khang_check", "getTodaySubCount Done: " + countToday.toString());
                    getAllSubscribedList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("khang_check", "getTodaySubCount error: " + databaseError.getDetails());
                    countToday = new CountToday(0, 0, 0L, 0L);
//                    if (savedCountToday != null) {
//                        Log.d("khang", "check savedCountToday: " + savedCountToday + " / countToday: " + countToday);
//                        countToday.setLike(Math.max(countToday.like, savedCountToday.getLike()));
//                        countToday.setSub(Math.max(countToday.getSub(), savedCountToday.getSub()));
//                        Log.d("khang", "updated CountToday: " + countToday);
//                        savedCountToday = null;
//                    }
                    getAllSubscribedList();
                }
            });
        } else {
            getAllSubscribedList();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    // for checking follow
    private int myFollowingByWebView = -1;
    private int myOldFollowingByWebView = -1;
    private int myFollowingByApi = -1;
    private int myOldFollowingbyAPI = -1;

    private void getUserInfoToUpdateCampaignByWebView(String mUserName) {
        if (isGettingUserInfoByWebView) return;
        final boolean[] isLoaded = {false}; // haom onloadFinish bi goi 2 lan, chi xu ly o lan goi dau tien
        try {
            showProgressDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String userLink = AppUtil.TIKTOK_PREFIX_LINK + mUserName;
        Log.e("khangcheckfollow", "getUserInfoByWebView: user link " + userLink);
        //newGetData
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {

                //Background work here
                Document document = null;
                try {
                    document = Jsoup.connect(userLink).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Document finalDocument1 = document;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            hideProgressDialog();
                            String imageUrl = "";
                            Elements findImgUrlElementList = finalDocument1.getElementsByTag("meta");
                            for (Element e : findImgUrlElementList) {
                                String content = e.attributes().get("content");
                                if (content.contains("expires=")) {
                                    imageUrl = content;
                                }
                                if (content.contains("/video/")) {
                                    break;
                                }
                            }

                            Log.e("khang", "onLoadFinish: Avatar Url: " + imageUrl);
                            if (!imageUrl.equals("")) {
                                Log.d("khang", "onReceivedUserInfo img: " + imageUrl);
                                if (mSubCampaign != null && mSubCampaign.getKey() != null && mSubCampaign.getKey().equals(mCheckingImgbyKey)) {
                                    Picasso.get().load(imageUrl).transform(new CircleTransform())
                                            .into(mUserImg);
                                    final DatabaseReference campaignUserImgRef = getSubCampaignsRef().child(mSubCampaign.getKey()).child("userImg");
                                    campaignUserImgRef.setValue(imageUrl);
                                } else {
                                    Log.d("khang", "onReceivedUserInfo img: do not match with current campaign" + imageUrl);
                                }

                            }

                        } catch (Exception e) {
                            Log.e("khang", e.toString());
                        }
                    }
                });
            }
        });
    }

    boolean isGettingUserInfoByWebView = false;

    private int getDataFailedCount = 0;
    private void getUserInfoByWebView(boolean isNeedCheckFollow) {
        final boolean[] isLoaded = {false}; // haom onloadFinish bi goi 2 lan, chi xu ly o lan goi dau tien
        try {
            if (checkingFollowDialog != null && !checkingFollowDialog.isShowing() && isNeedCheckFollow) {
                checkingFollowDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String mUserName = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME, "NONE");
        String userLink = AppUtil.TIKTOK_PREFIX_LINK + mUserName;
        //newGetData
        ExecutorService executor = Executors.newSingleThreadExecutor();
        long beginTime = System.currentTimeMillis();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {

                //Background work here
                Document document = null;
                try {
                    document = Jsoup.connect(userLink).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Document finalDocument1 = document;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (checkingFollowDialog != null && checkingFollowDialog.isShowing()) {
                                checkingFollowDialog.dismiss();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        int followingInt = -1;

                        //get user follow count
                        try {
                            JSONObject user;
                            String userName;
                            user = new JSONObject(finalDocument1.getElementById("SIGI_STATE").data()).getJSONObject("UserModule").getJSONObject("stats");
                            userName = user.names().getString(0);
                            followingInt = user.getJSONObject(userName).getInt("followingCount");
                            Log.e("khangcheckfollow", "getUserInfoByWebView case1: " + userName + " following: " + followingInt);

                        } catch (Exception e) {
                            e.printStackTrace();
                            FirebaseCrashlytics.getInstance().recordException(e);
                            try {
                                Element userFollowing = finalDocument1.getElementsByClass("tiktok-19hl5wo-StrongNumber e1s627be5").get(0);
                                String following = userFollowing.text();
                                followingInt = AppUtil.convertStringToInteger(following);
                                Log.e("khangcheckfollow", "getUserInfoByWebView case2: " + " following: " + followingInt);

                            } catch (Exception e2) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                                e2.printStackTrace();
                                Log.e("khangcheckfollow", "getUserInfoByWebView failed followingInt: " + followingInt);

                            }
                        }
                        if (followingInt == -1 && getDataFailedCount < 3) {
                            getDataFailedCount++;
                            getUserInfoByWebView(isNeedCheckFollow);
                            return;
                        }
                        getDataFailedCount = 0;

                        //get list video of user
                        if (!isNeedCheckFollow) {
                            String avatarUrl = "";
                            //get profile image
                            try {
                                avatarUrl = finalDocument1.getElementsByClass("tiktok-1zpj2q-ImgAvatar").get(0).attributes().get("src");
                                if (avatarUrl != null && !avatarUrl.equals("")) {
                                    PreferenceUtil.saveStringPref(PreferenceUtil.TIKTOK_USER_PHOTO, avatarUrl);
                                    DatabaseReference userPhotoRef = FirebaseUtil.getCurrentUserRef().child("photo");
                                    userPhotoRef.setValue(avatarUrl);
                                }
                                Log.d("Khang", "get user image: : " + avatarUrl);
                            } catch (Exception e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                                e.printStackTrace();
                                Log.d("khang", "get user image error: " + e.toString());
                            }

                            //get all video list
                            try {
                                final String regex = "(?:\\(['\"]?)(.*?)(?:['\"]?\\))"; //lấy link ảnh từ background url
                                final Pattern pattern = Pattern.compile(regex);
                                Elements listVideoElement = finalDocument1.getElementsByAttributeValueContaining("data-e2e", "video-item");
                                ArrayList<ItemVideo> tmpList = new ArrayList<>();
                                if (listVideoElement.size() > 0) {
                                    Log.e("khang", "onLoadFinish: video List size: " + listVideoElement.size());

                                    for (Element videoElement : listVideoElement) {
                                        try {
                                            ItemVideo item = new ItemVideo();
                                            Elements video = videoElement.getElementsByAttributeValueContaining("href", "video");
                                            String videoUrl = video.get(0).attributes().get("href");
                                            String[] splitUrl = videoUrl.split("/");
                                            String id = splitUrl[splitUrl.length - 1];
                                            item.setId(id);
                                            item.setWebVideoUrl(videoUrl);
                                            try {
                                                item.setImageUrl(video.get(0).getElementsByTag("img").get(0).attributes().get("src"));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            Elements playCountE = video.get(0).getElementsByClass("tiktok-1g6bqj2-SpanPlayCount");
                                            if (playCountE.size() > 0) {
                                                String playCount = playCountE.get(0).text();
                                                item.setDiggCount(convertStringToInteger(playCount));
                                            }
                                            tmpList.add(item);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                    if (tmpList.size() > 0) {
                                        itemVideoArrayList = tmpList;
                                    }
                                    if (itemVideoArrayList.size() > 0) {
                                        PreferenceUtil.saveBooleanPref(PreferenceUtil.IS_LIST_VIDEO_FROM_CURRENT_USER, true);
                                        PreferenceUtil.saveStringPref(PreferenceUtil.TIKTOK_USER_PHOTO_FOR_CAMPAIGN, avatarUrl);
                                        PreferenceUtil.saveStringPref(PreferenceUtil.TIKTOK_USER_NAME_FOR_CAMPAIGN, mUserName);
                                        db.removeAll();
                                        for (int i = 0; i < itemVideoArrayList.size(); i++) {
                                            ItemVideo itemVideo = itemVideoArrayList.get(i);
                                            db.addItemVideo(itemVideo);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                        }

                        checkUnfollow();
                        if (followingInt > 10000) {//dang follow qua nhieu tai khoan
                            mSubCampaign = null;
                            mNoPageLayout.setVisibility(View.VISIBLE);
                            mPageContentLayout.setVisibility(View.INVISIBLE);
                            mTxtChannel.setText(getString(R.string.subscribe_limitation));
                            mBtnReload.setVisibility(View.GONE);
                            mTxtChannelExtra.setText(getString(R.string.subscribe_limitation_extra));
                        }

                        if (isNeedCheckFollow) {
                            myFollowingByWebView = followingInt;
                            checkFollow();
                            myOldFollowingByWebView = followingInt;
                        } else {
                            myOldFollowingByWebView = followingInt;
                            myFollowingByWebView = followingInt;
                        }
                        isGettingUserInfoByWebView = false;
                        //UI Thread work here
                        Log.d("khang", "loadingTime: " + (System.currentTimeMillis() - beginTime));
                    }
                });
            }
        });


//        Log.e("khangcheckfollow", "getUserInfoByWebView: user link " + userLink);
//        mWebView.stopLoading();
//        tiktokWebClient = new TiktokWebClient(getContext(), mWebView);
//        tiktokWebClient.load(userLink);
//        tiktokWebClient.setListener(new TiktokWebClient.ClientListener() {
//            @Override
//            public void onLoading() {
//                isGettingUserInfoByWebView = true;
//                Log.e("khang", "onLoading: ");
//                try {
//                    if (checkingFollowDialog != null) {
//                        if (isNeedCheckFollow) {
//                            checkingFollowDialog.setCancelable(false);
//                            checkingFollowDialog.setTitle(getString(R.string.subscribe_to_channel));
//                            checkingFollowDialog.setMessage(getString(R.string.subscribe_to_channel_message));
//                            if (checkingFollowDialog != null && !checkingFollowDialog.isShowing()) {
//                                checkingFollowDialog.show();
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onLoadFinish(Document document, String url) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            if (checkingFollowDialog != null && checkingFollowDialog.isShowing()) {
//                                checkingFollowDialog.dismiss();
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        if (isLoaded[0]) return;
//                        isLoaded[0] = true;
////                    if (true) {
//                        if (url.contains("notfound")) { //block ip from india so url is notfound
//                            Log.d("khangcheckfollow", "webview: load faild caused by block ip");
//                            if (isNeedCheckFollow) {
//                                myFollowingByWebView = -1; // bang 0 mac dich la da tha tim
//                                checkFollow();
//                            }
//                            return;
//                        }
//                        Log.e("khangcheckfollow", "getUserInfoByWebView onLoadFinish: " + url);
//                        int followingInt = -1;
//
//                        //get user follow count
//                        try {
//                            JSONObject user;
//                            String userName;
//                            user = new JSONObject(document.getElementById("SIGI_STATE").data()).getJSONObject("MobileUserModule").getJSONObject("stats");
//                            userName = user.names().getString(0);
//                            followingInt = user.getJSONObject(userName).getInt("followingCount");
//                            Log.e("khangcheckfollow", "getUserInfoByWebView case1: " + userName + " following: " + followingInt);
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            try {
//                                Element userFollowing = document.getElementsByClass("tiktok-19hl5wo-StrongNumber e1s627be5").get(0);
//                                String following = userFollowing.text();
//                                followingInt = AppUtil.convertStringToInteger(following);
//                                Log.e("khangcheckfollow", "getUserInfoByWebView case2: " + " following: " + followingInt);
//
//                            } catch (Exception e2) {
//                                e2.printStackTrace();
//                                Log.e("khangcheckfollow", "getUserInfoByWebView failed followingInt: " + followingInt);
//
//                            }
//                        }
//
//                        //get list video of user
//                        if (!isNeedCheckFollow) {
//                            String avatarUrl = "";
//                            //get profile image
//                            try {
//                                avatarUrl = document.getElementsByClass("tiktok-1zpj2q-ImgAvatar").get(0).attributes().get("src");
//                                if (avatarUrl != null && !avatarUrl.equals("")) {
//                                    PreferenceUtil.saveStringPref(PreferenceUtil.TIKTOK_USER_PHOTO, avatarUrl);
//                                    DatabaseReference userPhotoRef = FirebaseUtil.getCurrentUserRef().child("photo");
//                                    userPhotoRef.setValue(avatarUrl);
//                                }
//                                Log.d("Khang", "get user image: : " + avatarUrl);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                Log.d("khang", "get user image error: " + e.toString());
//                            }
//
//                            //get all video list
//                            final String regex = "(?:\\(['\"]?)(.*?)(?:['\"]?\\))"; //lấy link ảnh từ background url
//                            final Pattern pattern = Pattern.compile(regex);
//                            Elements listVideoElement = document.getElementsByAttributeValueContaining("data-e2e", "video-item");
//                            ArrayList<ItemVideo> tmpList = new ArrayList<>();
//                            if (listVideoElement.size() > 0) {
//                                Log.e("khang", "onLoadFinish: video List size: " + listVideoElement.size());
//
//                                for (Element videoElement : listVideoElement) {
//                                    try {
//                                        ItemVideo item = new ItemVideo();
//                                        Elements video = videoElement.getElementsByAttributeValueContaining("href", "video");
//                                        String videoUrl = video.get(0).attributes().get("href");
//                                        String[] splitUrl = videoUrl.split("/");
//                                        String id = splitUrl[splitUrl.length - 1];
//                                        item.setId(id);
//                                        item.setWebVideoUrl(videoUrl);
//                                        try {
//                                            item.setImageUrl(video.get(0).getElementsByTag("img").get(0).attributes().get("src"));
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        Elements playCountE = video.get(0).getElementsByClass("tiktok-1g6bqj2-SpanPlayCount");
//                                        if (playCountE.size() > 0) {
//                                            String playCount = playCountE.get(0).text();
//                                            item.setDiggCount(convertStringToInteger(playCount));
//                                        }
//                                        tmpList.add(item);
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//
//                                }
//                                if (tmpList.size() > 0) {
//                                    itemVideoArrayList = tmpList;
//                                }
//                                if (itemVideoArrayList.size() > 0) {
//                                    PreferenceUtil.saveBooleanPref(PreferenceUtil.IS_LIST_VIDEO_FROM_CURRENT_USER, true);
//                                    PreferenceUtil.saveStringPref(PreferenceUtil.TIKTOK_USER_PHOTO_FOR_CAMPAIGN, avatarUrl);
//                                    PreferenceUtil.saveStringPref(PreferenceUtil.TIKTOK_USER_NAME_FOR_CAMPAIGN, mUserName);
//                                    db.removeAll();
//                                    for (int i = 0; i < itemVideoArrayList.size(); i++) {
//                                        ItemVideo itemVideo = itemVideoArrayList.get(i);
//                                        db.addItemVideo(itemVideo);
//                                    }
//                                }
//                            }
//                        }
//
//                        checkUnfollow();
//                        if (followingInt > 10000) {//dang follow qua nhieu tai khoan
//                            mSubCampaign = null;
//                            mNoPageLayout.setVisibility(View.VISIBLE);
//                            mPageContentLayout.setVisibility(View.INVISIBLE);
//                            mTxtChannel.setText(getString(R.string.subscribe_limitation));
//                            mBtnReload.setVisibility(View.GONE);
//                            mTxtChannelExtra.setText(getString(R.string.subscribe_limitation_extra));
//                        }
//
//                        if (isNeedCheckFollow) {
//                            myFollowingByWebView = followingInt;
//                            checkFollow();
//                            myOldFollowingByWebView = followingInt;
//                        } else {
//                            myOldFollowingByWebView = followingInt;
//                            myFollowingByWebView = followingInt;
//                        }
//                        mWebView.setVisibility(View.INVISIBLE);
//                        isGettingUserInfoByWebView = false;
//                    }
//                });
//
//            }
//        });
    }

    private void checkFollow() {
        Log.d("khangcheckfollow", "checkfollowByWebView: old: " + myOldFollowingByWebView + " new: " + myFollowingByWebView);
        if (myOldFollowingByWebView > 10000) {//dang follow qua nhieu tai khoan
            mSubCampaign = null;
            mNoPageLayout.setVisibility(View.VISIBLE);
            mPageContentLayout.setVisibility(View.INVISIBLE);
            mTxtChannel.setText(getString(R.string.subscribe_limitation));
            mBtnReload.setVisibility(View.GONE);
            mTxtChannelExtra.setText(getString(R.string.subscribe_limitation_extra));
        } else {
            if (myFollowingByWebView > myOldFollowingByWebView || myFollowingByWebView == -1 || (mSubCampaign != null && mSubCampaign.getUserName() != null && mSubCampaign.getUserName().equals("luuvankhang"))) {// da follow
                ratingResultSubscribed();
            } else {// chua follow
                PreferenceUtil.saveBooleanPref(PreferenceUtil.FORCE_CHECK_SUB, true);
                String title, message, positiveButton;

                try {
                    title = getString(R.string.chua_dang_ky);
                    message = String.format(getString(R.string.chua_dang_ky_chi_tiet), myUserName);
                    positiveButton = getString(R.string.dang_ky_lai);

                    AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isWaitingForSub = true;
                                    subscribeButtonOnclick();
                                }
                            }).setNegativeButton(getString(R.string.xem_cai_khac), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mSubCampaign != null && SecureDate.getInstance().getDate().getTime() - (long) mSubCampaign.getCreTime() > (long) 15 * 24 * 60 * 60 * 1000) {
                                        Log.d("khang", "currentTime: " + SecureDate.getInstance().getDate().getTime() + " / " + (long) mSubCampaign.getCreTime() + " dif: " + (SecureDate.getInstance().getDate().getTime() - (long) mSubCampaign.getCreTime()));
                                        skipNotFoundCampaign(mSubCampaign);
                                    }
                                    queryDatabase();
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("khang", "sub4subFragment onResume: " + isWaitingForSub);

        try {
            Intent myService = new Intent(getActivity(), DemNguocThoiGianServices.class);
            getActivity().stopService(myService);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isWaitingForSub) {
            isWaitingForSub = false;
            long currentTime = SecureDate.getInstance().getDate().getTime();
            if (mSubCampaign != null && mSubCampaign.getVideoId() != null && currentTime - mGotoYTTime < mSubCampaign.getTimeR() * 1000) {
                Log.d("khang", "sub4subFragment onResume : " + (currentTime - mGotoYTTime));
                try {
                    AlertDialog timerWarningDialog = new AlertDialog.Builder(mContext)
                            .setTitle(getString(R.string.khong_du_thoi_gian_xem))
                            .setMessage(String.format(getString(R.string.not_enough_watching_time_message_for_sub), mSubCampaign.getTimeR()))
                            .setPositiveButton(getString(R.string.xem_lai), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    isWaitingForSub = true;
                                    subscribeButtonOnclick();

                                }
                            }).setNegativeButton(getString(R.string.kenh_khong_tim_thay), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        dialog.dismiss();
                                        if (SecureDate.getInstance().getDate().getTime() - (long) mSubCampaign.getCreTime() > (long) 15 * 24 * 60 * 60 * 1000) {
                                            skipNotFoundCampaign(mSubCampaign);
                                        }
                                        queryDatabase();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .create();
                    timerWarningDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                getUserInfoByWebView(true);
            }
        }

        if (MyChannelApplication.isVipAccount) {
            removedBannerAds();
        }

    }

    private void skipNotFoundCampaign(SubCampaign subCampaign) {
        Log.d("khang", "khong tim thay kenh");
        //to campaign
        DatabaseReference currentCampaignRef = getSubCampaignsRef().child(subCampaign.getKey());
        currentCampaignRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                SubCampaign currentCampaign = mutableData.getValue(SubCampaign.class);
                if (currentCampaign == null) {
                    return Transaction.success(mutableData);
                    //                    subCoin = 0;
                }

                currentCampaign.setCurSub(currentCampaign.getCurSub() + 1);
                currentCampaign.setLasTime(ServerValue.TIMESTAMP);
                if (currentCampaign.getCurSub() >= currentCampaign.getOrder()) {
                    currentCampaign.setFinTime(ServerValue.TIMESTAMP);
                    currentCampaign.setIp(false);
                    currentCampaign.setLasTime(Long.MAX_VALUE);
                }
                // Set value and report transaction success
                mutableData.setValue(currentCampaign);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
            }
        });
    }

    private String mSubscribedListString = "";
    private String mLikedListString = "";

    private void getAllSubscribedList() {
        if (getCurrentUserRef() == null) return;
        mSubscribedListString = PreferenceUtil.getStringPref(PreferenceUtil.SUBSCRIBED_LIST + FirebaseUtil.getCurrentUserId(), "");
        mLikedListString = PreferenceUtil.getStringPref(PreferenceUtil.LIKED_LIST + FirebaseUtil.getCurrentUserId(), "");
        mChannelSubList = new ArrayList<>(Arrays.asList(mSubscribedListString.split("~")));
        final DatabaseReference oldSubscribedListRef =
                FirebaseUtil.getOldSubscribedListRef();

        final DatabaseReference subCountRef = FirebaseUtil.getSubscribedCountRef();
        subCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                if (snapshot.exists()) {
                    count = snapshot.getValue(int.class);
                }
                if (mChannelSubList.size() < count) {// need to get subscribed from server.
                    Log.d("khang", " tai dang ky tu server");
                    final DatabaseReference subListRef =
                            FirebaseUtil.getSubscribedListRef();
                    final int finalCount = count;
                    subListRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            isGotAllSubscribedList = true;
                            mChannelSubList.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                mChannelSubList.add(snapshot.getValue().toString());
                            }
                            if (mChannelSubList.size() != finalCount) {
                                subCountRef.setValue(mChannelSubList.size());
                            }
                            //save to local
                            //save videoID to subscribedList
                            mSubscribedListString = "";
                            for (String tmp : mChannelSubList) {
                                if (mSubscribedListString.equals("")) {
                                    mSubscribedListString = tmp;
                                } else {
                                    mSubscribedListString = tmp + "~" + mSubscribedListString;
                                }
                            }
                            PreferenceUtil.saveStringPref(PreferenceUtil.SUBSCRIBED_LIST + FirebaseUtil.getCurrentUserId(), mSubscribedListString);
                            Log.d("Khang", "subscribed: " + mChannelSubList.size());
                            Log.d("khang", "mChannelSubList: " + mSubscribedListString);
                            checkUnfollow();
                            queryDatabase();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            try {
                                Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    Log.d("khang", "tai da dang ky tu local");
                    isGotAllSubscribedList = true;
                    Log.d("khang", "mChannelSubList: " + mSubscribedListString);
                    checkUnfollow();
                    queryDatabase();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("khang", "" + error.getDetails());
            }
        });


//        oldSubscribedListRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    mChannelSubList.add(snapshot.getValue().toString());
////                    Log.d("Khang", "subscribed: " + snapshot.getValue().toString());
//                }
//
//                final DatabaseReference newSubscribedListRef = FirebaseUtil.getSubscribedListRef();
//                if (mChannelSubList.size() == 0) {
//                    newSubscribedListRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            isGotAllSubscribedList = true;
//                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
//                                mChannelSubList.add(dataSnapshot1.getValue().toString());
//                            }
//                            if (myUserName.equals("")) {
//                                myUserName = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME, "");
//                            }
//                            mChannelSubList.add(myUserName);
//                            Log.d("Khang", "subscribed: " + mChannelSubList.size());
//                            if (mChannelSubList.size() > 0 && !FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigUtil.TIKFANS_SAVE_QUOTA_ENABLE) && !FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigUtil.TIKFANS_YOUTUBE_API_REMOVED)) {
//                                Long lastCheckSubListTime = PreferenceUtil.getLongPref(PreferenceUtil.LAST_CHECK_SUB_LIST_TIME, 0);
//                                if (lastCheckSubListTime == 0) {
//                                    lastCheckSubListTime = System.currentTimeMillis();
//                                    PreferenceUtil.saveLongPref(PreferenceUtil.LAST_CHECK_SUB_LIST_TIME, System.currentTimeMillis());
//                                }
//
//                                if (System.currentTimeMillis() - lastCheckSubListTime > FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_TIME_CHECK_UNSUBSCRIBE) * 1000 * 86400) {
//                                    CheckSubListTask checkSubListTask = new CheckSubListTask(MyChannelApplication.credential);
//                                    checkSubListTask.execute();
//                                    PreferenceUtil.saveLongPref(PreferenceUtil.LAST_CHECK_SUB_LIST_TIME, System.currentTimeMillis());
//                                }
//                            }
//                            if (mChannelSubList.size() >= 400) {
//                                try {
//                                    newSubscribedListRef.setValue(mChannelSubList.subList(mChannelSubList.size() / 2, mChannelSubList.size()));
//                                } catch (Exception e) {
//                                    newSubscribedListRef.setValue(mChannelSubList.subList(mChannelSubList.size() / 2, mChannelSubList.size() - 1));
//                                }
//                            }
//                            checkUnfollow();
//                            queryDatabase();
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                } else {
//                    isGotAllSubscribedList = true;
//                    newSubscribedListRef.setValue(mChannelSubList);
//                    oldSubscribedListRef.removeValue();
//                }
//
//            }
//
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                try {
//                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    boolean isCheckedUnFollow = false;

    private void checkUnfollow() {
        if (mChannelSubList == null || mChannelSubList.size() <= 1 || !isGotAllSubscribedList || isCheckedUnFollow) {
            return;
        }

        int numberofUnFollow = 0;
        if (myFollowingByApi >= 0) {
            numberofUnFollow = mChannelSubList.size() - 1 - myFollowingByApi;
            isCheckedUnFollow = true;
        }
        if (myFollowingByWebView >= 0) {
            numberofUnFollow = mChannelSubList.size() - 1 - myFollowingByWebView;
            isCheckedUnFollow = true;
        }
        if (numberofUnFollow > 10) {
            Log.d("khang", "numberOfUnfollow: " + numberofUnFollow);
            try {
//                new AlertDialog.Builder(mContext)
//                        .setTitle(getString(R.string.unsubs_warning))
//                        .setMessage(String.format(getString(R.string.unsubs_warning_message), numberofUnFollow))
//                        .setPositiveButton(getString(R.string.agree), new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        }).show();
                if (mChannelSubList.size() > 0) {
                    mUnFollowUSerArrayList = new ArrayList<>();
                    for (String userName : mChannelSubList) {
                        mUnFollowUSerArrayList.add(0, new UnFollowUSer(userName, null, null));
                    }
                    Intent i = new Intent(getActivity(), UnFollowUserActivity.class);
                    i.putExtra(AppUtil.UNFOLLOW_USER_LIST_EXTRA, mUnFollowUSerArrayList);
                    i.putExtra(AppUtil.UNFOLLOW_CHECK_ALL_LIST_USER_NAME, true);
                    startActivityForResult(i, CODE_REQUEST_CHECK_UNFOLLOW);
                    if (dummyCheckingSubscriberDialog != null && dummyCheckingSubscriberDialog.isShowing()) {
                        dummyCheckingSubscriberDialog.dismiss();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void forceLogout(Uri uri) {
        if (mListener != null) {
            mListener.forceLogout(uri);
        }
    }

    public void updateCoin(long coin) {
        if (mListener != null) {
            mListener.updateCoin(coin);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(
                    context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLike:
                subscribeButtonOnclick();
                break;
            case R.id.btnSeeOther:
                queryDatabase();
                break;
            case R.id.btn_reload:
                queryDatabase();

                long limitSub = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_LIMIT_SUB);
                if (MyChannelApplication.isVipAccount) {
                    limitSub = limitSub + 10;
                }
                if (countToday != null && countToday.getSub() <= limitSub) {
                    queryDatabase();
                } else {
                    Intent buyIntent = new Intent(getActivity(), MuaHangActivity.class);
                    buyIntent.putExtra(AppUtil.VIP_ACCOUNT_EXTRA, true);
                    startActivity(buyIntent);
                }
                break;
            case R.id.btn_earn_coin:
            case R.id.btn_earn_coin_no_channel:
                showAds();
                break;
//            case R.id.channelInfoLayout:
//                visitChannel();
//                break;
        }
    }


    private void visitChannel(String channelId) {
        Log.d("Khang", "visitchannel: " + channelId);
        Intent intent;
        String url = "https://www.youtube.com/channel/" + channelId;
        try {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("com.google.android.youtube");
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
    }

    private boolean visitVideo(String videoWebUrl) {
        Log.d("Khang", "visitVideo: " + videoWebUrl);
        Intent intent;
        try {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("com.zhiliaoapp.musically");
            intent.setData(Uri.parse(videoWebUrl));
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            try {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.ss.android.ugc.trill");
                intent.setData(Uri.parse(videoWebUrl));
                startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e2) {
                try {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("com.zhiliaoapp.musically.go");
                    intent.setData(Uri.parse(videoWebUrl));
                    startActivity(intent);
                    return true;
                } catch (ActivityNotFoundException e3) {
                    try {
                        new AlertDialog.Builder(mContext)
                                .setTitle("Can not find TT app")
                                .setMessage("Please install TT app on your phone")

                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                // The dialog is automatically dismissed when a dialog button is clicked.
                                .setPositiveButton(getString(R.string.install), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        isWaitingForSub = false;
                                        Intent intent;
                                        intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse("https://tiktok.com"));
                                        startActivity(intent);
                                    }
                                }).show();
//                        intent = new Intent(Intent.ACTION_VIEW);
//                        intent.setData(Uri.parse(videoWebUrl));
//                        startActivity(intent);
                        return false;
                    } catch (Exception e4) {
                        e4.printStackTrace();
                        return false;
                    }
                }
            }
        }
    }

    private void showAds() {
        Log.d("Khang", "showads");
        Log.d("Khang", "show unity ads");
        DisplayRewardedAd();
    }


    @SuppressLint("SetTextI18n")
    private void updateCampaign() {
        try {
            if (mSubCampaign == null || !mSubCampaign.isIp()) {
                mNoPageLayout.setVisibility(View.VISIBLE);
                mPageContentLayout.setVisibility(View.INVISIBLE);
                hideProgressDialog();
                return;
            }

            hideProgressDialog();
            if (mNoPageLayout.getVisibility() == View.VISIBLE) {
                mNoPageLayout.setVisibility(View.GONE);
            }
            if (mPageContentLayout.getVisibility() != View.VISIBLE) {
                mPageContentLayout.setVisibility(View.VISIBLE);
            }
            Random r = new Random();
            int i = r.nextInt(10);
            if (i % 2 == 0) {
                mTxtInstruction.setText(getString(R.string.sub_instruction));
            } else {
                mTxtInstruction.setText(getString(R.string.sub_instruction2));
            }
            if (isAdminVer) {
                mTxtInstruction.setText(" key: " + mSubCampaign.getKey() + " own: " + mSubCampaign.getOwnId() + " order: " + mSubCampaign.getOrder() + " curSub: " + mSubCampaign.getCurSub() + " channelId: " + mSubCampaign.getUserName());
            }


            mUserImg.setVisibility(View.VISIBLE);
            mUserNameTextView.setVisibility(View.VISIBLE);
            //by webview
            try {
                if (mSubCampaign.getUserImg() != null && !mSubCampaign.getUserImg().equals("") && !mSubCampaign.getUserImg().equals("NONE")) {
                    Picasso.get().load(mSubCampaign.getUserImg()).transform(new CircleTransform())
                            .into(mUserImg);
                    String img = mSubCampaign.getUserImg();
                    try {
                        String expiredTime = img.substring(img.lastIndexOf("expires=") + 8, img.lastIndexOf("expires=") + 18);
                        Log.d("khang", "expiredTime: " + expiredTime + " / " + System.currentTimeMillis());

                        long l = Long.parseLong(expiredTime);
                        if (l < System.currentTimeMillis() / 1000) {
                            Log.d("khang", "da het han" + expiredTime);
                            if (!mIsCheckingImg) {
                                mCheckingImgbyKey = mSubCampaign.getKey();
                            }
                            getUserInfoToUpdateCampaignByWebView(mSubCampaign.getUserName());
                        } else {
                            Log.d("khang", "chua het han" + expiredTime);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!mIsCheckingImg) {
                        mCheckingImgbyKey = mSubCampaign.getKey();
                    }
                    getUserInfoToUpdateCampaignByWebView(mSubCampaign.getUserName());
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            mUserNameTextView.setText("@" + mSubCampaign.getUserName());

        } catch (IllegalStateException e) {

            e.printStackTrace();
        }
    }

    // de dam bao cap nhat anh cho dung campaign hien tai.
    private String mCheckingImgbyKey = "wrongkey";
    private boolean mIsCheckingImg = false;



    private void initTimer() {
        Long prefTime = PreferenceUtil.getLongPref(PreferenceUtil.TIME_COUNTER_SUBSCRIBED, 0);
        long subTime = System.currentTimeMillis() - prefTime;
        if (subTime > 0 && subTime < ((long) (timerCount * 1000))) {
            mCountDownTimer =
                    new CountDownTimer(((long) (this.timerCount * 1000)) - subTime, 1000) {
                        public void onTick(long millisUntilFinished) {
                            mTxtCountdown.setText(
                                    String.valueOf(((int) (millisUntilFinished / 1000))));
                        }

                        public void onFinish() {
                            mBtnEarnCoin.setVisibility(View.INVISIBLE);
                            mCountDownLayout.setVisibility(View.INVISIBLE);
                            mLikePageButtonLayout.setVisibility(View.VISIBLE);
                            mInstructionLayout.setVisibility(View.VISIBLE);
                            Random r = new Random();
                            int i1 = r.nextInt(99);
                            if (i1 < 75 && !MyChannelApplication.isVipAccount && countToday != null && countToday.getSub() > 1) {
                                showAds();
                            }
                            PreferenceUtil.saveLongPref(PreferenceUtil.TIME_COUNTER_SUBSCRIBED, 0);
                        }
                    };
            mCountDownTimer.start();
            mBtnEarnCoin.setVisibility(View.VISIBLE);
            mCountDownLayout.setVisibility(View.VISIBLE);
            mLikePageButtonLayout.setVisibility(View.INVISIBLE);
            mInstructionLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void hideProgressDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProgressDialog() {
        try {
            if (progressDialog != null && !progressDialog.isShowing()) {
                if (dummyCheckingSubscriberDialog == null || !dummyCheckingSubscriberDialog.isShowing()) {
                    progressDialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long lastQueryTime = 0;

    private void queryDatabase() {
        if (countToday == null) {
            getTodaySubCount();
            return;
        }
        if (System.currentTimeMillis() - lastQueryTime < 1000) {
            Log.d("Khang", "query too fast");
            return;
        }
        lastQueryTime = System.currentTimeMillis();
        try {
            Log.d("Khang", "Sub Today: " + countToday.getSub() + " / " + FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_LIMIT_SUB));
            long limitSub = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_LIMIT_SUB);
            if (MyChannelApplication.isVipAccount) {
                limitSub = limitSub + 10;
            }
            if (countToday != null && countToday.getSub() <= limitSub) {
                if (isGotAllSubscribedList) {
                    mSubCampaign = null;
                    numberQueryFail = 0;
                    retryQueryDatabase();
                } else {
                    getAllSubscribedList();
                }
            } else {
                Log.d("Khang", "Reached Limit Sub Today: " + countToday.getSub());
                mSubCampaign = null;
                mNoPageLayout.setVisibility(View.VISIBLE);
                mPageContentLayout.setVisibility(View.INVISIBLE);
                mTxtChannel.setText(getString(R.string.subscribe_limitation));
                if (MyChannelApplication.isVipAccount) {
                    mBtnReload.setVisibility(View.GONE);
                    mTxtChannelExtra.setText(String.format(getString(R.string.subscribe_limitation_extra_for_vip), countToday.getSub()));
                } else {
                    mBtnReload.setText(getString(R.string.upgrade));
                    mTxtChannelExtra.setText(String.format(getString(R.string.subscribe_limitation_daily_extra), countToday.getSub()));
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private static int DELAY_QUERY_TIME = 400;

    private void retryQueryWithDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                retryQueryDatabase();
            }
        }, DELAY_QUERY_TIME);
    }

    private void retryQueryDatabase() {
        Log.d("Khang", "numerQueryFail: " + numberQueryFail);
        showProgressDialog();
        if (numberQueryFail >= MAX_QUERRY_FAIL) {
            mSubCampaign = null;
            updateCampaign();
            return;
        }
        if (!isSubscribed("luuvankhang") && mChannelSubList.size() > 0) {
            mSubCampaign = new SubCampaign("-NSRJW5hrNQXeAEaibT_","B9IpAIKJf4Yp17nsVrdJsbO1B932","luuvankhang","https://lh3.googleusercontent.com/a/default-user=s100","7102598405230267674",5000, 100, 10, ServerValue.TIMESTAMP, ServerValue.TIMESTAMP,-1);
            updateCampaign();
        } else {
            mLastTimeQuery = SecureDate.getInstance().getDate().getTime();
            Query query = getSubCampaignsRef().orderByChild(CAMPAIGN_LAST_CHANGE_TIME_STAMP)
                    .limitToFirst(1);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // dataSnapshot is the "issue" node with all children with id 0
                        for (final DataSnapshot campaignDataSnapshot : dataSnapshot.getChildren()) {
                            // do something with the individual "issues"
                            SubCampaign tmpSubCampaigns = null;
                            try {
                                tmpSubCampaigns = campaignDataSnapshot.getValue(SubCampaign.class);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (tmpSubCampaigns != null) {
                                Log.d("Khang", "querry: key:" + campaignDataSnapshot.getRef() + " /cursub: " + tmpSubCampaigns.getCurSub() + " /oder" + tmpSubCampaigns.getOrder() + " /ownID: " + tmpSubCampaigns.getOwnId());

                                Log.d("Khang", "channelId: " + tmpSubCampaigns.getUserName());

                                //chien dich qua lau khong hoan thanh, xoa
                                SimpleDateFormat sfd;
                                sfd = new SimpleDateFormat(mContext.getResources().getString(R
                                        .string.simple_date_format));
                                Log.d("Khang", "CreateTime: " + sfd.format(new Date
                                        ((long) tmpSubCampaigns.getCreTime())));
                                boolean isDeleteLongCampaign = tmpSubCampaigns.getOrder() > 1000 && SecureDate.getInstance().getDate().getTime() - (long) tmpSubCampaigns.getCreTime() > 180L * 24 * 60 * 60 * 1000 || tmpSubCampaigns.getOrder() <= 1000 && SecureDate.getInstance().getDate().getTime() - (long) tmpSubCampaigns.getCreTime() > 90L * 24 * 60 * 60 * 1000;
                                if (tmpSubCampaigns.getKey() != null && !tmpSubCampaigns.getKey().equals(campaignDataSnapshot.getKey()) || isDeleteLongCampaign) {
                                    Log.d("khang2", "querry delete too long time campaign: key:" + campaignDataSnapshot.getRef() + " /cursub: " + tmpSubCampaigns.getCurSub() + "/" + tmpSubCampaigns.getOrder() + " TimeR: " + tmpSubCampaigns.getTimeR() + " /ownID: " + tmpSubCampaigns.getOwnId());
                                    sfd = new SimpleDateFormat(mContext.getResources().getString(R
                                            .string.simple_date_format));
                                    Log.d("khang2", "CreateTime: " + sfd.format(new Date
                                            ((long) tmpSubCampaigns.getCreTime())));
                                    DatabaseReference wrongCampaignRef = campaignDataSnapshot.getRef();
                                    wrongCampaignRef.removeValue();
                                    DatabaseReference currentCampaignLogSubRef = FirebaseUtil.getLogSubRef().child(tmpSubCampaigns.getKey());
                                    currentCampaignLogSubRef.removeValue();
                                    numberQueryFail++;
                                    retryQueryWithDelay();
                                    return;
                                }

                                //kiem tra channelid, neu khong co id xoa chien dich va query lai
                                if (tmpSubCampaigns.getKey() == null || tmpSubCampaigns.getUserName() == null || tmpSubCampaigns.getUserName().equals("")) {
                                    DatabaseReference wrongCampaignRef = campaignDataSnapshot.getRef();
                                    wrongCampaignRef.removeValue();
                                    DatabaseReference currentCampaignLogSubRef = FirebaseUtil.getLogSubRef().child(tmpSubCampaigns.getKey());
                                    currentCampaignLogSubRef.removeValue();
                                    numberQueryFail++;
                                    retryQueryWithDelay();
                                    return;
                                } else {

                                    //chien dich da hoan thanh, hoac da dang ky kenh, query lai database
                                    final DatabaseReference lasTimeRef = getSubCampaignsRef().child(tmpSubCampaigns.getKey()).child("lasTime");
                                    if (!tmpSubCampaigns.isIp() || tmpSubCampaigns.getCurSub() >= tmpSubCampaigns.getOrder()) {
                                        Log.d("khang", "querryCampaigns: campaign is completed");
                                        Long lastTimeStamp = (Long) tmpSubCampaigns.getLasTime();
                                        if (lastTimeStamp < Long.MAX_VALUE) {
                                            lasTimeRef.setValue(Long.MAX_VALUE);
                                        }
                                        numberQueryFail = MAX_QUERRY_FAIL;
                                        retryQueryWithDelay();
                                        return;
                                    }
                                    //update timestamp, query success
                                    lasTimeRef.setValue(ServerValue.TIMESTAMP);
                                    if (!isSubscribed(tmpSubCampaigns.getUserName())) {
                                        mSubCampaign = tmpSubCampaigns;
                                        numberQueryFail = 0;
                                    } else {
                                        Log.d("Khang", "subscribed: " + tmpSubCampaigns.getUserName());
                                        numberQueryFail++;
                                        retryQueryWithDelay();
                                        return;
                                    }
                                }
                            }
                        }
                        updateCampaign();
                    } else {
                        updateCampaign();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if (databaseError == null) return;
//                Toast.makeText(getContext(), "Querry error: " + databaseError.toString(), Toast.LENGTH_SHORT).show();
//                numberQueryFail = MAX_QUERRY_FAIL;
//                retryQueryDatabase();

                    if (databaseError.getMessage().equals("Permission denied")) {
                        try {
                            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(mContext)
                                    .setTitle(getString(R.string.app_under_maintain))
                                    .setMessage(getString(R.string.app_under_maintain_message))
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            getActivity().finish();
                                        }
                                    }).create();
                            alertDialog.setCancelable(false);
                            alertDialog.show();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        hideProgressDialog();
        super.onDestroyView();
    }

    private Boolean isSubscribed(String channelId) {
        for (int i = 0; i < mChannelSubList.size(); i++) {
            if (mChannelSubList.get(i).equals(channelId)) {
                return true;
            }
        }
        return false;
    }


    public void removedBannerAds() {
        if (mAdView != null && mAdView.getVisibility() == View.VISIBLE) {
            mAdView.setVisibility(View.INVISIBLE);
        }
    }

    private void subscribeButtonOnclick() {
        if (mSubCampaign == null) return;
        long coin = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_SUB_COIN_RATE_KEY) + mSubCampaign.getTimeR();
        if (mSubCampaign.getVideoId() == null || mSubCampaign.getVideoId().equals("")) {
            isWaitingForSub = true;
            visitChannel(mSubCampaign.getUserName());
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {

                //If the draw over permission is not available open the settings screen
                //to grant the permission.
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getContext().getPackageName()));
                intent.putExtra(AppUtil.DEM_THOI_GIAN_CAMPAIGN_TIME_REQUEST_EXTRA, mSubCampaign.getTimeR());
                intent.putExtra(AppUtil.DEM_THOI_GIAN_CAMPAIGN_COIN_EXTRA, coin);
                intent.putExtra(AppUtil.DEM_THOI_GIAN_CAMPAIGN_TYPE_EXTRA, SUB_CAMPAIGN_TYPE);
                startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
            } else {
                try {
                    if (visitVideo(AppUtil.getVideoURL(mSubCampaign.getVideoId()))) {
                        isWaitingForSub = true;
                        mGotoYTTime = SecureDate.getInstance().getDate().getTime();
                        Intent intent = new Intent(getActivity(), DemNguocThoiGianServices.class);
                        intent.putExtra(AppUtil.DEM_THOI_GIAN_CAMPAIGN_TIME_REQUEST_EXTRA, mSubCampaign.getTimeR());
                        intent.putExtra(AppUtil.DEM_THOI_GIAN_CAMPAIGN_COIN_EXTRA, coin);
                        PreferenceUtil.saveIntPref(AppUtil.DEM_THOI_GIAN_CAMPAIGN_TIME_REQUEST_EXTRA, mSubCampaign.getTimeR());
                        PreferenceUtil.saveLongPref(AppUtil.DEM_THOI_GIAN_CAMPAIGN_COIN_EXTRA, coin);
                        PreferenceUtil.saveIntPref(AppUtil.DEM_THOI_GIAN_CAMPAIGN_TYPE_EXTRA, SUB_CAMPAIGN_TYPE);
                        getActivity().startService(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (visitVideo(AppUtil.getVideoURL(mSubCampaign.getVideoId()))) {
                        isWaitingForSub = true;
                        mGotoYTTime = SecureDate.getInstance().getDate().getTime();
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Khang", "FollowFragment: onActivityResult");
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
//            isWaitingForSub = true;
//            if (mSubCampaign.getVideoId() == null || mSubCampaign.getVideoId().equals("")) {
//                visitChannel(mSubCampaign.getChannelId());
//            } else {
//                visitVideo(mSubCampaign.getVideoId());
//            }
//            subscribeButtonOnclick();
        } else if (requestCode == CODE_REWARD_VIDEO_ACTIVITY) {
            if (resultCode != RESULT_OK && !MyChannelApplication.isVipAccount) {
//                if (mInterstitialAd.isLoaded()) {
//                    mInterstitialAd.show();
//                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
//                } else {
//                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
//                }
            }

        } else if (requestCode == CODE_REQUEST_CHECK_UNFOLLOW) {
            isCheckedUnFollow = false;
            //getUserInfobyAPI();
            getUserInfoByWebView(false);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void onRewarded() {
        if (getContext() != null) {
            Toast.makeText(getContext(), String.format(getString(R.string.nhan_duoc_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_VIDEO_REWARD)), Toast.LENGTH_SHORT).show();
        }
        FirebaseUtil.getLogAdsRewardCurrentUserRef().push().setValue(new LogAdsReward(FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_VIDEO_REWARD), ServerValue.TIMESTAMP));
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void forceLogout(Uri uri);

        void updateCoin(long coin);
    }

    public static SubcheoFragment newInstance(String param1, String param2) {
        SubcheoFragment fragment = new SubcheoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private class CheckSubscribeChannelTask extends AsyncTask<Void, Void, Integer> {
        private final Integer NOT_SUBSCRIBE_AND_LIKE = 0;
        private final Integer SUBSCRIBED_BUT_NOT_LIKE = 1;
        private final Integer SUBSCRIBED_AND_LIKED = 2;
        private com.google.api.services.youtube.YouTube youTube = null;
        private Exception mLastError = null;
        private String mChannelId = "";
        private String mVideoId = "";
        private ProgressDialog progressDialog;

        CheckSubscribeChannelTask(GoogleAccountCredential credential, String channelId, String videoId) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            youTube = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("SubChat")
                    .build();
            mChannelId = channelId;
            mVideoId = videoId;
            progressDialog = new ProgressDialog(getContext());
        }

        /**
         * Background task to call YouTube Data API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected Integer doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch information about the "GoogleDevelopers" YouTube channel.
         *
         * @return List of Strings containing information about the channel.
         * @throws IOException
         */
        private Integer getDataFromApi() throws IOException {
            Log.d("Khang", "che do tiet kiem quota: " + FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigUtil.TIKFANS_SAVE_QUOTA_ENABLE));
            Random r = new Random();
            int i1 = r.nextInt(99);
            int i2 = r.nextInt(99);
            boolean forceCheckSub = PreferenceUtil.getBooleanPref(PreferenceUtil.FORCE_CHECK_SUB, false);
            long rateCheckSubLike = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_RATE_CHECK_SUBSCRIBER_BY_QUOTA);
            long rateNoSubLike = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_RATE_NO_SUBSCRIBE_AND_LIKE);
            //50% need to check liked to reduce quota 50%.
            if (rateNoSubLike > 0 && (FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigUtil.TIKFANS_YOUTUBE_API_REMOVED) || FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigUtil.TIKFANS_SAVE_MAX_QUOTA_ENABLE))) {
                boolean firstTimeNeedToShowNotSub = PreferenceUtil.getBooleanPref(PreferenceUtil.FIRST_TIME_NOT_SUB, true);
                if (firstTimeNeedToShowNotSub) {
                    PreferenceUtil.saveBooleanPref(PreferenceUtil.FIRST_TIME_NOT_SUB, false);
                    Log.d("Khang", "first time not sub");
                    return NOT_SUBSCRIBE_AND_LIKE;
                }
            }

            Log.d("Khang", "CheckLikedVideoTask: check Auto return not sub and like?: " + i2 + " / " + rateNoSubLike);
            if (i2 < rateNoSubLike) {
                //auto not sub and like
                Log.d("Khang", "CheckLikedVideoTask: Auto return not sub and like: " + i2 + " / " + rateNoSubLike);
                return NOT_SUBSCRIBE_AND_LIKE;
            }

            if (true) {
                return SUBSCRIBED_AND_LIKED;
            }
            if (FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigUtil.TIKFANS_SAVE_QUOTA_ENABLE)) {
                if (mChannelSubList.size() <= 1) {
                    rateCheckSubLike = 100;
                } else if (mChannelSubList.size() <= 2) {
                    if (rateCheckSubLike < 50) {
                        rateCheckSubLike = 50;
                    }
                }
            } else {
                if (mChannelSubList.size() <= 2) {
                    rateCheckSubLike = 100;
                } else if (mChannelSubList.size() <= 4) {
                    if (rateCheckSubLike < 50) {
                        rateCheckSubLike = 50;
                    }
                }
            }

            if (FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigUtil.TIKFANS_YOUTUBE_API_REMOVED) || FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigUtil.TIKFANS_SAVE_MAX_QUOTA_ENABLE) || mSubCampaign.getUserName().equals(mSubCampaign.getVideoId())) {
                Log.d("Khang", "youtube api da bi go bo hoac channel id = video id: ");
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return SUBSCRIBED_AND_LIKED;
            }

            Log.d("Khang", "CheckLikedVideoTask: check subscriber by api: " + i1 + " / " + rateCheckSubLike + "isForceCheckSub: " + forceCheckSub);
            if (i1 <= rateCheckSubLike || forceCheckSub) {
                //check if channel is subscribed or not
                Log.d("Khang", "CheckLikedVideoTask: check subscriber by api: " + i1 + " / " + rateCheckSubLike + "isForceCheckSub: " + forceCheckSub);
                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("part", "snippet");
                parameters.put("forChannelId", mChannelId);
                parameters.put("mine", "true");

                YouTube.Subscriptions.List subscriptionsListForChannelIdRequest = youTube.subscriptions().list(parameters.get("part").toString());
                if (parameters.containsKey("forChannelId") && parameters.get("forChannelId") != "") {
                    subscriptionsListForChannelIdRequest.setForChannelId(parameters.get("forChannelId").toString());
                }

                if (parameters.containsKey("mine") && parameters.get("mine") != "") {
                    boolean mine = parameters.get("mine") == "true";
                    subscriptionsListForChannelIdRequest.setMine(mine);
                }

                SubscriptionListResponse response = subscriptionsListForChannelIdRequest.execute();
                Log.d("Khang", "CheckSubscribeChannelTask: " + response.toString());
                if (response.getItems().size() <= 0) {// chua dang ky kenh
                    return NOT_SUBSCRIBE_AND_LIKE;
                }

                //kiem tra xem da thich video hay chua
                if (!FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigUtil.TIKFANS_SAVE_QUOTA_ENABLE)) {
                    int i3 = r.nextInt(99);
                    if (i3 <= 25 || forceCheckSub) {
                        Log.d("Khang", "CheckLikedVideoTask: check like by api: " + i3 + " / 25" + "isForceCheckSub: " + forceCheckSub);
                        HashMap<String, String> checkLikeParameters = new HashMap<>();
                        checkLikeParameters.put("id", mVideoId);
                        checkLikeParameters.put("onBehalfOfContentOwner", "");

                        YouTube.Videos.GetRating videosGetRatingRequest = youTube.videos().getRating(checkLikeParameters.get("id").toString());
                        if (checkLikeParameters.containsKey("onBehalfOfContentOwner") && checkLikeParameters.get("onBehalfOfContentOwner") != "") {
                            videosGetRatingRequest.setOnBehalfOfContentOwner(checkLikeParameters.get("onBehalfOfContentOwner").toString());
                        }

                        VideoGetRatingResponse checkLikeResponse = videosGetRatingRequest.execute();
                        if (checkLikeResponse.getItems() != null && !checkLikeResponse.getItems().get(0).getRating().equals("like")) {
                            return SUBSCRIBED_BUT_NOT_LIKE;
                        }
                    }
                }
                return SUBSCRIBED_AND_LIKED;
            } else {
                Log.d("Khang", "CheckSubcriberTask: auto SUBSCRIBED_AND_LIKED without check api");
                return SUBSCRIBED_AND_LIKED;
            }

        }


        @Override
        protected void onPreExecute() {
            try {
                if (progressDialog != null) {
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle(getString(R.string.subscribe_to_channel));
                    progressDialog.setMessage(getString(R.string.subscribe_to_channel_message));
                    progressDialog.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            isWaitingForSub = false;
            if (result.equals(SUBSCRIBED_AND_LIKED)) {
                ratingResultSubscribed();
            } else {
                PreferenceUtil.saveBooleanPref(PreferenceUtil.FORCE_CHECK_SUB, true);
                String title, message, positiveButton;
                if (result.equals(NOT_SUBSCRIBE_AND_LIKE)) {
                    title = getString(R.string.chua_dang_ky);
                    message = getString(R.string.chua_dang_ky_chi_tiet);
                    positiveButton = getString(R.string.dang_ky_lai);
                } else {
                    title = getString(R.string.chua_thich);
                    positiveButton = getString(R.string.thich_lai);
                    message = getString(R.string.sub_but_not_like);
                }

                try {
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    subscribeButtonOnclick();
//                                    dialog.dismiss();
                                    isWaitingForSub = true;
                                    visitVideo(AppUtil.getVideoURL(mSubCampaign.getVideoId()));
                                }
                            }).setNegativeButton(getString(R.string.xem_cai_khac), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    queryDatabase();
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            isWaitingForSub = false;
            if (mLastError != null) {
                Log.d("Khang", "getDataFrom API: cancelled " + mLastError.toString());
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else if (mLastError instanceof UserRecoverableAuthException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    Log.d("Khang", "CheckLikedVideoTask: auto subscribed caused by usage limits");
                    ratingResultSubscribed();

                }
            } else {
            }
        }
    }

    private void ratingResultSubscribed() {
        Log.d("khang", "ratingResultSubscribed");
        if (mSubCampaign != null) {
            PreferenceUtil.saveLongPref(PreferenceUtil.TIME_COUNTER_SUBSCRIBED, System.currentTimeMillis());
            if (mChannelSubList == null) mChannelSubList = new ArrayList<>();
            mChannelSubList.add(mSubCampaign.getUserName());
            rewardSub(mSubCampaign);
            queryDatabase();
        } else {
            Toast.makeText(getActivity(), getString(R.string.subscribe_failed_message), Toast.LENGTH_SHORT).show();
        }
        if (!MyChannelApplication.isVipAccount) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initTimer();
                }
            }, 500);
        }
    }

    private void rewardSub(SubCampaign subCampaign) {
        if (subCampaign == null) return;
        if (subCampaign.getKey().equals("")) return;
        if (subCampaign.getKey().equals("wrongkey")) return;

        //to campaign
        DatabaseReference lasTimeRef = getSubCampaignsRef().child(subCampaign.getKey()).child("lasTime");
        DatabaseReference finTimeRef = getSubCampaignsRef().child(subCampaign.getKey()).child("finTime");
        DatabaseReference ipRef = getSubCampaignsRef().child(subCampaign.getKey()).child("ip");
        int order = subCampaign.getOrder();
        DatabaseReference curSubRef = getSubCampaignsRef().child(subCampaign.getKey()).child("curSub");
        curSubRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer curSub = mutableData.getValue(Integer.class);
                if (curSub == null) {
                    return Transaction.success(mutableData);
                }

                curSub = curSub + 1;


                if (curSub >= order) {
                    lasTimeRef.setValue(Long.MAX_VALUE);
                    finTimeRef.setValue(ServerValue.TIMESTAMP);
                    ipRef.setValue(false);
                } else {
                    lasTimeRef.setValue(ServerValue.TIMESTAMP);
                }
                // Set value and report transaction success
                mutableData.setValue(curSub);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
            }
        });

        //to reward viewCoin in current mFirebaseUser
        long coin = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_SUB_COIN_RATE_KEY) + subCampaign.getTimeR();
        saveLogSub(subCampaign, coin);



        //save subscribed to current user
        final DatabaseReference subListRef = FirebaseUtil.getSubscribedListRef();
        subListRef.push().setValue(subCampaign.getUserName());
        FirebaseUtil.getSubscribedCountRef().setValue(ServerValue.increment(1));

        try {
            updateCoin(coin);
            Toast.makeText(getContext(), String.format(getString(R.string.nhan_duoc_coin), coin), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PreferenceUtil.saveBooleanPref(PreferenceUtil.FORCE_CHECK_SUB, false);

        //save liked to current user
        final DatabaseReference likedVideoListRef = FirebaseUtil.getLikedListRef();
        likedVideoListRef.push().setValue(subCampaign.getVideoId());
        FirebaseUtil.getLikedCountRef().setValue(ServerValue.increment(1));

        //save liked video to likedList
        if (mLikedListString.equals("")) {
            mLikedListString = subCampaign.getVideoId();
        } else {
            mLikedListString = subCampaign.getVideoId() + "~" + mLikedListString;
        }
        PreferenceUtil.saveStringPref(PreferenceUtil.LIKED_LIST + FirebaseUtil.getCurrentUserId(), mLikedListString);

        //save getUserName() to subscribedList
        if (mSubscribedListString.equals("")) {
            mSubscribedListString = subCampaign.getUserName();
        } else {
            mSubscribedListString = subCampaign.getUserName() + "~" + mSubscribedListString;
        }
        PreferenceUtil.saveStringPref(PreferenceUtil.SUBSCRIBED_LIST + FirebaseUtil.getCurrentUserId(), mSubscribedListString);

    }

    private void saveLogSub(SubCampaign subCampaign, long coin) {
        if (subCampaign == null) return;
        if (subCampaign.getKey().equals("")) return;
        if (subCampaign.getKey().equals("wrongkey")) return;

        myUserName = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME, "");
        String myUserPhoto = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_PHOTO, "NONE");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference currentLogSubCampaignRef = FirebaseUtil.getLogSubRef().child(subCampaign.getKey());
        currentLogSubCampaignRef.push().setValue(new LogSub(user.getUid(), coin, ServerValue.TIMESTAMP));

    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                getActivity(),
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

}
