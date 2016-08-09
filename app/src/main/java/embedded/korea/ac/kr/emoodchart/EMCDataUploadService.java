package embedded.korea.ac.kr.emoodchart;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import embedded.korea.ac.kr.emoodchart.api.ApiInterface;
import embedded.korea.ac.kr.emoodchart.api.ApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EMCDataUploadService extends Service implements SensorEventListener {
    private ApiInterface api = new ApiInterface();
    private SQLiteOpenHelper mDB;

    private final static int DB_VERSION = 6;
    private final static String TABLE_NAME = "lightbackup";
    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Override
    public void onCreate() {
        super.onCreate();
        mDB = new DBHelper(this);
    }

    @Override
    public void onDestroy() {
        SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        sm.unregisterListener(this);

        if (mDB != null) mDB.close();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor lightSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor != null) {
            sm.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        return START_NOT_STICKY;
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
