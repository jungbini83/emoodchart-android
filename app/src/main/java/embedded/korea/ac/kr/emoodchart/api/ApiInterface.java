package embedded.korea.ac.kr.emoodchart.api;

import com.google.gson.JsonObject;
import embedded.korea.ac.kr.emoodchart.UserInfo;
import retrofit2.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Retrofit;

import java.util.Map;

/**
 * Created by Skais on 2016-08-09.
 */
public class ApiInterface {
    public ApiService service;

    public ApiInterface() {
        this.service = new Retrofit.Builder()
                //.baseUrl("http://52.68.83.209/api/")
                .baseUrl("http://10.16.16.125:4000/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
    }

    public Call<JsonObject> checkAuth(UserInfo info) {
        return service.checkAuth(info.getInstId(), info.getProjId(), info.getUserId(), info.getHash());
    }

    public Call<JsonObject> uploadLight(UserInfo info, Map<String, Float> body) {
        return service.uploadLight(info.getInstId(), info.getProjId(), info.getUserId(), info.getHash(), body);
    }

    public Call<JsonObject> checkApkUpdate() {
        return service.checkUpdate("3.0");
    }

    public Call<JsonObject> codeAuth(String code) {
        return service.codeAuth(code);
    }

    public static String genSurveyUrl(UserInfo user) {
        return null;
    }
    public static String genApkUrl() { return null; }
}
