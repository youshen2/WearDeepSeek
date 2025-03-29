package moye.sine.deepseek.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import moye.sine.deepseek.model.Chat;

public class ChatDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ChatSessions.db";
    private static final int DATABASE_VERSION = 1;

    // 表结构定义
    private static final String TABLE_CHATS = "chats";
    private static final String COLUMN_CHAT_ID = "chat_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CREATE_TIME = "create_time";

    // 建表语句
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_CHATS + " (" +
                    COLUMN_CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_TITLE + " TEXT," +
                    COLUMN_CREATE_TIME + " INTEGER)";

    public ChatDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHATS);
        onCreate(db);
    }

    // 创建新chat并返回chat_id
    public long createChat(String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CREATE_TIME, System.currentTimeMillis());

        long newChatId = db.insert(TABLE_CHATS, null, values);
        db.close();
        return newChatId;
    }

    // 更新聊天标题
    public int updateChatTitle(long chatId, String newTitle) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, newTitle);

        return db.update(TABLE_CHATS,
                values,
                COLUMN_CHAT_ID + " = ?",
                new String[]{String.valueOf(chatId)});
    }

    // 获取所有聊天记录（按创建时间倒序）
    public List<Chat> getAllChats() {
        List<Chat> chats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CHATS,
                new String[]{COLUMN_CHAT_ID, COLUMN_TITLE, COLUMN_CREATE_TIME},
                null, null, null, null,
                COLUMN_CREATE_TIME + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Chat chat = new Chat();
                chat.setChat_id(cursor.getLong(0));
                chat.setTitle(cursor.getString(1));
                chat.setCreate_time(cursor.getLong(2));
                chats.add(chat);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return chats;
    }

    // 删除单个聊天记录
    public void deleteChat(long chatId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CHATS,
                COLUMN_CHAT_ID + " = ?",
                new String[]{String.valueOf(chatId)});
        db.close();
    }
}