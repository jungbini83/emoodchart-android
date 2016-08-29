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
import android.widget.Toast;
import embedded.korea.ac.kr.emoodchart.api.ApiService;
import embedded.korea.ac.kr.emoodchart.api.response.ApiResponse;
import embedded.korea.ac.kr.emoodchart.api.response.CodeResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CodeInputActivity extends AppCompatActivity implements Callback<ApiResponse<CodeResponse>> {
    EditText codeEdit;
    Button codeConfirmBtn;
    Button codeCancelBtn;
    SharedPreferences pfSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_input);

        codeEdit        = (EditText)    findViewById(R.id.code_edit);
        codeConfirmBtn  = (Button)      findViewById(R.id.code_confirm);
        codeCancelBtn   = (Button)      findViewById(R.id.code_cancel);

        pfSetting = getSharedPreferences("user", MODE_PRIVATE);

        if(this.getIntent().getStringExtra("code") !=  null)
        {
            codeEdit.setText(this.getIntent().getStringExtra("code"));
        }

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

        ApiService api = new ApiService();

        api.authenticate(code).enqueue(this);
    }

    @Override
    public void onResponse(Call<ApiResponse<CodeResponse>> call, Response<ApiResponse<CodeResponse>> response) {
        switch(response.code()) {
            case 200: Log.v("teemo",response.body().toString() ); break;
            default: return;
        }

        Log.v("teemo", ""+response.body().getResult().getIdentifier() );
        Log.v("teemo", ""+response.body().getResult().getProj_id() );
        Log.v("teemo", ""+response.body().getResult().getInst_id() );
        Log.v("teemo", ""+response.body().getResult().getHash() );

        pfSetting.edit().putInt("userId",  response.body().getResult().getIdentifier() ).apply();
        pfSetting.edit().putInt("projectId", response.body().getResult().getProj_id() ).apply();
        pfSetting.edit().putInt("instId", response.body().getResult().getInst_id() ).apply();
        pfSetting.edit().putString("hash", response.body().getResult().getHash() ).apply();

        Intent intent = new Intent(this , StatusActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onFailure(Call<ApiResponse<CodeResponse>> call, Throwable t) {
        //Fail
        Toast.makeText(CodeInputActivity.this.getBaseContext()  ,"서버 연결에 실패하였습니다.",Toast.LENGTH_LONG).show();
    }
}
