package embedded.korea.ac.kr.emoodchart;

import embedded.korea.ac.kr.emoodchart.api.ApiInterface;
import embedded.korea.ac.kr.emoodchart.api.ApiResponse;
import embedded.korea.ac.kr.emoodchart.api.ApiService;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        ApiInterface service = new ApiInterface();
        service.authenticate(234).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {

            }
        });
    }
}