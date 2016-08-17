package embedded.korea.ac.kr.emoodchart;

import embedded.korea.ac.kr.emoodchart.api.ApiService;
import embedded.korea.ac.kr.emoodchart.api.response.ApiResponse;
import org.junit.Test;
import retrofit2.Response;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ApiTest {
    @Test
    public void parseResultTest() throws Exception {
        ApiService service = new ApiService();
        Response<ApiResponse> ret = service.getInsts().execute();
        assertNotNull(ret.body());
    }
}