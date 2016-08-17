package embedded.korea.ac.kr.emoodchart;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import embedded.korea.ac.kr.emoodchart.api.ApiService;
import embedded.korea.ac.kr.emoodchart.api.response.ApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EMCService extends Service implements SensorEventListener {
    private BroadcastReceiver mLightRecv;

    static final String ACT_ALARM = "emoodchart.action.SURVEY_ALARM";
    static final String ACT_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON";
    static final String ACT_BOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED";
    static final String ACT_SCREEN_ON = "emoodchart.action.SCREEN_ON";

    private static final int NOTI_ID = 1234;

    private static final int PI_RESTART = 2;
    private static final int PI_SURVEY = 4;

    private ApiService api = new ApiService();
    private SQLiteOpenHelper mDB;

    private final static int DB_VERSION = 6;
    private final static String TABLE_NAME = "lightbackup";
    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

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

                    SharedPreferences pf = getSharedPreferences("appstatus", MODE_PRIVATE);
                    if (pf.getBoolean("rebootable", false)) {
                        pf.edit().putBoolean("rebootable", true).apply();

                        Intent chk = new Intent(this, MainActivity.class);
                        chk.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(chk);
                    }
                } else if (action.equals(ACT_ALARM)) {
                /*
                 * 알람 시간일 경우 해당 알람을 띄움
                 *
                 */
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ApiService.genSurveyUrl(user)));
                    PendingIntent pi = PendingIntent.getActivity(this, 0, browserIntent, PendingIntent.FLAG_ONE_SHOT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.noticon)
                            .setContentTitle("설문조사")
                            .setAutoCancel(true)
                            .setContentIntent(pi)
                            .setContentText("오늘의 설문조사를 진행해 주세요");

                    NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    nm.notify(NOTI_ID, builder.build());

                    Vibrator vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                    vib.vibrate(1000);
                } else if (action.equals(ACT_SCREEN_ON)){
                    SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
                    Sensor lightSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);

                    if (lightSensor != null) {
                        sm.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
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

        new Uploader(event.values[0]).start(new UserInfo(this));
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

        mDB = new DBHelper(this);
    }

    @Override
    public void onDestroy() {
        if (mLightRecv != null) unregisterReceiver(mLightRecv);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.cancel(createPendingIntent(PI_SURVEY));

        SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        sm.unregisterListener(this);

        if (mDB != null) mDB.close();

        super.onDestroy();
    }

    private PendingIntent createPendingIntent(int pid) {
        Intent intent;
    	if (pid == PI_RESTART) {
    	    intent = new Intent(this, EMCReceiver.class);
    	} else if (pid == PI_SURVEY) {
    		intent = new Intent(this, EMCService.class);
            intent.setAction(ACT_ALARM);
    	} else {
    	    throw new IllegalArgumentException("Unknown PendingIntent type");
    	}

        return PendingIntent.getService(this, pid, intent, PendingIntent.FLAG_ONE_SHOT);
    }

    /**
     * 새로운 데이터가 추가될 때마다 새롭게 생성하며 데이터의 업로드의 성공, 실패에 따라 데이터베이스를 조작하는 역할을 수행함
     */
    private class Uploader implements Callback<ApiResponse> {
        private long mKey;
        private float mValue;

        Uploader(float mValue) {
            this.mKey = new Date().getTime();
            this.mValue = mValue;
        }

        void start(UserInfo user) {
            Map<String,Float> data = createData();
            data.put(sdf.format(new Date(mKey)), mValue);
            api.uploadLight(user, data).enqueue(this);
        }

        @Override
        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
            // 성공했을 경우에는 데이터베이스에 존재했던 데이터들은 삭제하고 마무리한다
            SQLiteDatabase open = mDB.getWritableDatabase();

            open.beginTransaction();
            open.delete(TABLE_NAME, "trying=" + Long.toString(mKey), null);
            open.setTransactionSuccessful();
            open.endTransaction();
            open.close();
        }

        @Override
        public void onFailure(Call<ApiResponse> call, Throwable t) {
            // 업로드가 실패했을 경우에는 기존에 저장되어 있던 데이터는 원래대로 복구하고
            // 새로 수집했던 데이터를 데이터베이스에 추가한다

            ContentValues trying = new ContentValues();
            ContentValues newValue = new ContentValues();

            trying.put("trying", 0);

            newValue.put("trying", 0);
            newValue.put("value", mValue);
            newValue.put("date", mKey);

            SQLiteDatabase open = mDB.getWritableDatabase();

            open.beginTransaction();
            open.update(TABLE_NAME, trying, "trying=" + Long.toString(mKey), null);
            open.insert(TABLE_NAME, null, newValue);
            open.setTransactionSuccessful();
            open.endTransaction();
            open.close();
        }

        private Map<String, Float> createData() {
            ContentValues trying = new ContentValues();
            trying.put("trying", mKey);

            SQLiteDatabase open = mDB.getWritableDatabase();
            open.beginTransaction();
            // 현재 업로드가 수행되지 않는 데이터들을 전부 현재 날짜로 업데이트
            open.update(TABLE_NAME, trying, "trying=0", null);

            // 업데이트가 되었던 리스트들을 하나씩 가져와서 데이터로 넣는다.
            Cursor cur = open.query(TABLE_NAME, null, "trying=" + Long.toString(mKey), null, null, null, null);
            Map<String, Float> tmp = new HashMap<>();
            while (cur.moveToNext()) {
                String key = sdf.format(new Date(cur.getLong(1)));
                tmp.put(key,cur.getFloat(0));
            }
            cur.close();
            open.setTransactionSuccessful();
            open.endTransaction();
            open.close();

            return tmp;
        }
    }

    /**
     * 기존에 실패했던 데이터들을 보관하는 데이터베이스를 관리하기 위한 인터페이스
     */
    private static class DBHelper extends SQLiteOpenHelper {
        DBHelper(Context context) {
            super(context, "backup", null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + TABLE_NAME + " (mValue real, date integer, trying integer);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists " + TABLE_NAME + ";");
            this.onCreate(db);
        }
    }
}
