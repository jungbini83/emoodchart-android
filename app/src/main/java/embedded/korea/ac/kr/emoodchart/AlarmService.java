package embedded.korea.ac.kr.emoodchart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

    }
/*
    Vibrator vibrator;
    NotificationManager notificationManager;
    Notification notification;
    PendingIntent pendingIntent;

    @Override
        public void onReceive(Context context, Intent intent) {//N초마다 이것을 실행
            vibrator=(Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notification = new Notification(R.drawable.ic_launcher, "설문조사 할 시간입니다.", System.currentTimeMillis());
            notification.flags = Notification.FLAG_AUTO_CANCEL;

            pendingIntent= PendingIntent.getActivity(context, 0, new Intent(context, TestActivity.class), PendingIntent.FLAG_ONE_SHOT);
            notification.setLatestEventInfo(context, "설문조사 알림", "설문조사를 실시해주세요.",pendingIntent);
            notificationManager.notify(1234, notification);

            vibrator.vibrate(2*1000);
            Toast.makeText(context,"설문조사 할 시간입니다." , Toast.LENGTH_SHORT).show();
    }
*/
}
