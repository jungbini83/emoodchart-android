package embedded.korea.ac.kr.emoodchart.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Skais on 2016-08-08.
 * API에서 돌려주는 가장 기본적인 API로써, 서버에서 돌려주는 데이터와 요청이 끝난 시점을 파악할 수 있는 데이터를 가지고 있음
 */
public class ApiResponse {
    @SerializedName("result")
    private JsonObject result;

    @SerializedName("results")
    private JsonArray results;

    @SerializedName("endTime")
    private long endTime;

    public JsonObject getResult() {
        return result;
    }

    public long getDatetime() {
        return getEndTime();
    }

    public JsonArray getResults() {
        return results;
    }

    public long getEndTime() {
        return endTime;
    }
}
