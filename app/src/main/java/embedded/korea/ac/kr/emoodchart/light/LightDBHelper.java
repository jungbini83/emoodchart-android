package embedded.korea.ac.kr.emoodchart.light;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 기존에 실패했던 데이터들을 보관하는 데이터베이스를 관리하기 위한 인터페이스
 */
class LightDBHelper extends SQLiteOpenHelper {
    private final static int DB_VERSION = 6;
    public final static String TABLE_NAME = "lightbackup";

    LightDBHelper(Context context) {
        super(context, "backup", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (value real, date text, trying integer);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME + ";");
        this.onCreate(db);
    }

    public void clearUploadedData(Date date) {
        SQLiteDatabase open = getWritableDatabase();

        open.beginTransaction();
        open.delete(TABLE_NAME, "trying=" + date.getTime(), null);
        open.setTransactionSuccessful();
        open.endTransaction();
        open.close();
    }

    public Map<String, Float> getData(Date key) {
        ContentValues trying = new ContentValues();
        long requestId = key.getTime();

        trying.put("trying", requestId);

        SQLiteDatabase open = getWritableDatabase();
        open.beginTransaction();
        // 현재 업로드가 수행되지 않는 데이터들을 전부 현재 날짜로 업데이트
        open.update(TABLE_NAME, trying, "trying=0", null);

        // 업데이트가 되었던 리스트들을 하나씩 가져와서 데이터로 넣는다.
        Cursor cur = open.query(TABLE_NAME, null, "trying=" + Long.toString(requestId), null, null, null, null);
        Map<String, Float> tmp = new HashMap<>();
        while (cur.moveToNext()) {
            tmp.put(cur.getString(1), cur.getFloat(0));
        }
        cur.close();
        open.setTransactionSuccessful();
        open.endTransaction();
        open.close();

        return tmp;
    }

    public void storeUploadFailed(SimpleDateFormat sdf,  Date key, float value) {
        // 업로드가 실패했을 경우에는 기존에 저장되어 있던 데이터는 원래대로 복구하고
        // 새로 수집했던 데이터를 데이터베이스에 추가한다

        SQLiteDatabase open = getWritableDatabase();
        ContentValues updateValue = new ContentValues();
        ContentValues newValue = new ContentValues();

        updateValue.put("trying", 0);

        newValue.put("trying", 0);
        newValue.put("value", value);
        newValue.put("date", sdf.format(key));

        open.beginTransaction();
        open.update(TABLE_NAME, updateValue, "trying=" + key.getTime(), null);
        open.insert(TABLE_NAME, null, newValue);
        open.setTransactionSuccessful();
        open.endTransaction();
        open.close();
    }
}
