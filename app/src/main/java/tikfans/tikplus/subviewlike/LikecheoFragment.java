package tikfans.tikplus.subviewlike;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoGetRatingResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.IUnityBannerListener;
import com.unity3d.services.banners.UnityBanners;
import com.unity3d.services.banners.view.BannerPosition;


import tikfans.tikplus.BuildConfig;
import tikfans.tikplus.ItemURLTiktok;
import tikfans.tikplus.MyChannelApplication;
import tikfans.tikplus.R;
import tikfans.tikplus.TiktokWebClient;
import tikfans.tikplus.model.CountToday;
import tikfans.tikplus.model.ItemVideo;
import tikfans.tikplus.model.LikeCampaign;
import tikfans.tikplus.model.LogAdsReward;
import tikfans.tikplus.model.LogLike;
import tikfans.tikplus.model.ResultUser;
import tikfans.tikplus.model.ResultVideo;
import tikfans.tikplus.service.DemNguocThoiGianServices;
import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.CircleTransform;
import tikfans.tikplus.util.FirebaseUtil;
import tikfans.tikplus.util.PreferenceUtil;
import tikfans.tikplus.util.RemoteConfigUtil;
import tikfans.tikplus.util.SecureDate;

import static android.app.Activity.RESULT_OK;
import static com.unity3d.services.core.misc.Utilities.runOnUiThread;
import static com.unity3d.services.core.properties.ClientProperties.getApplicationContext;
import static tikfans.tikplus.util.AppUtil.LIKE_CAMPAIGN_TYPE;
import static tikfans.tikplus.util.FirebaseUtil.CAMPAIGN_LAST_CHANGE_TIME_STAMP;

import org.jsoup.nodes.Document;


public class LikecheoFragment extends Fragment
        implements View.OnClickListener, RewardedVideoAdListener, IUnityAdsListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MAX_QUERRY_FAIL = 10;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //for overlay
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private static final int CODE_REWARD_VIDEO_ACTIVITY = 3000;

    private OnFragmentInteractionListener mListener;

    private ImageView mVideoThumb;
    //    private TextView mUserNameTextView;
//    private TextView mUserFullnameTextView;
    private Button mBtnLike, mBtnEarnCoin, mBtnReload;
    private Button mBtnSeeOther;
    private Button mBtnEarnCoinNoChannel;
    //    private ArrayList<LikeCampaign> mCampaignsList;
    private LikeCampaign mLikeCampaign;
    private int numberQueryFail = 0;
    private ArrayList<String> mVideoLikedList;
    //    private int mCurrentPosCampaign = 0;
    private ProgressDialog progressDialog;
    private boolean isWaitingForLike = false;

    private RelativeLayout mPageContentLayout;
    private RelativeLayout mNoPageLayout;
    private TextView mTxtChannel, mTxtChannelExtra, mTxtInstruction;
    private RelativeLayout mInstructionLayout, mCountDownLayout;
    private TextView mTxtCountdown;
    private LinearLayout mLikePageButtonLayout;
//    private CardView mCampaignHeaderLayout;

    private CountDownTimer mCountDownTimer;
    private int timerCount = 10;
    private RewardedVideoAd mRewardedVideoAd;
    private Context mContext;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private String myUserName;
    private long mLastTimeQuery = 0;
    private boolean isGotAllSubscribedList = false;
    private CountToday likeCountToday;
    private ProgressDialog dummyCheckingSubscriberDialog;
    private Long mGotoYouTubeTime = 0L;

    //unity
    private View rootView;
    private String unityGameID = "3737693";
    private boolean testMode = false;
    private String placementId = "rewardedVideo";
    final UnityAdsListener myAdsListener = new UnityAdsListener();
    private View bannerView;
    private int loadBannerViewTryCount = 0;

    public LikecheoFragment() {
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
        MobileAds.initialize(mContext, getString(R.string.abmob_interstitial_ad_unit_id));
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getActivity().getApplicationContext());
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        mInterstitialAd = new InterstitialAd(mContext);
        mInterstitialAd.setAdUnitId(getString(R.string.abmob_interstitial_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        myUserName = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME, "UCjDXGbXlfSO4SXN0YLdGO2A");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("Khang", "sub4subFragment oncreate");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_likecheo, container, false);
        mVideoThumb = rootView.findViewById(R.id.video_thumb);
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
        mBtnEarnCoin = rootView.findViewById(R.id.btn_earn_coin);
        mBtnEarnCoinNoChannel = rootView.findViewById(R.id.btn_earn_coin_no_channel);
        mBtnReload = rootView.findViewById(R.id.btn_reload);

        //for checking follow
        mWebView = rootView.findViewById(R.id.webView);
        mWebView.setVisibility(View.INVISIBLE);
        tiktokWebClient = new TiktokWebClient(getApplicationContext(), mWebView);

        mAdView = rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

//        if (bannerView == null && !MyChannelApplication.isVipAccount) {
//            UnityBanners.setBannerPosition(BannerPosition.BOTTOM_CENTER);
//            UnityBanners.loadBanner(getActivity(), "banner");
//        }
        // Initialize the SDK:
