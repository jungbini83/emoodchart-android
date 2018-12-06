package embedded.korea.ac.kr.emoodchart.api.push;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import embedded.korea.ac.kr.emoodchart.BrowserActivity;
import embedded.korea.ac.kr.emoodchart.R;
import embedded.korea.ac.kr.emoodchart.StatusActivity;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = FirebaseMessagingService.class.getSimpleName();

    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.i(TAG, "onMessageReceived");

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("content");
        String sound = data.get("sound");

        sendPushNotification(title, message + ", sound: " + sound);
    }

    private void sendPushNotification(String title, String message) {
        System.out.println("received message : " + message);

        Intent intent = new Intent(this, BrowserActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_dialog_info))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

//        intent.putExtra("data", message);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_SINGLE_TOP);
//
//        getApplicationContext().startActivity(intent);
    }
}
