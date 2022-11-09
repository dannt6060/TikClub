package tikfans.tikplus.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;

public class AppUtil {
    public static final int INTENT_REQUEST_CODE_FOR_CREATE_CAMPAIGN = 10;
    public static final String UNFOLLOW_USER_LIST_EXTRA ="unfollow_user_list_extra" ;
    public static final String UNFOLLOW_CHECK_ALL_LIST_USER_NAME = "unfollow_check_all_user_list_extra";
    public static String CAMPAIGN_PATH_STRING_EXTRA = "campaign_path_string_extra";
    public static String DEM_THOI_GIAN_CAMPAIGN_TIME_REQUEST_EXTRA = "campaign_time_extra";
    public static String DEM_THOI_GIAN_CAMPAIGN_COIN_EXTRA = "campaign_coin_extra";
    public static String DEM_THOI_GIAN_CAMPAIGN_TYPE_EXTRA = "campaign_type_extra";
    public static String CREATE_CAMPAIGN_TYPE = "create_campaign_type";
    public static String RUNNING_CAMPAIGN_PATH_STRING_EXTRA = "running_campaign_path_string_extra";
    public static String NUMBER_CAMPAIGN_PATH_STRING_EXTRA = "number_campaign_path_string_extra";
    public static String FORMAT_DATE = "MM/dd/yyyy";
    public static String SELECTED_VIDEO_THUMBNAIL_STRING_EXTRA = "selected_video_extra";
    public static String SELECTED_VIDEO_WEB_LINK_STRING_EXTRA = "video_web_link_extra";
    public static String SELECTED_VIDEO_ID_STRING_EXTRA = "video_id_extra";
    public static String SELECTED_USER_NAME_FOR_CAMPAIGN_EXTRA = "user_name_for_campaign_extra";
    public static String SELECTED_USER_IMG_FOR_CAMPAIGN_EXTRA = "user_img_for_campaign_extra";
    public static String VIP_ACCOUNT_EXTRA = "vip_account_boolean_extra";
    public static boolean isAdminVer = false;
    public static String YOUTUBE_API_KEY = "AIzaSyC1GDxMdWYaxiFzeQZzYrxXPaUo37r1LWc";

    public static int SUB_CAMPAIGN_TYPE = 1;
    public static int VIEW_CAMPAIGN_TYPE = 2;
    public static int LIKE_CAMPAIGN_TYPE = 3;
    public static String TIKTOK_PREFIX_LINK = "https://www.tiktok.com/@";

    //for purchase
    public static final String SKU_HUGE_PACKAGE = "tikfans.hugepackage";
    public static final String SKU_LARGE_PACKAGE = "tikfans.largepackage";
    public static final String SKU_MAX_PACKAGE = "tikfans.maxpackage";
    public static final String SKU_MINI_PACKAGE = "tikfans.minipackage";
    public static final String SKU_SMALL_PACKAGE = "tikfans.smallpackage";
    //for promotion package
    public static final String SKU_MINI_PROMOTE_PACKAGE = "tikfans.minipromopackage";
    public static final String SKU_LARGE_PROMOTE_PACKAGE = "tikfans.largepromopackage";
    public static final String SKU_MAX_PROMOTE_PACKAGE = "tikfans.maxpromopackage";

    public static final String SKU_VIP_MONTHLY = "tikfans.monthly";
    public static final String SKU_VIP_THREE_MONTHS = "tikfans.threemonths";
    public static final String SKU_VIP_YEARLY = "tikfans.yearly";

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityMgr.getActiveNetworkInfo();
        /// if no network is available networkInfo will be null
        return networkInfo != null && networkInfo.isConnected();
    }

    public static void showAlertDialog(Context context, String title, String message) {
        try {
            AlertDialog alertDialog = new AlertDialog.Builder(context).setTitle(title).setMessage(message).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create();
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getVideoURL(String videoId) {
        return TIKTOK_PREFIX_LINK + "user/video/" + videoId;
    }

    public static void getServerTime() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("Khang", "getServerTime start ");
                    String TIME_SERVER = "time1.google.com";
                    NTPUDPClient timeClient = new NTPUDPClient();
                    InetAddress inetAddress = null;
                    inetAddress = InetAddress.getByName(TIME_SERVER);
                    TimeInfo timeInfo = null;
                    timeInfo = timeClient.getTime(inetAddress);
                    long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
                    Log.d("Khang", "return time: " + returnTime);
                    Date serverDate = new Date(returnTime);
                    SecureDate.getInstance().initServerDate(serverDate);
                } catch (UnknownHostException e) {
                    Log.d("Khang", "getServerTime error:  " + e.getMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.d("Khang", "getServerTime error:  " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        boolean found = true;
        try {
            packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {

            found = false;
        }
        return found;
    }

    public static java.lang.String extractYTId(String url) throws MalformedURLException {
        if (url.contains("youtu.be")) {
            java.lang.String[] param = url.split("/");
            java.lang.String id = null;
            if (param.length == 4) {
                id = param[3];
            }
            return id;
        } else {
            java.lang.String query = new URL(url).getQuery();
            java.lang.String[] param = query.split("&");
            java.lang.String id = null;
            for (java.lang.String row : param) {
                java.lang.String[] param1 = row.split("=");
                if (param1[0].equals("v")) {
                    id = param1[1];
                }
            }
            return id;
        }
    }

    public static int convertStringToInteger(String numberString) {
        try {
            int heso = 1;
            if (numberString.contains("K") || numberString.contains("k")) {
                heso = 1000;
            }
            if (numberString.contains("M") || numberString.contains("m")) {
                heso = 1000000;
            }
            if (numberString.contains("B") || numberString.contains("b")) {
                heso = 1000000000;
            }
            numberString = numberString.replace("K", "");
            numberString = numberString.replace("k", "");
            numberString = numberString.replace("M", "");
            numberString = numberString.replace("m", "");
            numberString = numberString.replace("B", "");
            numberString = numberString.replace("b", "");
            float number = Float.parseFloat(numberString) * heso;
            return (int) number;
        } catch (Exception e) {
            return -1;
        }
    }
}
