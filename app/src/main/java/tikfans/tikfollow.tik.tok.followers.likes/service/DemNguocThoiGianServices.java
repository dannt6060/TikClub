package tikfans.tikfollow.tik.tok.followers.likes.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import tikfans.tikfollow.tik.tok.followers.likes.MainActivity;
import tikfans.tikfollow.tik.tok.followers.likes.R;
import tikfans.tikfollow.tik.tok.followers.likes.util.AppUtil;
import tikfans.tikfollow.tik.tok.followers.likes.util.CountDownTimer;
import tikfans.tikfollow.tik.tok.followers.likes.util.PreferenceUtil;
import tikfans.tikfollow.tik.tok.followers.likes.util.RemoteConfigUtil;

public class DemNguocThoiGianServices extends Service {

    private WindowManager mWindowManager;
    private View mFloatingView, mReturnAppView;
    private CountDownTimer mCountDownTimer;
    private long timerCount = 30;
    private TextView mTxtTimer, mTxtCoin, mTxtInstruction;
    private Button mBtnReturnApp;
    private LinearLayout mButtonLinearLayout, mTimerLayout;
//    private RelativeLayout mCoverLayout;
    private WindowManager.LayoutParams params;
    private int campaignType = AppUtil.SUB_CAMPAIGN_TYPE;

    private IntentFilter mFilter;
    private InnerRecevier mRecevier;
    private Context mContext;
    private ProgressBar mProgressBar;


    public DemNguocThoiGianServices() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                timerCount = 60;
                timerCount = PreferenceUtil.getIntPref(AppUtil.DEM_THOI_GIAN_CAMPAIGN_TIME_REQUEST_EXTRA, 60);
                long coin = PreferenceUtil.getLongPref(AppUtil.DEM_THOI_GIAN_CAMPAIGN_COIN_EXTRA, FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_LIKE_COIN_RATE_KEY) + timerCount);
                campaignType = PreferenceUtil.getIntPref(AppUtil.DEM_THOI_GIAN_CAMPAIGN_TYPE_EXTRA, AppUtil.SUB_CAMPAIGN_TYPE);
                mTxtCoin.setText(String.valueOf(coin));
                mTxtTimer.setText(String.valueOf(timerCount));
                initCountdownTimer();
            }
        }, 500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mCountDownTimer == null) {
                    initCountdownTimer();
                }
                mCountDownTimer.start();
            }
        }, 5000);

        return super.onStartCommand(intent, flags, startId);
    }

    private void initCountdownTimer() {
        mCountDownTimer =
                new CountDownTimer(((long) (this.timerCount * 1000)), 1000) {
                    public void onTick(long millisUntilFinished) {
                        mTxtTimer.setText(
                                String.valueOf(((int) (millisUntilFinished / 1000))));
                        int progressPercentage = (int) ((millisUntilFinished) *100 /mCountDownTimer.getmMillisInFuture());
                        mProgressBar.setProgress(progressPercentage);
                    }

                    public void onFinish() {
                        try {
                            mWindowManager.removeView(mFloatingView);
                            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                            mWindowManager.addView(mReturnAppView, params);
                            if (campaignType == AppUtil.SUB_CAMPAIGN_TYPE) {
                                mTxtInstruction.setText(R.string.sub_instruction3);
                            } else {
                                mTxtInstruction.setText(R.string.like_instruction3);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
    }

    @Override
    public void onCreate() {
        Log.d("Khang", "Floating service oncreate");
        super.onCreate();
        //Inflate the floating view layout we created
        mContext = getApplicationContext();
        mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        mReturnAppView = LayoutInflater.from(this).inflate(R.layout.layout_return_app_view, null);

        mTxtTimer = mFloatingView.findViewById(R.id.txt_time_required);
        mTxtCoin = mFloatingView.findViewById(R.id.txt_coin_reward);
        mTxtInstruction = mReturnAppView.findViewById(R.id.sub_instruction);
        mButtonLinearLayout = mReturnAppView.findViewById(R.id.overlay_button_layout);
        mTimerLayout = mFloatingView.findViewById(R.id.timer_layout);
//        mCoverLayout = mFloatingView.findViewById(R.id.cover);
        mProgressBar = mFloatingView.findViewById(R.id.countdown_progressbar);
        mProgressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
//        mCoverLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (campaignType == AppUtil.SUB_CAMPAIGN_TYPE) {
//                    Toast.makeText(DemNguocThoiGianServices.this, getString(R.string.sub_instruction4), Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(DemNguocThoiGianServices.this, getString(R.string.like_instruction4), Toast.LENGTH_LONG).show();
//                }
//            }
//        });

        mRecevier = new InnerRecevier();
        startWatch();


        //Add the view to the window.
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        //Specify the view position
        params.gravity = Gravity.BOTTOM | Gravity.FILL_HORIZONTAL;        //Initially view will be added to top-left corner
//        params.x = 1000;
//        params.y = 0;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);
        mFloatingView.setVisibility(View.VISIBLE);

        //Open the application on thi button click
        mBtnReturnApp = mReturnAppView.findViewById(R.id.btn_back);
        long delayTime = 1000;
        if (campaignType == AppUtil.LIKE_CAMPAIGN_TYPE) {
            delayTime = 3000;
        }
        long finalDelayTime = delayTime;
        mBtnReturnApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               new Handler().postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       returnApp();
                   }
               }, finalDelayTime);
            }
        });

        //Drag and move floating view using user's touch action.
        mReturnAppView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;
//                        initialX = v.getLayoutParams().width;
//                        initialY = v.getLayoutParams().height;
                        Log.d("khang", "down: " + initialX + " / " + initialY);


                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);

                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX - (int) (event.getRawX() - initialTouchX);
                        params.y = initialY - (int) (event.getRawY() - initialTouchY);
                        Log.d("khang", "move: " + params.x + " / " + params.y);

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mReturnAppView, params);
                        return true;
                }
                return false;
            }
        });


        Log.d("Khang", "Floating service oncreate end");
    }

    private void returnApp() {
        //Open the application  click.
        Intent intent = new Intent(DemNguocThoiGianServices.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //close the service and remove view from the view hierarchy
        stopSelf();
    }

    /**
     * Detect if the floating view is collapsed or expanded.
     *
     * @return true if the floating view is collapsed.
     */

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
            stopWatch();
            if (mFloatingView != null && mFloatingView.isAttachedToWindow())
                mWindowManager.removeView(mFloatingView);
            if (mReturnAppView != null && mReturnAppView.isAttachedToWindow())
                mWindowManager.removeView(mReturnAppView);
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
                mCountDownTimer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class InnerRecevier extends BroadcastReceiver {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final String SYSTEM_DIALOG_REASON_POWER_KEY = "dream";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";


        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    Log.e("Khang", "action:" + action + ",reason:" + reason);
                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY) || reason.equals(SYSTEM_DIALOG_REASON_POWER_KEY) || reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                        returnApp();
                    }
                }
            }
        }
    }

    public void startWatch() {
        if (mRecevier != null) {
            mContext.registerReceiver(mRecevier, mFilter);
        }
    }


    public void stopWatch() {
        if (mRecevier != null) {
            mContext.unregisterReceiver(mRecevier);
        }
    }


}
