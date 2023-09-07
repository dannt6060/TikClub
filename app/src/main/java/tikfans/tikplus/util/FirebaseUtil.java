
/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tikfans.tikplus.util;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtil {
    private static final String APP_REF = "myChannelApp";
    public static final String SUBSCRIBED_REF = "subscribed";
    public static final String SUBSCRIBED_LIST_REF = "list";
    public static final String SUBSCRIBED_COUNT_REF = "count";
    private static final String COUNT_TODAY = "countToday";
    private static final String COUNT_TODAYIP = "countTodayIP";
    private static final String CHAT = "chat";
    private static final String CHAT_ALL = "all";
    private static final String REPORT_REF = "report";
    public static String CAMPAIGN_LAST_CHANGE_TIME_STAMP = "lasTime";
    public static String COUNTRY_CODE_REF = "CCode";
    public static String EMAIL_REF = "email";
    public static String PHOTO_REF = "photo";
    public static String TIKTOK_UID = "tikUId";
    public static String TIKTOK_USER_NAME = "userName";
    public static String TOKEN_REF = "token";
    public static String STATIC_REF = "static";
    public static String PURCHASE_TIMES_REF = "purchaseTimes";
    public static String VIP_ACCOUNT_REF = "vipAccount";
    public static String IS_BLOCKED_REF = "isBlocked";
    public static String SUBSCRIPTION_INFO = "subscriptionAndroid";
    public static String USUB_PURCHASE = "verifyPurchaseAndroid";
    public static String LOGSUB = "logSub";
    public static String USUB_LAST_SIGN_IN_AT = "lastSignInAt";
    public static String REFERRED_BY = "referredBy";
    public static String SUB_CAMPAIGNS = "subCampaigns";
    public static String CAMPAIGNS_CURRENT_USER_REF = "campaigns";
    public static String VIEW_CAMPAIGNS = "viewCampaigns";
    public static String SUB_COIN_REF = "subCoin";
    public static String UN_SUB_COUNT = "unSubCount";
    public static String REFERRED_TO = "referredTo";
    public static String LOGVIEW = "logView";
    public static String LOGADSREWARD = "logAdsReward2";

    //for like
    public static String UN_LIKE_REF = "unLikeCount";
    public static final String LIKED_REF = "liked";
    public static final String LIKED_LIST_REF = "list";
    public static final String LIKED_COUNT_REF = "count";
    public static final String LIKED_TODAY_REF = "today";
    public static String LIKE_CAMPAIGNS = "likeCampaigns";
    public static String LOGLIKE = "logLike";

    public static DatabaseReference getBaseRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public static String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    public static DatabaseReference getCountTodayRef() {
        if (getCurrentUserId() != null) {
            return getBaseRef().child(COUNT_TODAY).child(getCurrentUserId());
        }
        return null;
    }

    public static DatabaseReference getCurrentUserRef() {
        String uid = getCurrentUserId();
        if (uid != null) {
            Log.d("khang", "myID: " + uid);
            return getAccountRef().child(getCurrentUserId());
        }
        return null;
    }

//    public static DatabaseReference getSubLikedCurrentUserRef() {
//        String uid = getCurrentUserId();
//        if (uid != null) {
//            return getSubLikedRef().child(getCurrentUserId());
//        }
//        return null;
//    }


    public static DatabaseReference getCampaignCurrentUser() {
        return getCurrentUserRef().child(CAMPAIGNS_CURRENT_USER_REF);
    }

    public static DatabaseReference getLogAdsRewardCurrentUserRef() {
        return getCurrentUserRef().child(LOGADSREWARD);
    }

    public static DatabaseReference getSubCampaignsRef() {
        return getBaseRef().child(SUB_CAMPAIGNS);
    }

    public static DatabaseReference getLogSubRef() {
        return getBaseRef().child(LOGSUB);
    }


    public static DatabaseReference getUSubPurchaseRef() {
        return getBaseRef().child(USUB_PURCHASE);
    }

    public static DatabaseReference getReferredToCurrentUser() {
        return getCurrentUserRef().child(REFERRED_TO);
    }

    public static DatabaseReference getAccountRef() {
        return getBaseRef().child("account");
    }

//    public static DatabaseReference getSubLikedRef() {
//        return getBaseRef().child(SUB_LIKED_REF);
//    }

    public static DatabaseReference getCoinCurrentAccountRef() {
        return getCurrentUserRef().child(SUB_COIN_REF);
    }


    public static DatabaseReference getPurchaseTimeCurrentAccountRef() {
        return getCurrentUserRef().child(STATIC_REF).child(PURCHASE_TIMES_REF);
    }

//    public static DatabaseReference getUnSubCountRef() {
//        return getSubLikedCurrentUserRef().child(UN_SUB_COUNT);
//    }


    public static DatabaseReference getOldSubscribedListRef() {
        return getCurrentUserRef().child(SUBSCRIBED_REF).child(SUBSCRIBED_LIST_REF);
    }

    public static DatabaseReference getSubscribedListRef() {
        return getCurrentUserRef().child(SUBSCRIBED_REF).child(SUBSCRIBED_LIST_REF);
    }

    public static DatabaseReference getUnSubscribedListRef() {
        return getCurrentUserRef().child(REPORT_REF);
    }

    public static DatabaseReference getSubscribedCountRef() {
        return getCurrentUserRef().child(SUBSCRIBED_REF).child(SUBSCRIBED_COUNT_REF);
    }



    public static DatabaseReference getLikedListRef() {
        return getCurrentUserRef().child(LIKED_REF).child(LIKED_LIST_REF);
    }

    public static DatabaseReference getLikedCountRef() {
        return getCurrentUserRef().child(LIKED_REF).child(LIKED_COUNT_REF);
    }


    public static DatabaseReference getReportSubRef(String uid) {
        return getCurrentUserRef().child(uid).child(REPORT_REF);
    }

    public static DatabaseReference getLikeCampaignsRef() {
        return getBaseRef().child(LIKE_CAMPAIGNS);
    }

    public static DatabaseReference getLogLikeRef() {
        return getBaseRef().child(LOGLIKE);
    }

    public static DatabaseReference getVipAccountRef() {
        return getCurrentUserRef().child(STATIC_REF).child(VIP_ACCOUNT_REF);
    }

    public static DatabaseReference getSubscriptionInfoRef() {
        return getCurrentUserRef().child(SUBSCRIPTION_INFO);
    }

    public static DatabaseReference getChatRoomAllRef() {
        return getBaseRef().child(CHAT).child(CHAT_ALL);
    }

    public static DatabaseReference getUserNameRef(String uid) {
        return getAccountRef().child(uid).child(TIKTOK_USER_NAME);
    }

    public static DatabaseReference getUserNameByUID(String uid) {
        return getAccountRef().child(uid).child(TIKTOK_USER_NAME);
    }

    public static DatabaseReference getUserPhotoByUID(String uid) {
        return getAccountRef().child(uid).child(PHOTO_REF);
    }
}
