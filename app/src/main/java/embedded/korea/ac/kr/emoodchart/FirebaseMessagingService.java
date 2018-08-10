package embedded.korea.ac.kr.emoodchart;

import android.content.Intent;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";

    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map<String, String> resData = remoteMessage.getData();
        String content = resData.get("content");

        sendPushNotification(content);
    }

    private void sendPushNotification(String message) {
        System.out.println("received message : " + message);

        Intent intent = new Intent(this, StatusActivity.class);
        intent.putExtra("data", message);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_SINGLE_TOP);

        getApplicationContext().startActivity(intent);
    }
}
