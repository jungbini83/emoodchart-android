package embedded.korea.ac.kr.emoodchart.api;

import embedded.korea.ac.kr.emoodchart.UserInfo;
import embedded.korea.ac.kr.emoodchart.api.response.ApiResponse;
import embedded.korea.ac.kr.emoodchart.api.response.CodeResponse;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Map;

/**
 * Created by Skais on 2016-08-09.
 */
public class ApiService {
    public ApiDefinition service;

    public ApiService() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        this.service = new Retrofit.Builder()
                //.baseUrl("http://52.68.83.209/api/")
                .baseUrl("http://10.16.16.125:4000/api/v2/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiDefinition.class);
    }

    public Call<ApiResponse> checkAuth(UserInfo info) {
        return service.checkAuth(info.getInstId(), info.getProjId(), info.getUserId(), info.getHash());
    }

    public Call<ApiResponse> uploadLight(UserInfo info, Map<String, Float> body) {
        return service.uploadLight(info.getInstId(), info.getProjId(), info.getUserId(), info.getHash(), body);
    }

    public Call<ApiResponse> checkApkUpdate() {
        return service.checkUpdate("3.0");
    }

    public Call<CodeResponse> authenticate(int code) {
        return service.authenticate(code);
    }

    public static String genSurveyUrl(UserInfo user) {
        return null;
    }
    public static String genApkUrl() { return null; }
    public Call<ApiResponse> getInsts() { return service.getInsts(); }
}
