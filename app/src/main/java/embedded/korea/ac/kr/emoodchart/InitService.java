package embedded.korea.ac.kr.emoodchart;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class InitService extends Service {
    BroadcastReceiver recv;
    AlarmManager am;

    int nid = 124253;
    
    private final int INTENT_RESTART = 0;
    private final int INTENT_CHKALARM = 1;
    private final int INTENT_ALARM = 2;

    public InitService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || recv == null) init();
        else parseAction(intent);

        return START_STICKY;
    }

    private void parseAction(Intent intent) {
        SharedPreferences pf = getSharedPreferences("setting", MODE_PRIVATE);
        int userId = pf.getInt("userId", 0);
        int prjId = pf.getInt("projectId",0);
        int instId = pf.getInt("instId",0);
        String hash = pf.getString("hash","");

        if (userId == 0) return;

        //테스트 등록
               
        String act = intent.getAction();
        if (act == null) return;
        if (!pf.getBoolean("chk_reboot", false) && (act.equals("android.intent.action.QUICKBOOT_POWERON") || act.equals("android.intent.action.BOOT_COMPLETED"))) {
            pf.edit().putBoolean("chk_reboot", true).putInt("testAlarmCnt", 0).apply();
            
            Toast.makeText(this, "재부팅시 서비스 시작 확인되었으며 알림 테스트를 진행합니다.", Toast.LENGTH_SHORT).show();
            
            //알람 확인
            registerCheckAlarm();
        }
 
        if (act.equals("ACTION.SET_SURVEY_ALARM")) {
        	am.cancel(createPendingIntent(INTENT_ALARM));
            notifySurvey(userId,prjId,instId,hash);
            registerSurveyAlarm(userId,prjId,instId);
        }
        if (act.equals("ACTION.CHECK_ALARM"))
        {
        	int cnt = pf.getInt("testAlarmCnt", 0);
        	pf.edit().putInt("testAlarmCnt", ++cnt).apply();
        	
        	Log.v("teemo","alarm confirmed");
        	
        	am.cancel(createPendingIntent(INTENT_CHKALARM));
        	if(cnt==2)
        	{
        		Toast.makeText(this, "테스트가 완료되었습니다", Toast.LENGTH_SHORT).show();
        		registerSurveyAlarm(userId,prjId,instId);
        		
        	}
        	else
        	{
        		notifyTest(userId);
        		registerCheckAlarm();
        	}        	
        }
    }
    
    private void notifyTest(int userId) {
    	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://fierce-sea-3163-395.herokuapp.com/user/" + userId + "/daily/survey"));
        PendingIntent pi = PendingIntent.getActivity(this, 0, browserIntent, PendingIntent.FLAG_ONE_SHOT);
        
    	notify("테스트", "설문조사 테스트입니다.", pi);
    	
    	Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vib.vibrate(1000);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        if (am == null) {
            am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        }

        if (recv == null) {
            SharedPreferences pref = getSharedPreferences("setting", MODE_PRIVATE);

            int userId = pref.getInt("userId", 0);
            int prjId = pref.getInt("projectId",0);
            int instId = pref.getInt("instId",0);

            if (userId != 0) {
                recv = new LightReceiver();
                IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
                registerReceiver(recv, filter);
                registerSurveyAlarm(userId,prjId,instId);
                
                am.cancel(createPendingIntent(INTENT_RESTART));
                registerRestartAlarm();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (recv != null) unregisterReceiver(recv);

        am.cancel(createPendingIntent(INTENT_ALARM));

        super.onDestroy();
    }

    private void registerRestartAlarm() {
        PendingIntent restartIntent = createPendingIntent(INTENT_RESTART);

        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 15 * 60 * 1000;
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, firstTime, AlarmManager.INTERVAL_FIFTEEN_MINUTES, restartIntent);
    }
    
    private PendingIntent createPendingIntent(int type) {
    	if (type == INTENT_RESTART) {
    		Intent intent = new Intent(this, RestartReceiver.class);
    		return PendingIntent.getBroadcast(this, 0, intent, 0);
    	} else if (type == INTENT_ALARM) {
    		Intent intent = new Intent(this, InitService.class);
            intent.setAction("ACTION.SET_SURVEY_ALARM");

            return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
    	} else if (type == INTENT_CHKALARM) {
    		Intent i = new Intent(this, InitService.class);
            i.setAction("ACTION.CHECK_ALARM");
            return PendingIntent.getService(this, 0, i, PendingIntent.FLAG_ONE_SHOT);
    	} else {
    		return null;
    	}
    }
    
    private void notify(String title, String text, PendingIntent intent) {
    	NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.noticon)
        .setContentTitle(title)
        .setAutoCancel(true)
        .setContentIntent(intent)
        .setContentText(text);
    	
    	NotificationManagerCompat nmc = NotificationManagerCompat.from(this);
        nmc.notify(nid, builder.build());
    }

    public void notifySurvey(int userId, int prjId, int instId,String hash) {
    	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://fierce-sea-3163-395.herokuapp.com/inst/"+instId+"/project/"+prjId+"/user/"+userId+"/survey/daily?hash="+hash));
        PendingIntent pi = PendingIntent.getActivity(this, 0, browserIntent, PendingIntent.FLAG_ONE_SHOT);
        
    	notify("설문조사", "오늘의 설문조사를 진행해 주세요", pi);

        Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vib.vibrate(1000);
    }

    private void registerSurveyAlarm(int userId, int prjId, int instId) {
        PendingIntent pi = createPendingIntent(INTENT_ALARM);

        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, 21);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);

        if (Calendar.getInstance().after(target)) //현재 시간이 목표 시간보다 작으면
        {
            target.add(Calendar.DATE, 1);
        }

        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, target.getTime().getTime(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
    }

    private void registerCheckAlarm() {
        Calendar target = Calendar.getInstance();
        target.add(Calendar.SECOND,15);
       
        PendingIntent pi = createPendingIntent(INTENT_CHKALARM);        
        
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, target.getTime().getTime(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
    }
}
