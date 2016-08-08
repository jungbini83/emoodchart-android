package embedded.korea.ac.kr.emoodchart.api;

import retrofit2.Call;
import retrofit2.http.*;

public interface Api {
    @POST("/inst/{iid}/proj/{pid}/ident/{ident}/light")
    Call<ApiResponse> uploadLight(@Query("hash") String hash);

    @GET("/apk/update?version=")
    Call<ApiResponse> checkUpdate();
}