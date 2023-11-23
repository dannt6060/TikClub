package tikfans.tikfollow.tik.tok.followers.likes;

import android.util.Log;

import com.google.gson.JsonObject;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tikfans.tikfollow.tik.tok.followers.likes.model.ResultUser;
import tikfans.tikfollow.tik.tok.followers.likes.model.ResultVideo;
import tikfans.tikfollow.tik.tok.followers.likes.util.RetrofitClient;

public class ItemURLTiktok {
    public static final int URL_TYPE_INVALID = 0;
    public static final int URL_TYPE_USER = 1;
    public static final int URL_TYPE_POST = 2;
    private static final String TAG = ItemURLTiktok.class.getSimpleName();
    private String baseUrl;
    private String userName;
    private int TYPE_URL = URL_TYPE_INVALID;
    private RetrofitClient.ClientRequest clientRequest;
    final String regex_user = "^http(s)?:\\/\\/((www.)?)((m.)?)tiktok.com\\/{1}@{1}([^.])([a-z0-9]*[._]*[a-z0-9]*)*(\\/?)$";
    final Pattern pattern = Pattern.compile(regex_user, Pattern.MULTILINE);

    private ClientTikTokListener listener;

    public interface ClientTikTokListener {
        void onLoading();

        void onReceivedListVideo(ResultVideo result);

        void onReceivedUserInfo(ResultUser result);

        void onError(int code, String mess);

        void onInvalidLink();

        void onCheckLinkDone();
    }

    public ItemURLTiktok(String url, ClientTikTokListener listener) {
        this.listener = listener;
        if (listener != null) {
            listener.onLoading();
        }
        if (url == null)
            return;
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
        clientRequest = RetrofitClient.getClientRequest();
    }

    public void getListVideoFromUser() {
        Log.d("khang", "get list video from user");
        if (this.getTYPE_URL() == URL_TYPE_USER && listener != null) {
            listener.onLoading();
            JsonObject payload = new JsonObject();
            JsonObject user = new JsonObject();
            user.addProperty("uniqueId", getUserName());
            payload.add("data", user);
            clientRequest.getListVideoFromUser(payload).enqueue(new Callback<ResultVideo>() {
                @Override
                public void onResponse(Call<ResultVideo> call, Response<ResultVideo> response) {
                    //Log.e(TAG, "onResponse: " + response.body().toString());
                    if (response.isSuccessful()) {
                        listener.onReceivedListVideo(response.body());
                    } else {
                        listener.onError(0, "Server error!");
                    }
                }

                @Override
                public void onFailure(Call<ResultVideo> call, Throwable t) {
                    listener.onError(400, t.getMessage());
                }
            });
        }
    }

    int tried = 0;


    public void getUserInfo() {
        if (this.getTYPE_URL() == URL_TYPE_USER && listener != null) {

            listener.onLoading();
            JsonObject payload = new JsonObject();
            JsonObject user = new JsonObject();
            user.addProperty("username", getUserName());
            payload.add("data", user);
            clientRequest.getUserInfo(tried == 0 ? "" : tried <= 2 ? String.valueOf(tried) : "", payload).enqueue(new Callback<ResultUser>() {
                @Override
                public void onResponse(Call<ResultUser> call, Response<ResultUser> response) {
                    //Log.e(TAG, "onResponse: " + response.body().toString());
                    tried = 0;
                    if (response.isSuccessful()) {
                        listener.onReceivedUserInfo(response.body());
                    } else {
                        listener.onError(0, "Server error!");
                    }
                }

                @Override
                public void onFailure(Call<ResultUser> call, Throwable t) {
                    listener.onError(400, t.getMessage());
                    if(tried<=2){
                        tried++;
                        getUserInfo();
                    }else {
                        tried = 0;
                    }
                }
            });
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
