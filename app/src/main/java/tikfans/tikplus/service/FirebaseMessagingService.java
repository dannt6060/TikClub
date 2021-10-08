package tikfans.tikplus.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import tikfans.tikplus.ManHinhDauTienActivity;
import tikfans.tikplus.R;

/**
 * Created by sev_user on 12/24/2016.
 */

public class FirebaseMessagingService
        extends com.google.firebase.messaging.FirebaseMessagingService {
    private SharedPreferences mPre;
    private long lastPushTimes = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("Khang", "Message: " + remoteMessage.getNotification().getTitle() + ": " +
                remoteMessage.getNotification().getBody());
        Intent i = new Intent(this, ManHinhDauTienActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

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
    }

}
