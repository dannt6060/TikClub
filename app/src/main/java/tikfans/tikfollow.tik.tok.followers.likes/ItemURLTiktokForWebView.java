package tikfans.tikfollow.tik.tok.followers.likes;

import android.util.Log;

import java.util.regex.Pattern;


public class ItemURLTiktokForWebView {
    public static final int URL_TYPE_INVALID = 0;
    public static final int URL_TYPE_USER = 1;
    public static final int URL_TYPE_VIDEO = 2;
    private static final String TAG = ItemURLTiktok.class.getSimpleName();
    private String baseUrl;
    private String userName;
    private int TYPE_URL = URL_TYPE_INVALID;
    final String regex_user = "^http(s)?:\\/\\/((www.)?)((m.)?)tiktok.com\\/{1}@{1}([^.])([a-z0-9]*[._]*[a-z0-9]*)*(\\/?)$";
    final Pattern pattern = Pattern.compile(regex_user, Pattern.MULTILINE);

    private ClientTikTokListener listener;

    public interface ClientTikTokListener {
        void onLoading();

        void onError(int code, String mess);

        void onInvalidLink();

        void onCheckLinkDone();
    }

    public ItemURLTiktokForWebView(String url, ClientTikTokListener listener) {
        this.listener = listener;
        if (listener != null) {
            listener.onLoading();
        }
        if (url == null)
            return;
        url = url.trim();
        this.baseUrl = url;

        if (!url.contains("tiktok.com/")) {
            setTYPE_URL(URL_TYPE_INVALID);
            if (listener != null) {
                listener.onInvalidLink();
            }
            Log.e(TAG, "ItemURLTiktok: type invalid");
        }


        if (Pattern.matches(regex_user, url)) {
            setTYPE_URL(URL_TYPE_USER);
            this.userName = url.split("@")[1];
            while (userName.endsWith("/")) {
                userName = userName.substring(0, userName.length() - 1);
            }
            Log.e(TAG, "ItemURLTiktok: type user +" + userName);
        } else if (url.contains("/video/") || url.contains("vt.tiktok.com/")) {
            setTYPE_URL(URL_TYPE_VIDEO);
            Log.e(TAG, "ItemURLTiktok: type video +" + url);

        } else {
            setTYPE_URL(URL_TYPE_INVALID);
            if (listener != null) {
                listener.onInvalidLink();
            }
            Log.e(TAG, "ItemURLTiktok: type invalid");
        }
        if (listener != null) {
            listener.onCheckLinkDone();
        }

    }

    public String getUserName() {
        return userName;
    }

    public int getTYPE_URL() {
        return TYPE_URL;
    }

    public void setTYPE_URL(int TYPE_URL) {
        this.TYPE_URL = TYPE_URL;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

}
