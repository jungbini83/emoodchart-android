package embedded.korea.ac.kr.emoodchart.api;

import embedded.korea.ac.kr.emoodchart.UserInfo;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Map;

/**
 * Created by Skais on 2016-08-09.
 */
public class ApiInterface {
    public ApiService service;

    public ApiInterface() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        this.service = new Retrofit.Builder()
                //.baseUrl("http://52.68.83.209/api/")
                .baseUrl("http://192.168.0.16:4000/api/v2/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
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

    public Call<ApiResponse> authenticate(int code) {
        return service.authenticate(code);
    }

    public static String genSurveyUrl(UserInfo user) {
        return null;
    }
    public static String genApkUrl() { return null; }
}
