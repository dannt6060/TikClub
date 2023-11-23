package tikfans.tikfollow.tik.tok.followers.likes;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTubeScopes;

import java.util.Arrays;

import tikfans.tikfollow.tik.tok.followers.likes.model.CountToday;
import tikfans.tikfollow.tik.tok.followers.likes.util.AppUtil;
import tikfans.tikfollow.tik.tok.followers.likes.util.PreferenceUtil;

public class MyChannelApplication extends MultiDexApplication {
    static Context applicationContext;
    public static Uri invitationUrl = null;
    public static GoogleAccountCredential credential;
    private static final String[] SCOPES = {YouTubeScopes.YOUTUBE};

    public static Context getGlobalContext() {
        return applicationContext;
    }
    public static String myChanelId = "";
    public static boolean isVipAccount = false;
    public static CountToday countToday;
    public static CountToday savedCountToday;
    public static CountToday countTodayIP;

    public void onCreate() {
        super.onCreate();
        Log.d("Khang", "MyChannelApplication onCreate");
        applicationContext = getApplicationContext();
        myChanelId = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME, "");
        AppUtil.getServerTime();

        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }
}
