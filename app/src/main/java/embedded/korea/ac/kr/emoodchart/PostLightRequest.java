package embedded.korea.ac.kr.emoodchart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 서버에 로컬에 있는 조도값을 전송함
 * 전송에 실패할 경우 이를 로컬 db에 저장하며, 성공할 경우 현재 요청에서 처리된 데이터들을 제거한다
 */
public class PostLightRequest implements Response.ErrorListener, Response.Listener<JSONObject> {
    final static String TABLE_NAME = "lightbackup";
    final static String FORMAT_DATE = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    static SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE);
    SQLiteOpenHelper db;
    double value;
    long date;
    long id;

    public PostLightRequest(Context ctx, double value, long date) {
        this.db = new DBHelper(ctx);

        this.value = value;
        this.date = date;
        this.id = date;
    }

    public JsonObjectRequest createRequest(int userId,int prjId,int instId, String hash,Object tag) throws JSONException {
        ContentValues trying = new ContentValues();
        trying.put("trying", id);

        JSONArray arr = new JSONArray();

        SQLiteDatabase open = db.getWritableDatabase();
        open.beginTransaction();
        open.update(TABLE_NAME, trying, "trying=0", null);
        Cursor cur = open.query(TABLE_NAME, null, "trying=" + Long.toString(id), null, null, null, null, null);
        while (cur.moveToNext()) {
            JSONObject obj = new JSONObject();
            obj.put("value", cur.getFloat(0));
            obj.put("date", sdf.format(new Date(cur.getLong(1))));
            arr.put(obj);
        }
        cur.close();
        open.setTransactionSuccessful();
        open.endTransaction();

        JSONObject obj = new JSONObject();
        obj.put("value", value);
        obj.put("date", sdf.format(new Date(date)));
        arr.put(obj);

        obj = new JSONObject();
        obj.put("data", arr);

        JsonObjectRequest ret = new JsonObjectRequest(Request.Method.POST, StaticValue.uploadLightUrl(userId,instId,prjId,hash), obj, this, this);
        ret.setTag(tag);
        return ret;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        ContentValues trying = new ContentValues();
        ContentValues newValue = new ContentValues();

        trying.put("trying", 0);

        newValue.put("trying", 0);
        newValue.put("value", value);
        newValue.put("date", date);

        SQLiteDatabase open = db.getWritableDatabase();

        open.beginTransaction();
        open.update(TABLE_NAME, trying, "trying=" + Long.toString(id), null);
        open.insert(TABLE_NAME, null, newValue);
        open.setTransactionSuccessful();
        open.endTransaction();

        db.close();
    }

    @Override
    public void onResponse(JSONObject response) {
        SQLiteDatabase open = db.getWritableDatabase();

        open.beginTransaction();
        open.delete(TABLE_NAME, "trying=" + Long.toString(id), null);
        open.setTransactionSuccessful();
        open.endTransaction();

        db.close();
    }

    private class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, "backup", null, 6);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + TABLE_NAME + " (value real, date integer, trying integer);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists " + TABLE_NAME + ";");
            this.onCreate(db);
        }
    }
}
