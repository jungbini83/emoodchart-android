package embedded.korea.ac.kr.emoodchart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;


public class TestActivity extends Activity implements SensorEventListener, Runnable, OnClickListener {
    SharedPreferences pfSetting;
    SensorManager sensorManager;
    Handler handler = new Handler();
    float[] values;
    int userId,prjId,instId;
    
    String hash;

    Intent service;
    
    Button serveyButton,updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        updateButton = (Button)findViewById(R.id.btn_update);
        pfSetting = getSharedPreferences("setting", MODE_PRIVATE);
        String service_name = Context.SENSOR_SERVICE;
        sensorManager = (SensorManager)getSystemService(service_name);
        initLightSensor();

        userId = pfSetting.getInt("userId", 0);
        prjId = pfSetting.getInt("projectId",0);
        instId = pfSetting.getInt("instId",0);
        hash = pfSetting.getString("hash", "");


        TextView rebootable = (TextView)findViewById(R.id.avail_reboot);
        if (!pfSetting.contains("chk_reboot")) {
            pfSetting.edit().putBoolean("chk_reboot", false).apply();
            rebootable.setText("재부팅 필요");
        } else {
            boolean available = pfSetting.getBoolean("chk_reboot", false);
            if (available) {
                rebootable.setText("가능");
            } else {
                rebootable.setText("불가능");
            }
        }

        findViewById(R.id.start).setOnClickListener(this);
        serveyButton = (Button)findViewById(R.id.button_opensurvey);
        
        serveyButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://52.68.83.209:4000/inst/"+instId+"/project/"+prjId+"/user/"+userId+"/survey?hash="+hash));
				 startActivity(browserIntent);
			}
		});
        updateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				checkUpdate();
			}
		});
    }

    @Override
    public void onResume() {
        super.onResume();
        initLightSensor();
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (service != null) {
            stopService(service);
            service = null;
        }
    }

    public void initLightSensor(){
        Sensor light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        TextView availLight = (TextView)findViewById(R.id.avail_light);
        if(light == null){
            availLight.setText("불가능");
        } else {
            availLight.setText("가능");
        }

        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        values = event.values;
        handler.post(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void run() {
        if (values == null) return;
        TextView valueLight = (TextView)findViewById(R.id.value_light);
        valueLight.setText(Float.toString(values[0]));
    }

    @Override
    public void onClick(View v) {
        //R.id.start Only
        service = new Intent(this, eMoodChartService.class);
        startService(service);
    }
    public void checkUpdate()
    {
    	RequestQueue queue = Volley.newRequestQueue(this);
    	String url = StaticValue.checkUpdate();
		
		JSONObject params = new JSONObject();
		
		Log.v("teemo",url);
		
		JsonObjectRequest jReq = new JsonObjectRequest(Request.Method.GET , url, params, 
					new Response.Listener<JSONObject>()
					{
						@Override
						public void onResponse(JSONObject jObj)
						{
							Toast.makeText(getBaseContext(), "어플리케이션이 최신버전입니다.",Toast.LENGTH_LONG).show();
						}
					},
					new Response.ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError vError) {
							
							Log.v("teemo",vError.toString());
							
							
							if(vError.networkResponse!=null)
							{
								int errCode = vError.networkResponse.statusCode;
								if(errCode == 302)
								{
									//업데이트 진행
									
									Toast.makeText(getBaseContext(), "업데이트 페이지로 이동합니다.",Toast.LENGTH_LONG).show();
									 Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(StaticValue.checkUpdate()));
									 startActivity(browserIntent);
								}
								else
									Toast.makeText(getBaseContext(), "확인에 실패하였습니다.",Toast.LENGTH_LONG).show();
							}
							
						}
					}
				);
					
		
		queue.add(jReq);
    }
}