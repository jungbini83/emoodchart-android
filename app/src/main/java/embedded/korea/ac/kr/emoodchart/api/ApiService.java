package embedded.korea.ac.kr.emoodchart.api;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("/api/inst/{iid}/proj/{pid}/ident/{ident}/light")
    Call<ApiResponse> uploadLight(@Path("iid") int iid, @Path("pid") int pid, @Path("ident") int ident, @Query("hash") String hash);

    @GET("/apk/update")
    Call<ApiResponse> checkUpdate(@Query("version") String version);

    @HEAD("/api/inst/{iid}/proj/{pid}/ident/{ident}")
    Call<ApiResponse> checkAuth(@Path("iid") int iid, @Path("pid") int pid, @Path("ident") int ident, @Query("hash") String hash);
}