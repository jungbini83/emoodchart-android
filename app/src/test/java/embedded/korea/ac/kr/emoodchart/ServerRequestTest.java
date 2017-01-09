package embedded.korea.ac.kr.emoodchart;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import embedded.korea.ac.kr.emoodchart.api.ApiClient;
import embedded.korea.ac.kr.emoodchart.api.response.*;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.HttpUrl;
import org.junit.*;
import retrofit2.converter.gson.GsonConverterFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ServerRequestTest {
    private AdminApiClient adminClient;
    private ApiClient userClient;

    public ServerRequestTest() {
        userClient = createAPI(ApiClient.class, "/api/v2/");
        adminClient = createAPI(AdminApiClient.class, "/api/v2/admin/");
    }

    private static <T> T createAPI(Class<T> className, String path) {
        final String URL_API = BuildConfig.API_URL + path;

        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    private List<Cookie> cookies;

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        this.cookies = cookies;
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        return cookies != null? cookies : new ArrayList<Cookie>();
                    }
                })
                .build();

        return new retrofit2.Retrofit.Builder()
                .baseUrl(URL_API)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(className);
    }

    private static void setPrivateField(Field field, Object value) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, "");
    }

    @Test
    public void testServiceAPIVersion() throws Exception {
        retrofit2.Response<ApiResponse<VersionResponse>> ret = userClient.checkUpdate().execute();
        assertEquals(ret.body().getResult().getVersion(), BuildConfig.version);
    }

    @Test
    public void testCodeLogin() throws Exception {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("passwd", "1234");

            assertTrue("Login Failed", adminClient.login(98, params).execute().isSuccessful());

            retrofit2.Response res = adminClient.createuser(98, 1).execute();
            assertTrue("User Creation Failed", res.isSuccessful() || res.code() == 304);

            retrofit2.Response<ApiResponse> code = adminClient.activate(98, 1).execute();
            assertTrue("User Activation Failed", code.isSuccessful());

            int loginCode = new Gson().fromJson(code.body().getResult().toString(), JsonObject.class).get("code").getAsInt();
            retrofit2.Response<ApiResponse<CodeResponse>> userInfo = userClient.authenticate(loginCode).execute();
            assertTrue(userInfo.isSuccessful());

            CodeResponse user = userInfo.body().getResult();
            assertTrue(userClient.checkAuth(98, 98, 1, user.getHash()).execute().isSuccessful());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            Map<String, Float> maps = new HashMap<>();

            maps.put(sdf.format(new Date()), 0.4f);
            assertTrue("Upload a light datum", userClient.uploadLight(98, 98, 1, user.getHash(), maps).execute().isSuccessful());

            maps.put(sdf.format(new Date()), 1000f);
            assertTrue("Upload light data", userClient.uploadLight(98, 98, 1, user.getHash(), maps).execute().isSuccessful());
        } finally {
            adminClient.deactivate(98, 1).execute();
        }
    }

    @Test
    public void uploadLight() {
    }
}