package tikfans.tikplus.util;

import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import tikfans.tikplus.MyChannelApplication;

public class PreferenceUtil {
    public static final String FIRST_SIGN_IN_TIME = "first_sign_in_time";
    public static final String IS_LIST_VIDEO_FROM_CURRENT_USER = "is_list_video_from_current_user";
    public static final String TIKTOK_USER_NAME_FOR_CAMPAIGN = "user_name_for_campaign" ;
    public static final String TIKTOK_USER_PHOTO_FOR_CAMPAIGN = "user_photo_for_campaign";
    public static String SUBSCRIBED_LIST = "subscribed_list";
    public static String LIKED_LIST = "liked_list";
    public static final String LAST_CLOSE_PROMOTE_TIME = "last_close_promote_time" ;
    public static final String SUB_BREAK_ADS = "sub_break_ads";
    public static final String TIME_COUNTER_SUBSCRIBED = "time_counter_subscribed";
    public static final String TIME_COUNTER_LIKED = "time_counter_liked";
    public static final String LAST_LIMIT_SUB_TIME = "last_limit_sub_time";
    public static final String SUB_TODAY = "sub_today";
    public static final String TIKTOK_USER_NAME = "user_name";
    public static final String TIKTOK_USER_PHOTO = "user_photo";
    public static String RECENT_VIDEO_LIST_PREF = "recent_video_list";
    public static String VIDEO_VIEWED_TODAY_LIST_PREF = "video_viewed_today_list";
    public static String VIDEO_VIEWED_LAST_DAY_STRING_PREF = "video_viewed_lastday_string";
    public static String APP_CONFIG_VER_KEY = "app_config_ver";
    public static String COIN_RATE_KEY = "coin_rate";
    public static String DAILY_BONUS_KEY = "daily_bonus";
    public static String LIMIT_VIEW_KEY = "limit_view";
    public static String LIMIT_TIME_KEY = "limit_time";
    public static String PACKAGE_APP_KEY = "package_app";
    public static String PURCHA1_KEY = "purcha1";
    public static String PURCHA2_KEY = "purcha2";
    public static String PURCHA3_KEY = "purcha3";
    public static String PURCHA4_KEY = "purcha4";
    public static String VIDEO_AWARD_KEY = "video_award";
    public static String VERSION_APP_KEY = "version_app";
    public static String LAST_BONUS_DAY = "last_bonus_day";
    public static String SAVE_DAY = "save_day";
    public static String VIEW_TODAY = "view_today";
    public static String LAST_LIMIT_TIME = "last_limit_time";
    public static String IS_FTU = "is_ftu";
    public static String IS_RATED = "is_rate";
    public static String REFERRED_BY = "referred_by";
    public static String REFERRED_REWARDED = "referred_rewared";
    public static String LAST_CHECK_SUB_LIST_TIME = "last_check_sub_list_time";
    public static String LAST_CHECK_LIKED_LIST_TIME = "last_check_liked_list_time";
    public static String FORCE_CHECK_SUB = "force_check_sub";
    public static String FORCE_CHECK_LIKE = "force_check_like";
    public static String INVITE_FRIEND_LINK = "invite_friend_link";
    public static String FIRST_TIME_NOT_SUB = "first_time_not_sub";


    public static void clearPref() {
        Editor shareEditor = PreferenceManager.getDefaultSharedPreferences(MyChannelApplication
                                                                                   .getGlobalContext())
                .edit();
        shareEditor.clear();
        shareEditor.apply();
    }

    public static void saveStringPref(String key, String value) {
        Editor shareEditor = PreferenceManager.getDefaultSharedPreferences(MyChannelApplication.getGlobalContext()).edit();
        shareEditor.putString(key, value);
        shareEditor.apply();
    }

    public static String getStringPref(String key, String defValue) {
        return PreferenceManager.getDefaultSharedPreferences(MyChannelApplication.getGlobalContext()).getString(key, defValue);
    }

    public static int getIntPref(String key, int defValue) {
        return PreferenceManager.getDefaultSharedPreferences(MyChannelApplication.getGlobalContext()).getInt(key, defValue);
    }

    public static void saveIntPref(String key, int value) {
        Editor shareEditor = PreferenceManager.getDefaultSharedPreferences(MyChannelApplication.getGlobalContext()).edit();
        shareEditor.putInt(key, value);
        shareEditor.apply();
    }

    public static long getLongPref(String key, long defValue) {
        return PreferenceManager.getDefaultSharedPreferences(MyChannelApplication.getGlobalContext()).getLong(key, defValue);
    }

    public static void saveLongPref(String key, long value) {
        Editor shareEditor = PreferenceManager.getDefaultSharedPreferences(MyChannelApplication.getGlobalContext()).edit();
        shareEditor.putLong(key, value);
        shareEditor.apply();
    }

    public static boolean getBooleanPref(String key, boolean defValue) {
        return PreferenceManager.getDefaultSharedPreferences(MyChannelApplication.getGlobalContext()).getBoolean(key, defValue);
    }

    public static void saveBooleanPref(String key, boolean value) {
        Editor shareEditor = PreferenceManager.getDefaultSharedPreferences(MyChannelApplication.getGlobalContext()).edit();
        shareEditor.putBoolean(key, value);
        shareEditor.apply();
    }
}
