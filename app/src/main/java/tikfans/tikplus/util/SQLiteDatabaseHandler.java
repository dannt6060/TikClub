package tikfans.tikplus.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tikfans.tikplus.model.ItemVideo;

import static java.sql.Types.BOOLEAN;

public class SQLiteDatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ItemVideoDB";
    private static final String TABLE_NAME = "ItemVideo";
    private static final String KEY_ID = "id";
    private static final String KEY_IMAGE_URL = "imageUrl";
    private static final String KEY_DIGG_COUNT = "diggCount";
    private static final String KEY_VIDEO_URL = "videoUrl";
    private static final String KEY_WEB_VIDEO_URL = "webVideoUrl";
    private static final String KEY_SECRET = "secret";
    private static final String KEY_FOR_FRIEND = "forFriend";

    private static final String[] COLUMNS = { KEY_ID, KEY_IMAGE_URL, KEY_DIGG_COUNT,KEY_VIDEO_URL,
            KEY_WEB_VIDEO_URL,KEY_SECRET, KEY_FOR_FRIEND};

    public SQLiteDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATION_TABLE = "CREATE TABLE ItemVideo ( "
                + "id TEXT, " + "imageUrl TEXT, "
                + "diggCount INTEGER, " + "videoUrl TEXT, " + " webVideoUrl TEXT, " + "secret BOOLEAN, " + "forFriend BOOLEAN )";

        db.execSQL(CREATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // you can implement here migration process
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }

    public void deleteOne(ItemVideo video) {
        // Get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "id = ?", new String[] { String.valueOf(video.getId()) });
        db.close();
    }

    public void removeAll()
    {
        // db.delete(String tableName, String whereClause, String[] whereArgs);
        // If whereClause is null, it will delete all rows.
        SQLiteDatabase db = this.getWritableDatabase(); // helper is object extends SQLiteOpenHelper
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    public ItemVideo getItemVideo(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, // a. table
                COLUMNS, // b. column names
                " id = ?", // c. selections
                new String[] { String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit

        if (cursor != null)
            cursor.moveToFirst();

        ItemVideo itemVideo = new ItemVideo();
        itemVideo.setId(cursor.getString(0));
        itemVideo.setImageUrl(cursor.getString(1));
        itemVideo.setDiggCount(Integer.parseInt(cursor.getString(2)));
        itemVideo.setVideoUrl(cursor.getString(3));
        itemVideo.setWebVideoUrl(cursor.getString(4));
        return itemVideo;
    }

    public ArrayList<ItemVideo> getAllItemVideo() {

        ArrayList<ItemVideo> itemVideoList = new ArrayList<ItemVideo>();
        String query = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        ItemVideo itemVideo = null;

        if (cursor.moveToFirst()) {
            do {
                itemVideo = new ItemVideo();
                itemVideo.setId(cursor.getString(0));
                itemVideo.setImageUrl(cursor.getString(1));
                itemVideo.setDiggCount(Integer.parseInt(cursor.getString(2)));
                itemVideo.setVideoUrl(cursor.getString(3));
                itemVideo.setWebVideoUrl(cursor.getString(4));
                itemVideoList.add(itemVideo);
            } while (cursor.moveToNext());
        }

        return itemVideoList;
    }


    public void addItemVideo(ItemVideo itemVideo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, itemVideo.getId());
        values.put(KEY_IMAGE_URL, itemVideo.getImageUrl());
        values.put(KEY_DIGG_COUNT, itemVideo.getDiggCount());
        values.put(KEY_VIDEO_URL, itemVideo.getVideoUrl());
        values.put(KEY_WEB_VIDEO_URL, itemVideo.getWebVideoUrl());
        db.insert(TABLE_NAME,null, values);
        db.close();
    }

}