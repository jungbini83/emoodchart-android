package embedded.korea.ac.kr.emoodchart;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import embedded.korea.ac.kr.emoodchart.api.ApiInterface;
import embedded.korea.ac.kr.emoodchart.api.ApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CodeInputActivity extends AppCompatActivity {
    EditText codeEdit;
    Button codeConfirmBtn;
    Button codeCancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_input);

        codeEdit        = (EditText)    findViewById(R.id.code_edit);
        codeConfirmBtn  = (Button)      findViewById(R.id.code_confirm);
        codeCancelBtn   = (Button)      findViewById(R.id.code_cancel);

        codeConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int code = Integer.parseInt(codeEdit.getText().toString());
                sendCodeToServer(code);
            }
        });

        codeCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
    private void sendCodeToServer(int code)
    {
        final Context ctx = this.getBaseContext();
        final SharedPreferences pfSetting	= getSharedPreferences("setting", MODE_PRIVATE);

        ApiInterface api = new ApiInterface();

        api.authenticate(code).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                Log.v("teemo",response.body().toString() );
                /*
                pfSetting.edit().putInt("userId", userId).apply();
                pfSetting.edit().putInt("projectId", prjId).apply();
                pfSetting.edit().putInt("instId", instId).apply();
                pfSetting.edit().putString("hash", hash).apply();*/


                Intent intent = new Intent(ctx , StatusActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                //Fail
            }
        });
    }
}
