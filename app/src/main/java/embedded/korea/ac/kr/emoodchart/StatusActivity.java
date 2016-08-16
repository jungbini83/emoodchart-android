package embedded.korea.ac.kr.emoodchart;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonObject;
import embedded.korea.ac.kr.emoodchart.api.ApiInterface;
import embedded.korea.ac.kr.emoodchart.api.ApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class StatusActivity extends Activity {
    private ApiInterface mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status);

        mApi = new ApiInterface();

        // 조명 사용 가능 여부 확인
        SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        boolean light_available = (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null);

        TextView availLight = (TextView)findViewById(R.id.avail_light);
        if(light_available){
            availLight.setText("불가능");
        } else {
            availLight.setText("가능");
        }

        // 재부팅 시 자동 실행 가능 여부 확인
        TextView availReboot = (TextView)findViewById(R.id.avail_reboot);
        SharedPreferences pf = getSharedPreferences("appstatus", MODE_PRIVATE);
        if (pf.getBoolean("rebootable", true)) {
            availReboot.setText("가능");
        } else {
            availReboot.setText("불가능 혹은 확인필요");
        }

        // 서베이 이동 버튼 링크
        findViewById(R.id.button_opensurvey).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
                Uri uri = Uri.parse(ApiInterface.genSurveyUrl(new UserInfo(getBaseContext())));
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(browserIntent);
			}
		});

        // 업데이트 확인 버튼 링크
        findViewById(R.id.btn_update).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
                mApi.checkApkUpdate().enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        Toast.makeText(getBaseContext(), "어플리케이션이 최신버전입니다.",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
//                        int errCode = t.networkResponse.statusCode;
//                        if(errCode == 302)
//                        {
//                            //업데이트 진행
//
//                            Toast.makeText(getBaseContext(), "업데이트 페이지로 이동합니다.",Toast.LENGTH_LONG).show();
//                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ApiInterface.genApkUrl()));
//                            startActivity(browserIntent);
//                        }
//                        else
//                            Toast.makeText(getBaseContext(), "확인에 실패하였습니다.",Toast.LENGTH_LONG).show();
                    }
                });
			}
		});

        // 수집 서비스 시작
        Intent service = new Intent(this, EMCService.class);
        startService(service);
    }
}