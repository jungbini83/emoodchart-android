package embedded.korea.ac.kr.emoodchart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import embedded.korea.ac.kr.emoodchart.api.*;
import embedded.korea.ac.kr.emoodchart.api.response.ApiResponse;
import embedded.korea.ac.kr.emoodchart.api.response.FitbitResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 앱을 시작하면 기존에 설정된 아이디가 있는지 확인하고, 해당 아이디가 유효한지를 서버를 통해 확인함
 * 아니라면 새로운 아이디를 할당받기 위해서는 두 가지 프로세스가 존재함
 * - fitbit 아이디를 통한 확인
 * - 관리자 화면에서 특정 환자의 요청을 수락하면 이를 결합하도록 함
 */
public class MainActivity extends Activity {
    private ApiClient api = APIHelper.createClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserInfo info = new UserInfo(this);

        if (info.isValid()) {
            onAuthorized();
//            APIHelper.checkAuth(api, info).enqueue(new Callback<Void>() {
//				@Override
//				public void onResponse(Call<Void> call, Response<Void> response) {
//				    if (response.code() != 200) setLoginLayout();
//                    else onAuthorized();
//				}
//
//				@Override
//				public void onFailure(Call<Void> call, Throwable t) {
//                    // 1. 관리자가 발급한 issue ID를 통한 로그인
//                    // 2. fitbit 계정을 통한 로그인
//                    setLoginLayout();
//                    //Toast.makeText(MainActivity.this.getBaseContext()  ,"서버 연결에 실패하였습니다.",Toast.LENGTH_LONG).show();
//				}
//			});
        } else {
            setLoginLayout();
        }
    }

    private void setLoginLayout() {

        Button fitbitLoginBtn, coordLoginBtn;
        setContentView(R.layout.login);

        fitbitLoginBtn  = (Button)findViewById(R.id.btnLoginWithFitbit);
        coordLoginBtn   = (Button)findViewById(R.id.btnLoginWithCoord);

        fitbitLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            fitbitLoginRequest();
            }
        });

        coordLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCodeInputActivity(CodeInputActivity.VIA_CODE, null, null);
            }
        });
    }
    private void onAuthorized() {
        Intent intent = new Intent(this, StatusActivity.class);
        startActivity(intent);
        finish();
    }

    private void openCodeInputActivity(String type, String code, String url)
    {
        Intent intent = new Intent(this, CodeInputActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("code", code);
        intent.setAction(type);
        startActivity(intent);
    }
    private void fitbitLoginRequest()
    {
        api.loginWithFitbit().enqueue(new Callback<ApiResponse<FitbitResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FitbitResponse>> call, Response<ApiResponse<FitbitResponse>> response) {
                switch(response.code()) {
                    case 200: Log.v("teemo",response.body().toString() ); break;
                    default: return;
                }

                String code = response.body().getResult().getCode();
                String url = response.body().getResult().getUrl();
                openCodeInputActivity(CodeInputActivity.VIA_FITBIT, code, url);
            }

            @Override
            public void onFailure(Call<ApiResponse<FitbitResponse>> call, Throwable t) {
                Toast.makeText(MainActivity.this.getBaseContext()  ,"서버 연결에 실패하였습니다.",Toast.LENGTH_LONG).show();
            }
        });
    }
}