package embedded.korea.ac.kr.emoodchart.light;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static junit.framework.Assert.*;

/**
 * Created by skais on 17. 1. 9.
 */

@RunWith(AndroidJUnit4.class)
public class testSQLite {
    private final static String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);

    private Map<String, Float> selectAll(LightDBHelper db) {
        SQLiteDatabase open = db.getReadableDatabase();
        Cursor cursor = open.query(LightDBHelper.TABLE_NAME, null, null, null, null, null, null);

        Map<String, Float> ret = new HashMap<>();
        while(cursor.moveToNext()) {
            ret.put(cursor.getString(1), cursor.getFloat(0));
        }

        cursor.close();
        open.close();
        return ret;
    }

    @After
    public void clearAll() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        LightDBHelper db = new LightDBHelper(ctx);

        SQLiteDatabase open = db.getWritableDatabase();
        open.delete(LightDBHelper.TABLE_NAME, null, null);
        open.close();
        db.close();
    }


    @Test
    public void testStoreWhenFailed() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        LightDBHelper db = new LightDBHelper(ctx);

        float value = 0.5f;
        db.storeUploadFailed(sdf, new Date(), 0.5f);
    }

    @Test
    public void testUpload() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        LightDBHelper db = new LightDBHelper(ctx);

        // 데이터가 없음

        Date firstUploadData = new Date();
        Map<String, Float> data = db.getData(firstUploadData);
        assertEquals(data.size(), 0);   //데이터가 없어야 함

        // 실패한 데이터를 하나 추가함
        db.storeUploadFailed(sdf, firstUploadData, 1.3f);

        Date secondUploadData = new Date();
        data = db.getData(secondUploadData);
        assertEquals(data.size(), 1);   // 당장 하나의 데이터를 업로드 시도함

        Date thirdUploadDate = new Date();
        data = db.getData(thirdUploadDate);
        assertEquals(data.size(), 0);   //이미 업로드 중인 데이터는 하나이므로 당장은 아무것도 없어야 함

        db.storeUploadFailed(sdf, secondUploadData, 2.0f);
        assertEquals(selectAll(db).size(), 2);   // 또 실패할 경우 새로 추가된 데이터와 함께 두 개가 있어야 함

        Date fourthUploadDate = new Date();
        data = db.getData(fourthUploadDate);
        assertEquals(data.size(), 2);   // 현재까지 실패한 모든 데이터가 있어야 함

        Date fifthUpload = new Date();
        db.storeUploadFailed(sdf, firstUploadData, 2.0f);   // 업로드 동안 새로운 데이터가 생성됨
        data = db.getData(fifthUpload);
        assertEquals(data.size(), 1);

        db.clearUploadedData(fourthUploadDate);
        assertEquals(selectAll(db).size(), 1);   // 모든 데이터가 업로드 되었으므로 현재 업로드 중인 하나의 데이터가 존재함

        db.clearUploadedData(fifthUpload);
        assertEquals(selectAll(db).size(), 0);

        db.close();
    }
}
