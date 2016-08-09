package embedded.korea.ac.kr.emoodchart;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import embedded.korea.ac.kr.emoodchart.api.ApiInterface;

import java.util.Calendar;

public class eMoodChartService extends Service {
    private BroadcastReceiver recv;
    private AlarmManager am;

    private final String ACT_ALARM = "emoodchart.SURVEY_ALARM";
    private final String ACT_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON";
    private final String ACT_BOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED";

    private final int NOTI_ID = 1234;

    private int PI_RESTART = 2;
    private int PI_SURVEY = 4;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            UserInfo user = new UserInfo(this);
            String action = intent.getAction();

            //메모리가 부족하여 재시작된 경우에는 아무것도 하지 않음 (intent == null)
            if (user.isValid() && action != null) {
                if (action.equals(ACT_QUICKBOOT_POWERON) || action.equals(ACT_BOOT_COMPLETE)) {
                    /*
                     * 재부팅 시 RestartReceiver를 통해 서비스가 시작될 경우 작동하는 루틴
                     * 처음 테스트 과정에서는 앱을 실행시켜 코디네이터가 확인할 수 있도록 처리
                     * 그 이외의 경우에는 아무것도 수행하지 않음
                     */

                    SharedPreferences pf = getSharedPreferences("context", MODE_PRIVATE);
                    if (!pf.contains("reboot")) {
                        pf.edit().putBoolean("reboot", true).apply();

                        Intent chk = new Intent(this, StartActivity.class);
                        chk.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(chk);
                    }
                } else if (action.equals(ACT_ALARM)) {
                /*
                 * 알람 시간일 경우 해당 알람을 보여주고 다음 알림을 설정
                 *
                 */
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ApiInterface.genSurveyUrl(user)));
                    PendingIntent pi = PendingIntent.getActivity(this, 0, browserIntent, PendingIntent.FLAG_ONE_SHOT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.noticon)
                            .setContentTitle("설문조사")
                            .setAutoCancel(true)
                            .setContentIntent(intent)
                            .setContentText("오늘의 설문조사를 진행해 주세요");

                    NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    nm.notify(NOTI_ID, builder.build());

                    Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    vib.vibrate(1000);
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (recv == null) {
            // 조도값을 받기 위한 이벤트리시버 설정

            recv = new LightReceiver();
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            registerReceiver(recv, filter);

            // 설문 알람을 위한 설정
            PendingIntent pi = createPendingIntent(PI_SURVEY);

            Calendar target = Calendar.getInstance();
            target.set(Calendar.HOUR_OF_DAY, 21);
            target.set(Calendar.MINUTE, 0);
            target.set(Calendar.SECOND, 0);

            if (Calendar.getInstance().after(target)) //현재 시간이 목표 시간보다 작으면 다음 날 오후 9시로 설정
            {
                target.add(Calendar.DATE, 1);
            }

            am.setRepeating(AlarmManager.RTC_WAKEUP, target.getTime().getTime(), AlarmManager.INTERVAL_DAY, pi);
        }

        // 서비스가 살아있는지 확인하는 heartbeat alarm 설정
        PendingIntent restartIntent = createPendingIntent(PI_RESTART);
        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 15 * 60 * 1000;
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, firstTime, AlarmManager.INTERVAL_FIFTEEN_MINUTES, restartIntent);
    }

    @Override
    public void onDestroy() {
        if (recv != null) unregisterReceiver(recv);
        am.cancel(createPendingIntent(PI_SURVEY));

        super.onDestroy();
    }

    private PendingIntent createPendingIntent(int pid) {
        Intent intent = null;
    	if (pid == PI_RESTART) {
    	    intent = new Intent(this, eMoodChartReceiver.class);
    	} else if (pid == PI_SURVEY) {
    		intent = new Intent(this, eMoodChartService.class);
            intent.setAction(ACT_ALARM);
    	} else {
    	    throw new IllegalArgumentException("Unknown PendingIntent type");
    	}

        return PendingIntent.getService(this, pid, intent, PendingIntent.FLAG_ONE_SHOT);
    }
}
