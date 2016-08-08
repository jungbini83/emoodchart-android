package embedded.korea.ac.kr.emoodchart;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;

import java.util.Date;

public class LightUploadService extends Service implements SensorEventListener {
    SensorManager sensorManager;
    RequestQueue queue;

    int userId,instId,prjId;
    String hash;

    public LightUploadService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        queue = Volley.newRequestQueue(this);
        SharedPreferences pf = getSharedPreferences("setting", MODE_PRIVATE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        userId = pf.getInt("userId", 0);
        prjId = pf.getInt("projectId",0);
        instId = pf.getInt("instId",0);
        hash = pf.getString("hash","");
    }

    @Override
    public void onDestroy() {
        if (sensorManager != null) sensorManager.unregisterListener(this);
        if (queue != null) queue.cancelAll(this);

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals("ACTION.GET_LIGHT")) {
            detectLight();
        }

        return START_NOT_STICKY;
    }

    private void detectLight() {
        Sensor light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (light == null) {
            Log.v("error", "light sensor deactivated");
            return;
        }

        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void uploadData(double value, long date) {
        try {
            PostLightRequest req = new PostLightRequest(this, value, date);
            queue.add(req.createRequest(userId,prjId,instId,hash,this));
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sensorManager.unregisterListener(this);
        uploadData(event.values[0], new Date().getTime());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
