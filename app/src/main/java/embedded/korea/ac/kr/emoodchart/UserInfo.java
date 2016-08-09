package embedded.korea.ac.kr.emoodchart;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Skais on 2016-08-09.
 */
public class UserInfo {
    private int userId;
    private int instId;
    private int projId;
    private String hash;

    public UserInfo(Context ctx) {
        SharedPreferences pf = ctx.getSharedPreferences("setting", ctx.MODE_PRIVATE);

        setUserId(pf.getInt("uid", 0));
        setProjId(pf.getInt("pid",0));
        setInstId(pf.getInt("iid",0));
        setHash(pf.getString("hash", ""));
    }

    public UserInfo() {

    }

    public boolean isValid() {
        return getUserId() != 0;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public int getInstId() {
        return instId;
    }

    public void setInstId(int instId) {
        this.instId = instId;
    }

    public int getProjId() {
        return projId;
    }

    public void setProjId(int projId) {
        this.projId = projId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
