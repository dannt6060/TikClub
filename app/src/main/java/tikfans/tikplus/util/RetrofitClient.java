package tikfans.tikplus.util;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import tikfans.tikplus.model.ResultUser;
import tikfans.tikplus.model.ResultVideo;

public class RetrofitClient {

    private static Retrofit retrofit = null;

    private static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS);
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static final String BASE_URL = "https://us-central1-tikfans-prod-a3557.cloudfunctions.net/";

    public static ClientRequest getClientRequest() {
        return RetrofitClient.getClient(BASE_URL).create(ClientRequest.class);
    }

    public interface ClientRequest {
        @POST("getUserTikTokVideoList")
        @Headers("content-type: application/json")
        Call<ResultVideo> getListVideoFromUser(@Body JsonObject payload);

        @POST("getTikTokUserInfo{id}")
        @Headers("content-type: application/json")
        Call<ResultUser> getUserInfo(@Path("id") String id, @Body JsonObject payload);
    }
}
