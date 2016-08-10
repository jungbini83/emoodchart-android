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

    private UserInfo(int iid, int pid, int uid, String hash) {
        this.instId = iid;
        this.projId = pid;
        this.userId = uid;
        this.hash = hash;
    }

    UserInfo(Context ctx) {
        SharedPreferences pf = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);

        this.userId = pf.getInt("uid", 0);
        this.instId = pf.getInt("iid",0);
        this.projId = pf.getInt("pid",0);
        this.hash = pf.getString("hash", "");
    }

    static void set(Context ctx, int iid, int pid, int uid, String hash) {
        SharedPreferences pf = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);
        pf.edit().putInt("uid", uid).putInt("iid", iid).putInt("pid", pid).putString("hash", hash).apply();
    }

    boolean isValid() {
        return getUserId() != 0;
    }
    public int getUserId() {
        return userId;
    }
    public int getInstId() {
        return instId;
    }
    public int getProjId() {
        return projId;
    }
    public String getHash() {
        return hash;
    }
}
