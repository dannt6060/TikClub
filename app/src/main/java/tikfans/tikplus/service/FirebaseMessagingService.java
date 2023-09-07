package tikfans.tikplus.service;

import static tikfans.tikplus.util.FirebaseUtil.TOKEN_REF;
import static tikfans.tikplus.util.FirebaseUtil.getCurrentUserId;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.RemoteMessage;
import tikfans.tikplus.ManHinhDauTienActivity;
import tikfans.tikplus.R;
import tikfans.tikplus.util.FirebaseUtil;

/**
 * Created by sev_user on 12/24/2016.
 */

public class FirebaseMessagingService
        extends com.google.firebase.messaging.FirebaseMessagingService {
    private SharedPreferences mPre;
    private long lastPushTimes = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            Log.d("Khang", "Message: " + remoteMessage.getNotification().getTitle() + ": " +
                    remoteMessage.getNotification().getBody());
            Intent i = new Intent(this, ManHinhDauTienActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent;
            PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT >= 23) {
                // Create a PendingIntent using FLAG_IMMUTABLE.
                pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
            } else {
                // Existing code that creates a PendingIntent.
                pendingIntent =
                        PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this).setAutoCancel(true)
                            .setContentTitle(remoteMessage.getNotification().getTitle())
                            .setContentText(remoteMessage.getNotification().getBody())
                            .setSmallIcon(R.mipmap.ic_launcher).setContentIntent(pendingIntent);

            builder.setContentIntent(pendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = builder.build();
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            notification.defaults = Notification.DEFAULT_ALL;
            mNotificationManager.notify(111, notification);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("khang", "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        if (getCurrentUserId() != null && getCurrentUserId().length() > 0)  {
            final DatabaseReference currentUserTokenRef = FirebaseUtil.getAccountRef().child(getCurrentUserId()).child(TOKEN_REF);
            currentUserTokenRef.setValue(token);
        }

    }

}