//        final IUnityBannerListener myBannerListener = new LikecheoFragment.UnityBannerListener();
//        UnityBanners.setBannerListener(myBannerListener);
        UnityAds.initialize(getActivity(), unityGameID, myAdsListener, testMode);

        mBtnLike.setOnClickListener(this);
        mBtnSeeOther.setOnClickListener(this);
        mBtnEarnCoin.setOnClickListener(this);
        mBtnEarnCoinNoChannel.setOnClickListener(this);
        mBtnReload.setOnClickListener(this);
        progressDialog = new ProgressDialog(getContext());
        mVideoLikedList = new ArrayList<>();
        checkingFollowDialog = new ProgressDialog(getContext());

        //dummy check subscribed
        dummyCheckingSubscriberDialog = new ProgressDialog(getContext());
        dummyCheckingSubscriberDialog.setTitle(getString(R.string.kiem_tra_video_da_thich));
        dummyCheckingSubscriberDialog.setMessage(getString(R.string.kiem_tra_video_da_thich_chi_tiet));
        dummyCheckingSubscriberDialog.setCancelable(false);
        if (dummyCheckingSubscriberDialog != null && dummyCheckingSubscriberDialog.getContext() != null && !dummyCheckingSubscriberDialog.isShowing()) {
            dummyCheckingSubscriberDialog.show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (dummyCheckingSubscriberDialog != null && dummyCheckingSubscriberDialog.isShowing()) {
                        dummyCheckingSubscriberDialog.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 2500);

        initTimer();
        getTodayLikeCount();
        getAllLikedList();


        loadRewardedVideoAd();
        return rootView;
    }

    // Implement the banner listener interface methods:
    private class UnityBannerListener implements IUnityBannerListener {

        @Override
        public void onUnityBannerLoaded(String placementId, View view) {
            // When the banner content loads, add it to the view hierarchy:
            try {
                bannerView = view;
                if (view.getParent() != null) {
                    ((ViewGroup) view.getParent()).removeView(view);
                }
                ((ViewGroup) rootView.findViewById(R.id.adView)).addView(view);
                Log.d("Khang", "onUnityBannerLoaded: " + placementId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUnityBannerUnloaded(String placementId) {
            // When the banner’s no longer in use, remove it from the view hierarchy:
            Log.d("Khang", "onUnityBannerUnloaded: " + placementId);
            bannerView = null;
        }

        @Override
        public void onUnityBannerShow(String placementId) {
            // Called when the banner is first visible to the user.
            Log.d("Khang", "onUnityBannerShow: " + placementId);
        }

        @Override
        public void onUnityBannerClick(String placementId) {
            // Called when the banner is clicked.
            Log.d("Khang", "onUnityBannerClick: " + placementId);
        }

        @Override
        public void onUnityBannerHide(String placementId) {
            // Called when the banner is hidden from the user.
            Log.d("Khang", "onUnityBannerHide: " + placementId);
        }

        @Override
        public void onUnityBannerError(String message) {
            Log.d("Khang", "onUnityBannerError: " + message);
            if (bannerView == null && loadBannerViewTryCount < 10 && !MyChannelApplication.isVipAccount) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UnityBanners.setBannerPosition(BannerPosition.BOTTOM_CENTER);
                        UnityBanners.loadBanner(getActivity(), "banner");
                        loadBannerViewTryCount++;
                    }
                }, 1000);

            }
            // Called when an error occurred, and the banner failed to load or show.
        }
    }

    private class UnityAdsListener implements IUnityAdsListener {

        public void onUnityAdsReady(String placementId) {
            // Implement functionality for an ad being ready to show.
        }

        @Override
        public void onUnityAdsStart(String placementId) {
            // Implement functionality for a user starting to watch an ad.
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
        }
    }

    private void getTodayLikeCount() {
        likeCountToday = new CountToday();
        FirebaseUtil.getLikedTodayRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    likeCountToday = dataSnapshot.getValue(CountToday.class);
                    Date date = SecureDate.getInstance().getDate();
                    DateFormat df;
                    df = new SimpleDateFormat(AppUtil.FORMAT_DATE);
                    String today = df.format(date);
                    if (!likeCountToday.getToday().equals(today)) {
                        likeCountToday.setToday(today);
                        likeCountToday.setCount(0);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // for checking follow
    private WebView mWebView;
    private TiktokWebClient tiktokWebClient;
    private String myFollowingByWebView = "0";
    private String myOldFollowingByWebView = "0";
    private String mUserLink;
    private ProgressDialog checkingFollowDialog;

    private void getVideoInfoByWebView(boolean isNeedCheckFollow) {
        final boolean[] isLoaded = {false}; // haom onloadFinish bi goi 2 lan, chi xu ly o lan goi dau tien
        try {
            if (checkingFollowDialog != null && !checkingFollowDialog.isShowing()) {
                checkingFollowDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String videoURL = AppUtil.getVideoURL(mLikeCampaign.getVideoId());
        ItemURLTiktok url = new ItemURLTiktok(videoURL, null);
        Log.e("khangcheckfollow", "getUserInfoByWebView: user link " + url.getBaseUrl());
        tiktokWebClient.load(url.getBaseUrl());
        tiktokWebClient.setListener(new TiktokWebClient.ClientListener() {
            @Override
            public void onLoading() {
                Log.e("khang", "onLoading: ");
                try {
                    if (checkingFollowDialog != null) {
                        if (isNeedCheckFollow) {
                            checkingFollowDialog.setCancelable(false);
                            checkingFollowDialog.setTitle(getString(R.string.like_to_video));
                            checkingFollowDialog.setMessage(getString(R.string.like_to_video_message));
                        } else {
                            checkingFollowDialog.setCancelable(true);
                            checkingFollowDialog.setTitle("checkingfollowdialog");
                            checkingFollowDialog.setMessage("");
                        }
                        checkingFollowDialog.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLoadFinish(Document document, String url) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (checkingFollowDialog != null && checkingFollowDialog.isShowing()) {
                                checkingFollowDialog.dismiss();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (isLoaded[0]) return;
                        isLoaded[0] = true;
                        Log.e("khangcheckfollow", "getUserInfoByWebView onLoadFinish: " + url);
                        //for update video thumbnail;
                        String img = mLikeCampaign.getVideoThumb();
                        String expiredTime = img.substring(img.lastIndexOf("expires=") + 8, img.lastIndexOf("expires=") + 18);
                        Log.d("khangcheckfollow", "expiredTime: " + expiredTime + " / " + System.currentTimeMillis());

                        long l = Long.parseLong(expiredTime);
                        if (l < System.currentTimeMillis() / 1000) {
                            Log.d("khang", "checkby webview da het han" + expiredTime);
                            try {
                                String imageUrl = document.getElementsByClass("tiktok-player").get(0).getElementsByTag("img").get(0).attributes().get("src");
                                Log.d("khang", "video thumbnailUrl: " + imageUrl);
                                if (mLikeCampaign != null && imageUrl != null) {
                                    mVideoThumb.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Picasso.get().load(imageUrl).transform(new CircleTransform())
                                                    .into(mVideoThumb);
                                        }
                                    });
                                    final DatabaseReference campaignCurrentRef = FirebaseUtil.getLikeCampaignsRef().child(mLikeCampaign.getKey());
                                    campaignCurrentRef.runTransaction(new Transaction.Handler() {
                                        @NonNull
                                        @Override
                                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                            LikeCampaign currentCampaign = mutableData.getValue(LikeCampaign.class);
                                            if (currentCampaign == null) {
                                                return Transaction.success(mutableData);
                                            }

                                            currentCampaign.setVideoThumb(imageUrl);

                                            // Set value and report transaction success
                                            mutableData.setValue(currentCampaign);
                                            return Transaction.success(mutableData);
                                        }

                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Log.d("khang", "webview get imageUrl error: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            Log.d("khang", "checkby webview chua het han" + expiredTime);
                        }

                        if (url.contains("notfound")) { //block ip from india so url is notfound
                            Log.d("khangcheckfollow", "webview: load faild caused by block ip");
                            if (isNeedCheckFollow) {
                                myFollowingByWebView = "-1"; // bang 0 mac dich la da tha tim
                                checkFollow();
                                myOldFollowingByWebView = "-1";
                            } else {
                                myFollowingByWebView = "-1"; // bang 0 mac dich la da tha tim
                                myOldFollowingByWebView = "-1";
                            }
                            return;
                        }

                        String likeCount = "-1";
                        try {
                            likeCount = document.getElementsByClass("tiktok-toolbar").get(0).getElementsByTag("strong").get(0).text();
                        } catch (Exception e) {
                            Log.d("khang", "webview get get Likecount error: " + e.getMessage());
                            likeCount = "-1";
                            e.printStackTrace();
                        }
                        Log.e("khangcheckfollow", "getUserInfoByWebView likeCount: " + likeCount);
                        if (isNeedCheckFollow) {
                            myFollowingByWebView = likeCount;
                            checkFollow();
                            myOldFollowingByWebView = likeCount;
                        } else {
                            myOldFollowingByWebView = likeCount;

                            myFollowingByWebView = likeCount;
                        }
                        mWebView.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }

    private void checkFollow() {
        int oldFl = AppUtil.convertStringToInteger(myOldFollowingByWebView);
        int newFl = AppUtil.convertStringToInteger(myFollowingByWebView);
        Log.d("khangcheckfollow", "checkfollow: old: " + oldFl + " new: " + newFl);
        if (oldFl > 9999) {//video nhieu hon `10k like mac dich da follow
            ratingResultLiked();
        } else {
            if (newFl > oldFl || newFl == -1) {// da follow
                ratingResultLiked();
            } else {// chua follow
                PreferenceUtil.saveBooleanPref(PreferenceUtil.FORCE_CHECK_SUB, true);
                String title, message, positiveButton;
                title = getString(R.string.chua_thich);
                message = getString(R.string.chua_thich_chi_tiet);
                positiveButton = getString(R.string.thich_lai);

                try {
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isWaitingForLike = true;
                                    subscribeButtonOnclick();
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
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("khang", "sub4subFragment onResume: " + isWaitingForLike);
        mRewardedVideoAd.resume(getContext());

        try {
            Intent myService = new Intent(getActivity(), DemNguocThoiGianServices.class);
            getActivity().stopService(myService);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isWaitingForLike) {
            isWaitingForLike = false;

            long currentTime = SecureDate.getInstance().getDate().getTime();
            if (mLikeCampaign != null && mLikeCampaign.getVideoId() != null && currentTime - mGotoYouTubeTime < mLikeCampaign.getTimeR() * 1000 - 1000) {
                Log.d("khang", "sub4subFragment onResume : " + (currentTime - mGotoYouTubeTime));
                AlertDialog timerWarningDialog = new AlertDialog.Builder(mContext)
                        .setTitle(getString(R.string.khong_du_thoi_gian_xem))
                        .setMessage(String.format(getString(R.string.khong_du_thoi_gian_xem_de_like), mLikeCampaign.getTimeR()))
                        .setPositiveButton(getString(R.string.xem_lai), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                isWaitingForLike = true;
                                subscribeButtonOnclick();

                            }
                        }).setNegativeButton(getString(R.string.video_khong_tim_thay), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                timerWarningDialog.show();
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getVideoInfoByWebView(true);
                    }
                }, 1000);
            }
        }

        if (MyChannelApplication.isVipAccount) {
            removedBannerAds();
        }

    }

    private String mLikedListString;
    private void getAllLikedList() {
        mLikedListString = PreferenceUtil.getStringPref(PreferenceUtil.LIKED_LIST + FirebaseUtil.getCurrentUserId(), "");
        mVideoLikedList = new ArrayList<>(Arrays.asList(mLikedListString.split("~")));

        final DatabaseReference likeCountRef = FirebaseUtil.getLikedCountRef();
        likeCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                if (snapshot.exists()) {
                    count = snapshot.getValue(int.class);
                }
                if (mVideoLikedList.size() < count) {
                    final DatabaseReference likedListRef =
                            FirebaseUtil.getLikedListRef();
                    final int finalCount = count;
                    likedListRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            isGotAllSubscribedList = true;
                            mVideoLikedList.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                mVideoLikedList.add(snapshot.getValue().toString());
                                Log.d("Khang", "liked: " + snapshot.getValue().toString());
                            }
                            if (mVideoLikedList.size() != finalCount) {
                                likeCountRef.setValue(mVideoLikedList.size());
                            }
                            //save to local
                            //save videoID to subscribedList
                            if (mVideoLikedList.size() > 1000) {
                                try {
                                    mVideoLikedList = new ArrayList<>(mVideoLikedList.subList(mVideoLikedList.size() / 2, mVideoLikedList.size()));
                                    likedListRef.setValue(mVideoLikedList);
                                    likeCountRef.setValue(mVideoLikedList.size());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mVideoLikedList = new ArrayList<>(mVideoLikedList.subList(mVideoLikedList.size() / 2, mVideoLikedList.size() - 1));
                                    likedListRef.setValue(mVideoLikedList);
                                    likeCountRef.setValue(mVideoLikedList.size());
                                }
                            }

                            mLikedListString = "";
                            for (String tmp : mVideoLikedList) {
                                if (mLikedListString.equals("")) {
                                    mLikedListString = tmp;
                                } else {
                                    mLikedListString = mLikedListString + "~" + tmp;
                                }
                            }
                            PreferenceUtil.saveStringPref(PreferenceUtil.LIKED_LIST + FirebaseUtil.getCurrentUserId(), mLikedListString);
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
                    Log.d("khang", "mLikedList: " + mLikedListString);
                    queryDatabase();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        final DatabaseReference oldLikedListRef =
//                FirebaseUtil.getOldLikedListRef();
//        oldLikedListRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    mVideoLikedList.add(snapshot.getValue().toString());
//                    Log.d("Khang", "liked: " + snapshot.getValue().toString());
//                }
//
//                final DatabaseReference likedListRef = FirebaseUtil.getLikedListRef();
//                if (mVideoLikedList.size() == 0) {
//                    likedListRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            isGotAllSubscribedList = true;
//                            for (DataSnapshot snapshot2 : snapshot.getChildren()) {
//                                mVideoLikedList.add(snapshot2.getValue().toString());
//                                Log.d("Khang", "liked: " + snapshot2.getValue().toString());
//                            }
//
//                            if (mVideoLikedList.size() > 0) {//check unfollow
//                                Long lastCheckSubListTime = PreferenceUtil.getLongPref(PreferenceUtil.LAST_CHECK_LIKED_LIST_TIME, 0);
//                                if (lastCheckSubListTime == 0) {
//                                    lastCheckSubListTime = System.currentTimeMillis();
//                                    PreferenceUtil.saveLongPref(PreferenceUtil.LAST_CHECK_LIKED_LIST_TIME, System.currentTimeMillis());
//                                    // TODO: 8/14/2020
//                                    //CheckUnfollowTask;
//                                }
//                                Log.d("Khang", "Time check unLike: " + FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_TIME_CHECK_UNLIKE));
//
//                                if (System.currentTimeMillis() - lastCheckSubListTime > FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_TIME_CHECK_UNLIKE) * 1000 * 86400) {
//                                    try {
//                                        PreferenceUtil.saveLongPref(PreferenceUtil.LAST_CHECK_LIKED_LIST_TIME, System.currentTimeMillis());
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//
//                            }
//                            if (mVideoLikedList.size() > 400) {
//                                try {
//                                    likedListRef.setValue(mVideoLikedList.subList(mVideoLikedList.size() / 2, mVideoLikedList.size()));
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                    likedListRef.setValue(mVideoLikedList.subList(mVideoLikedList.size() / 2, mVideoLikedList.size() - 1));
//                                }
//                            }
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
//                    oldLikedListRef.removeValue();
//                    likedListRef.setValue(mVideoLikedList);
//                }
//            }
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

    public void forceLogout() {
        Log.d("Khang", " forceLogout: " + mListener);
        if (mListener != null) {
            mListener.forceLogout(null);
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
        mRewardedVideoAd.pause(getContext());
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(getContext());
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        if (dummyCheckingSubscriberDialog != null && dummyCheckingSubscriberDialog.isShowing()) {
            dummyCheckingSubscriberDialog.dismiss();
            dummyCheckingSubscriberDialog = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLike:
                // for release
                subscribeButtonOnclick();

                //for admin to delete campaign
//                if (mLikeCampaign != null && mLikeCampaign.getKey() != null && !mLikeCampaign.getKey().equals("")) {
//                    DatabaseReference wrongCampaignRef = FirebaseUtil.getLikeCampaignsRef().child(mLikeCampaign.getKey());
//                    wrongCampaignRef.removeValue(new DatabaseReference.CompletionListener() {
//                        @Override
//                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
//                            String message = "Deleted";
//                            if (databaseError != null) {
//                                message = databaseError.getMessage();
//                            }
//                            Toast.makeText(getContext(), "delete campaign: " + message, Toast.LENGTH_LONG).show();
//                        }
//                    });
//                }
                break;
            case R.id.btnSeeOther:
                queryDatabase();
                break;
            case R.id.btn_reload:
                queryDatabase();
                break;
            case R.id.btn_earn_coin:
            case R.id.btn_earn_coin_no_channel:
                showAds();
                break;
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
                                .setTitle("Can not find Tiktok app")
                                .setMessage("Please install Tiktok app on your phone")

                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                // The dialog is automatically dismissed when a dialog button is clicked.
                                .setPositiveButton(getString(R.string.install), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        isWaitingForLike = false;
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
        if (mRewardedVideoAd.isLoaded()) {
            Log.d("Khang", "show adsmob");
            mRewardedVideoAd.show();
        } else {
            Log.d("Khang", "show unity ads");
            if (UnityAds.isReady(placementId)) {
                UnityAds.show(getActivity(), placementId);
            } else {
                UnityAds.initialize(getActivity(), unityGameID, myAdsListener, testMode);
            }
        }
    }


    private void updateCampaign() {
        if (mLikeCampaign == null || !mLikeCampaign.isIp()) {
            mNoPageLayout.setVisibility(View.VISIBLE);
            mPageContentLayout.setVisibility(View.INVISIBLE);
            hideProgressDialog();
            if (!MyChannelApplication.isVipAccount) {
                if (mRewardedVideoAd.isLoaded()) {
                    mRewardedVideoAd.show();
                } else if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
            }
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
        try {
            if (i % 2 == 0) {
                mTxtInstruction.setText(getString(R.string.huong_dan_thich));
            } else {
                mTxtInstruction.setText(getString(R.string.like_instruction2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if (isAdminVer) {
//            mTxtInstruction.setText("videoID: " + mLikeCampaign.getVideoId() + " key: " + mLikeCampaign.getKey() + " own: " + mLikeCampaign.getOwnId() + " order: " + mLikeCampaign.getOrder() + " curSub: " + mLikeCampaign.getCurLike() + " channelId: ");
//        }
        Picasso.get().load(mLikeCampaign.getVideoThumb()).into(mVideoThumb);


        //check tha tim bang webview
        getVideoInfoByWebView(false);
        if (mLikeCampaign.getUserName() != null) {
            if (mLikeCampaign.getVideoThumb() != null && !mLikeCampaign.getVideoThumb().equals("") && !mLikeCampaign.getVideoThumb().equals("NONE")) {
                Picasso.get().load(mLikeCampaign.getVideoThumb()).transform(new CircleTransform())
                        .into(mVideoThumb);
                String img = mLikeCampaign.getVideoThumb();
                try {
                    String expiredTime = img.substring(img.lastIndexOf("expires=") + 8, img.lastIndexOf("expires=") + 18);
                    Log.d("khang", "expiredTime: " + expiredTime + " / " + System.currentTimeMillis());

                    long l = Long.parseLong(expiredTime);
                    if (l < System.currentTimeMillis() / 1000) {
                        Log.d("khang", "da het han" + expiredTime);
                        String link = AppUtil.TIKTOK_PREFIX_LINK + mLikeCampaign.getUserName();
                        ItemURLTiktok url = new ItemURLTiktok(link, listener);
                        url.getListVideoFromUser();
                    } else {
                        Log.d("khang", "chua het han" + expiredTime);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                String link = AppUtil.TIKTOK_PREFIX_LINK + mLikeCampaign.getUserName();
                ItemURLTiktok url = new ItemURLTiktok(link, listener);
                url.getListVideoFromUser();
            }
        } else {
            Log.d("khang", "mUserName is null");
            final DatabaseReference mUserNameRef = FirebaseUtil.getUserNameRef(mLikeCampaign.ownId);
            mUserNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String link = AppUtil.TIKTOK_PREFIX_LINK + snapshot.getValue(String.class);
                        ItemURLTiktok url = new ItemURLTiktok(link, listener);
                        url.getListVideoFromUser();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

    ItemURLTiktok.ClientTikTokListener listener = new ItemURLTiktok.ClientTikTokListener() {
        @Override
        public void onLoading() {
        }

        @Override
        public void onReceivedListVideo(ResultVideo result) {
            List<ItemVideo> itemVideoList = result.getResult();
            if (itemVideoList != null && itemVideoList.size() > 0) {
                for (int i = 0; i < itemVideoList.size(); i++) {
                    ItemVideo itemVideo = itemVideoList.get(i);

                    if (mLikeCampaign != null && mLikeCampaign.getVideoId() != null && mLikeCampaign.getVideoId().equals(itemVideo.getId())) {
                        String img = itemVideo.getImageUrl();
                        Picasso.get().load(img).transform(new CircleTransform())
                                .into(mVideoThumb);
                        final DatabaseReference campaignCurrentRef = FirebaseUtil.getLikeCampaignsRef().child(mLikeCampaign.getKey());
                        campaignCurrentRef.runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                LikeCampaign currentCampaign = mutableData.getValue(LikeCampaign.class);
                                if (currentCampaign == null) {
                                    return Transaction.success(mutableData);
                                }

                                currentCampaign.setVideoThumb(img);

                                // Set value and report transaction success
                                mutableData.setValue(currentCampaign);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                            }
                        });
                    }
                }
            }
        }

        @Override
        public void onReceivedUserInfo(final ResultUser result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onError(int code, String mess) {
            Log.d("khangcheckfollow", "get video by api error: " + mess);
        }

        @Override
        public void onInvalidLink() {
        }

        @Override
        public void onCheckLinkDone() {
        }
    };

    private void initTimer() {
        Long prefTime = PreferenceUtil.getLongPref(PreferenceUtil.TIME_COUNTER_LIKED, 0);
        long subTime = System.currentTimeMillis() - prefTime;
        if (subTime > 0 && subTime < ((long) (timerCount * 1000))) {
            Log.d("Khang", "init Timer");
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
                            if (i1 < 50 && !MyChannelApplication.isVipAccount && likeCountToday.getCount() > 5) {
                                showAds();
                            }
                            PreferenceUtil.saveLongPref(PreferenceUtil.TIME_COUNTER_LIKED, 0);
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
        if (System.currentTimeMillis() - lastQueryTime < 1000) {
            Log.d("Khang", "query too fast");
            return;
        }
        lastQueryTime = System.currentTimeMillis();
        long limitLike = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_LIMIT_LIKE);
        if (MyChannelApplication.isVipAccount) {
            limitLike = limitLike + 10;
        }
        Log.d("Khang", "Sub Today: " + likeCountToday.getCount() + " / " + limitLike);
        if (likeCountToday != null && likeCountToday.getCount() <= limitLike) {
            if (isGotAllSubscribedList) {
                mLikeCampaign = null;
                numberQueryFail = 0;
                retryQueryDatabase();
            } else {
                getAllLikedList();
            }
        } else {
            Log.d("Khang", "Reached Limit Sub Today: " + likeCountToday.getCount());
            mLikeCampaign = null;
            mNoPageLayout.setVisibility(View.VISIBLE);
            mPageContentLayout.setVisibility(View.INVISIBLE);
            mTxtChannel.setText(getString(R.string.gioi_han_luot_thich));
            mTxtChannelExtra.setText(String.format(getString(R.string.gioi_han_luot_thich_chi_tiet), likeCountToday.getCount()));
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
//        showProgressDialog();
        if (numberQueryFail >= MAX_QUERRY_FAIL) {
            mLikeCampaign = null;
            updateCampaign();
            return;
        }
        mLastTimeQuery = SecureDate.getInstance().getDate().getTime();
        Query query = FirebaseUtil.getLikeCampaignsRef().orderByChild(CAMPAIGN_LAST_CHANGE_TIME_STAMP)
                .limitToFirst(1);
        //        Query query = FirebaseUtil.getCampaignsRef().child("inProgess").equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot campaignDataSnapshot : dataSnapshot.getChildren()) {
                        // do something with the individual "issues"
                        LikeCampaign tmpLikeCampaigns = null;
                        try {
                            tmpLikeCampaigns = campaignDataSnapshot.getValue(LikeCampaign.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (tmpLikeCampaigns != null) {
                            Log.d("Khang", "querry: key:" + tmpLikeCampaigns.getKey() + " /cursub: " + tmpLikeCampaigns.getCurLike() + " /oder" + tmpLikeCampaigns.getOrder() + " /ownID: " + tmpLikeCampaigns.getOwnId());


                            //kiem tra key, neu key sai phai set lai key
                            if (tmpLikeCampaigns.getKey() != null && !tmpLikeCampaigns.getKey().equals(campaignDataSnapshot.getKey())) {
                                tmpLikeCampaigns.setKey(campaignDataSnapshot.getKey());
                                tmpLikeCampaigns.setLasTime(ServerValue.TIMESTAMP);
                                final DatabaseReference campaignCurrentRef = FirebaseUtil.getLikeCampaignsRef().child(tmpLikeCampaigns.getKey());
                                campaignCurrentRef.setValue(tmpLikeCampaigns);
                            }

                            //kiem tra channelid, neu khong co id xoa chien dich va query lai
                            if (tmpLikeCampaigns.getKey() == null || tmpLikeCampaigns.getVideoId() == null || tmpLikeCampaigns.getVideoId().equals("")) {
                                DatabaseReference wrongCampaignRef = campaignDataSnapshot.getRef();
                                wrongCampaignRef.removeValue();
                                numberQueryFail++;
                                retryQueryWithDelay();
                                return;
                            } else {

                                //chien dich da hoan thanh, hoac da dang ky kenh, query lai database
                                if (!tmpLikeCampaigns.isIp()) {
                                    Long lastTimeStamp = (Long) tmpLikeCampaigns.getLasTime();
                                    if (lastTimeStamp < Long.MAX_VALUE) {
                                        final DatabaseReference campaignCurrentRef = FirebaseUtil.getLikeCampaignsRef().child(tmpLikeCampaigns.getKey());
                                        tmpLikeCampaigns.setLasTime(Long.MAX_VALUE);
                                        campaignCurrentRef.setValue(tmpLikeCampaigns);
                                    }
                                    numberQueryFail = MAX_QUERRY_FAIL;
                                    retryQueryWithDelay();
                                    return;
                                }

                                //update timestamp, query success
                                final DatabaseReference campaignCurrentRef = FirebaseUtil.getLikeCampaignsRef().child(tmpLikeCampaigns.getKey());
                                tmpLikeCampaigns.setLasTime(ServerValue.TIMESTAMP);
                                campaignCurrentRef.setValue(tmpLikeCampaigns);

                                if (!isLiked(tmpLikeCampaigns.getVideoId())) {
                                    mLikeCampaign = tmpLikeCampaigns;
                                    numberQueryFail = 0;
                                } else {
                                    Log.d("Khang", "subscribed: " + tmpLikeCampaigns.getVideoId());
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

                try {
                    if (databaseError.getMessage().equals("Permission denied")) {
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
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        hideProgressDialog();
        super.onDestroyView();
    }

    private Boolean isLiked(String videoId) {
        for (int i = 0; i < mVideoLikedList.size(); i++) {
            if (mVideoLikedList.get(i).equals(videoId)) {
                return true;
            }
        }
        return false;
    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd
                .loadAd(getString(R.string.admob_reward_ad_unit_id), new AdRequest.Builder().build());
    }

    public void removedBannerAds() {
        if (mAdView != null && mAdView.getVisibility() == View.VISIBLE) {
            mAdView.setVisibility(View.GONE);
        }
    }

    private void subscribeButtonOnclick() {
        if (mLikeCampaign == null || mLikeCampaign.getVideoId() == null || mLikeCampaign.getVideoId().equals(""))
            return;

        long coin = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_LIKE_COIN_RATE_KEY) + mLikeCampaign.getTimeR();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {

            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getContext().getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
            try {
                if (visitVideo(AppUtil.getVideoURL(mLikeCampaign.getVideoId()))) {
                    isWaitingForLike = true;
                    mGotoYouTubeTime = SecureDate.getInstance().getDate().getTime();
                    Intent intent = new Intent(getActivity(), DemNguocThoiGianServices.class);
                    intent.putExtra(AppUtil.DEM_THOI_GIAN_CAMPAIGN_TIME_REQUEST_EXTRA, mLikeCampaign.getTimeR());
                    intent.putExtra(AppUtil.DEM_THOI_GIAN_CAMPAIGN_COIN_EXTRA, coin);
                    PreferenceUtil.saveIntPref(AppUtil.DEM_THOI_GIAN_CAMPAIGN_TIME_REQUEST_EXTRA, mLikeCampaign.getTimeR());
                    PreferenceUtil.saveLongPref(AppUtil.DEM_THOI_GIAN_CAMPAIGN_COIN_EXTRA, coin);
                    PreferenceUtil.saveIntPref(AppUtil.DEM_THOI_GIAN_CAMPAIGN_TYPE_EXTRA, LIKE_CAMPAIGN_TYPE);
                    getActivity().startService(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (visitVideo(AppUtil.getVideoURL(mLikeCampaign.getVideoId()))) {
                    isWaitingForLike = true;
                    mGotoYouTubeTime = SecureDate.getInstance().getDate().getTime();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Khang", "FollowFragment: onActivityResult");
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
//            isWaitingForLike = true;
////            if (mLikeCampaign.getVideoId() == null || mLikeCampaign.getVideoId().equals("")) {
////                visitChannel(mLikeCampaign.getChannelId());
////            } else {
////                visitVideo(mLikeCampaign.getVideoId());
////            }
////            subscribeButtonOnclick();
        } else if (requestCode == CODE_REWARD_VIDEO_ACTIVITY) {
            if (resultCode != RESULT_OK && !MyChannelApplication.isVipAccount) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                } else {
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        onRewarded();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        } else {
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        }
    }

    @Override
    public void onRewardedVideoCompleted() {

    }

    private void onRewarded() {
        if (getContext() != null) {
            Toast.makeText(getContext(), String.format(getString(R.string.nhan_duoc_coin), FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_VIDEO_REWARD)), Toast.LENGTH_SHORT).show();
        }
        FirebaseUtil.getLogAdsRewardCurrentUserRef().push().setValue(new LogAdsReward(FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_VIDEO_REWARD), ServerValue.TIMESTAMP));
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

    public static LikecheoFragment newInstance(String param1, String param2) {
        LikecheoFragment fragment = new LikecheoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private class CheckLikedVideoTask extends AsyncTask<Void, Void, String> {
        private com.google.api.services.youtube.YouTube youTube = null;
        private Exception mLastError = null;
        private String mVideoId = "";
        private ProgressDialog progressDialog;

        CheckLikedVideoTask(GoogleAccountCredential credential, String videoId) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            youTube = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("USub")
                    .build();
            mVideoId = videoId;
            progressDialog = new ProgressDialog(getContext());
        }

        /**
         * Background task to call YouTube Data API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected String doInBackground(Void... params) {
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
        private String getDataFromApi() throws IOException {
            Random r = new Random();
            int i1 = r.nextInt(99);
            int i2 = r.nextInt(99);
            boolean forceCheckLike = PreferenceUtil.getBooleanPref(PreferenceUtil.FORCE_CHECK_LIKE, false);
            long rateCheckLike = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_RATE_CHECK_LIKE_BY_QUOTA);
            long rateNoSubLike = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_RATE_NO_SUBSCRIBE_AND_LIKE);


            if (rateNoSubLike > 0 && (FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigUtil.TIKFANS_YOUTUBE_API_REMOVED) || FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigUtil.TIKFANS_SAVE_MAX_QUOTA_ENABLE))) {
                boolean firstTimeNeedToShowNotSub = PreferenceUtil.getBooleanPref(PreferenceUtil.FIRST_TIME_NOT_SUB, true);
                if (firstTimeNeedToShowNotSub) {
                    PreferenceUtil.saveBooleanPref(PreferenceUtil.FIRST_TIME_NOT_SUB, false);
                    Log.d("Khang", "first time not sub");
                    return "unspecified";
                }
            }


            Log.d("Khang", "CheckLikedVideoTask: check Auto return not sub and like: " + i2 + " / " + rateNoSubLike);
            if (i2 < rateNoSubLike) {
                //auto not sub and like
                Log.d("Khang", "CheckLikedVideoTask: Auto return not sub and like: " + i2 + " / " + rateNoSubLike);
                return "unspecified";
            }

            if (true) {
                return "like";
            }

            if (mVideoLikedList.size() <= 2) {
                rateCheckLike = 100;
            } else if (mVideoLikedList.size() <= 4) {
                if (rateCheckLike < 50) {
                    rateCheckLike = 50;
                }
            }

            if (FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigUtil.TIKFANS_YOUTUBE_API_REMOVED) || FirebaseRemoteConfig.getInstance().getBoolean(RemoteConfigUtil.TIKFANS_SAVE_MAX_QUOTA_ENABLE)) {
                Log.d("Khang", "youtube api da bi go bo ");
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "like";
            }

            Log.d("Khang", "CheckLikedVideoTask: ?check liked by api: " + i1 + " / " + rateCheckLike + " Size: " + mVideoLikedList.size() + "Force Check Like: " + forceCheckLike);
            //50% need to check liked to reduce quota 50%.
            if (i1 < rateCheckLike || forceCheckLike) {
                Log.d("Khang", "CheckLikedVideoTask: check liked by api: " + i1 + " / " + rateCheckLike + " Size: " + mVideoLikedList.size() + "Force Check Like: " + forceCheckLike);
                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("id", mVideoId);
                parameters.put("onBehalfOfContentOwner", "");

                YouTube.Videos.GetRating videosGetRatingRequest = youTube.videos().getRating(parameters.get("id").toString());
                if (parameters.containsKey("onBehalfOfContentOwner") && parameters.get("onBehalfOfContentOwner") != "") {
                    videosGetRatingRequest.setOnBehalfOfContentOwner(parameters.get("onBehalfOfContentOwner").toString());
                }

                VideoGetRatingResponse response = videosGetRatingRequest.execute();
                if (response.getItems() != null) {
                    return response.getItems().get(0).getRating();
                }
                return "unspecified";
            } else {
                Log.d("Khang", "CheckLikedVideoTask: auto like without check api");
                return "like";
            }


        }


        @Override
        protected void onPreExecute() {
            try {
                if (progressDialog != null && !progressDialog.isShowing()) {
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle(getString(R.string.like_to_video));
                    progressDialog.setMessage(getString(R.string.like_to_video_message));
                    progressDialog.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String ratingResult) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            isWaitingForLike = false;
            if (ratingResult.equals("like")) {
                ratingResultLiked();
                PreferenceUtil.saveBooleanPref(PreferenceUtil.FORCE_CHECK_LIKE, false);
            } else {
                PreferenceUtil.saveBooleanPref(PreferenceUtil.FORCE_CHECK_LIKE, true);
                try {
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                            .setTitle(getString(R.string.chua_thich))
                            .setMessage(getString(R.string.chua_thich_chi_tiet))
                            .setPositiveButton(getString(R.string.thich_lai), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isWaitingForLike = true;
                                    if (mLikeCampaign != null && mLikeCampaign.getVideoId() != null && !mLikeCampaign.getVideoId().equals("")) {
                                        visitVideo(AppUtil.getVideoURL(mLikeCampaign.getVideoId()));
                                    }
                                    dialog.dismiss();
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
            super.onPostExecute(ratingResult);
        }


        @Override
        protected void onCancelled() {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            isWaitingForLike = false;
            if (mLastError != null) {
                Log.d("Khang", "getDataFrom API: cancelled " + mLastError.toString());
                Log.d("Khang", "CheckLikedVideoTask: auto liked caused by usage limits");
                ratingResultLiked();
            }
        }
    }

    private void ratingResultLiked() {
        if (mLikeCampaign != null) {
            PreferenceUtil.saveLongPref(PreferenceUtil.TIME_COUNTER_LIKED, System.currentTimeMillis());
            if (mVideoLikedList == null) mVideoLikedList = new ArrayList<>();
            mVideoLikedList.add(mLikeCampaign.getVideoId());
            rewardLike(mLikeCampaign);
            queryDatabase();
        } else {
//            Toast.makeText(getActivity(), getString(R.string.liked_failed_message), Toast.LENGTH_SHORT).show();
        }
        if (!MyChannelApplication.isVipAccount) {
            initTimer();
        }

    }

    private void rewardLike(LikeCampaign likeCampaign) {
        if (likeCampaign == null) return;
        if (likeCampaign.getKey().equals("")) return;
        if (likeCampaign.getKey().equals("wrongkey")) return;

        //to campaign
        DatabaseReference currentCampaignRef = FirebaseUtil.getLikeCampaignsRef().child(likeCampaign.getKey());
        currentCampaignRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                LikeCampaign currentCampaign = mutableData.getValue(LikeCampaign.class);
                if (currentCampaign == null) {
                    return Transaction.success(mutableData);
                }

                currentCampaign.setCurLike(currentCampaign.getCurLike() + 1);
                currentCampaign.setLasTime(ServerValue.TIMESTAMP);
                if (currentCampaign.getCurLike() >= currentCampaign.getOrder()) {
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

        //to reward viewCoin in current mFirebaseUser
        long coin = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_LIKE_COIN_RATE_KEY) + likeCampaign.getTimeR();
        saveLogLike(mLikeCampaign, coin);

        //save videoID to subscribedList
        if (mLikedListString.equals("")) {
            mLikedListString = likeCampaign.getVideoId();
        } else {
            mLikedListString = likeCampaign.getVideoId() + "~" + mLikedListString;
        }
        PreferenceUtil.saveStringPref(PreferenceUtil.LIKED_LIST + FirebaseUtil.getCurrentUserId(), mLikedListString);



        //save subscribed to current user
        final DatabaseReference likedVideoListRef = FirebaseUtil.getLikedListRef();
        likedVideoListRef.push().setValue(likeCampaign.getVideoId());

        FirebaseUtil.getLikedCountRef().runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() != null) {
                    Integer likeCount = currentData.getValue(Integer.class);
                    currentData.setValue(likeCount + 1);
                    return Transaction.success(currentData);
                } else {
                    currentData.setValue(1);
                    return Transaction.success(currentData);
                }
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

            }
        });

        FirebaseUtil.getLikedTodayRef().runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                CountToday tmp = mutableData.getValue(CountToday.class);
                if (tmp == null) {
                    likeCountToday.setCount(likeCountToday.getCount() + 1);
                    mutableData.setValue(likeCountToday);
                    return Transaction.success(mutableData);
                } else {
                    if (tmp.getToday().equals(likeCountToday.getToday())) {
                        likeCountToday.setCount(tmp.getCount() + 1);
                        mutableData.setValue(likeCountToday);
                        return Transaction.success(mutableData);
                    } else {
                        likeCountToday.setCount(1);
                        mutableData.setValue(likeCountToday);
                        return Transaction.success(mutableData);
                    }
                }
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

            }
        });

        try {
            updateCoin(coin);
            Toast.makeText(getContext(), String.format(getString(R.string.nhan_duoc_coin), coin), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveLogLike(LikeCampaign likeCampaign, long coin) {
        if (likeCampaign == null) return;
        if (likeCampaign.getKey().equals("")) return;
        if (likeCampaign.getKey().equals("wrongkey")) return;

        myUserName = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME, "");
        String myUserPhoto = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_PHOTO, "NONE");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference currentLogSubCampaignRef = FirebaseUtil.getLogLikeRef().child(likeCampaign.getKey());
        currentLogSubCampaignRef.push().setValue(new LogLike(user.getUid(), myUserPhoto, likeCampaign.getKey(), myUserName, coin, ServerValue.TIMESTAMP));

    }
}

