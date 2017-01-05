package embedded.korea.ac.kr.emoodchart.services;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import embedded.korea.ac.kr.emoodchart.MainActivity;
import embedded.korea.ac.kr.emoodchart.R;
import embedded.korea.ac.kr.emoodchart.UserInfo;
import embedded.korea.ac.kr.emoodchart.api.APIHelper;
import embedded.korea.ac.kr.emoodchart.light.*;

import java.util.*;

public class EMCService extends Service implements SensorEventListener {
    private BroadcastReceiver mLightRecv;

    static final String ACT_ALARM = "emoodchart.action.SURVEY_ALARM";
    static final String ACT_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON";
    static final String ACT_BOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED";
    static final String ACT_SCREEN_ON = "emoodchart.action.SCREEN_ON";
    public static final String ACT_NOTIFY_PREDICTION = "emoodchart.action.NOTIFY_PREDICTION";

    private static final int NOTI_ID = 1234;
    private static final int PI_SURVEY = 4;

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
                switch(action) {
                    case ACT_QUICKBOOT_POWERON:
                    case ACT_BOOT_COMPLETE:
                    {
                        /*
                         * 재부팅 시 RestartReceiver를 통해 서비스가 시작될 경우 작동하는 루틴
                         * 처음 테스트 과정에서는 앱을 실행시켜 코디네이터가 확인할 수 있도록 처리
                         * 그 이외의 경우에는 아무것도 수행하지 않음
                         */
                        SharedPreferences pf = getSharedPreferences("appstatus", MODE_PRIVATE);
                        if (pf.getBoolean("rebootable", false)) {
                            pf.edit().putBoolean("rebootable", true).apply();

                            Intent chk = new Intent(this, MainActivity.class);
                            chk.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(chk);
                        }
                        break;
                    }
                    case ACT_ALARM:
                    {
                        /*
                         * 알람 시간일 경우 해당 알람을 띄움
                         *
                         */
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(APIHelper.genSurveyUrl(user)));
                        PendingIntent pi = PendingIntent.getActivity(this, 0, browserIntent, PendingIntent.FLAG_ONE_SHOT);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.noticon)
                                .setContentTitle("설문조사")
                                .setAutoCancel(true)
                                .setContentIntent(pi)
                                .setContentText("오늘의 설문조사를 진행해 주세요");

                        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                        nm.notify(NOTI_ID, builder.build());


                        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                        if (am.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                            Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                            vib.vibrate(1000);
                        }
                        break;
                    }
                    case ACT_SCREEN_ON:
                    {
                        SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
                        Sensor lightSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);

                        if (lightSensor != null) {
                            sm.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
                        }
                        break;
                    }
                    case ACT_NOTIFY_PREDICTION:
                    {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(APIHelper.genDashbardUrl(user)));
                        PendingIntent pi = PendingIntent.getActivity(this, 0, browserIntent, PendingIntent.FLAG_ONE_SHOT);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.noticon)
                                .setContentTitle("데이터 분석 결과 확인")
                                .setAutoCancel(true)
                                .setContentIntent(pi)
                                .setContentText("수집된 데이터 분석 결과를 확인하세요");

                        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                        nm.notify(NOTI_ID, builder.build());


                        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                        if (am.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                            Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                            vib.vibrate(1000);
                        }
                        break;
                    }
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //화면이 켜진 순간만 포착하기 위해 한번 수집한 이후에는 이벤트를 다시 받지 않기로 함
        SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        sm.unregisterListener(this);

        new LightUploader(this, event.values[0]).start(new UserInfo(this));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);

        if (mLightRecv == null) {
            // 조도값을 받기 위한 이벤트리시버 설정
            mLightRecv = new EMCScreenEventReceiver();
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            registerReceiver(mLightRecv, filter);

            Intent intent = new Intent(this, EMCService.class);
            intent.setAction(ACT_ALARM);

            PendingIntent pi = PendingIntent.getService(this, PI_SURVEY, intent, PendingIntent.FLAG_CANCEL_CURRENT);

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
    }

    @Override
    public void onDestroy() {
        if (mLightRecv != null) unregisterReceiver(mLightRecv);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, EMCService.class);
        intent.setAction(ACT_ALARM);
        PendingIntent pi = PendingIntent.getService(this, PI_SURVEY, intent, PendingIntent.FLAG_NO_CREATE);
        if (pi != null) am.cancel(pi);

        SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        sm.unregisterListener(this);

        super.onDestroy();
    }

}
