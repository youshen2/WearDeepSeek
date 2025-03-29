package moye.sine.deepseek.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import moye.sine.deepseek.model.Message;

public class MessageDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ChatMessages.db";
    private static final int DATABASE_VERSION = 1;

    // 表结构定义
    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_MESSAGE_ID = "message_id";
    private static final String COLUMN_CHAT_ID = "chat_id";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_REASON = "reason";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_TOKEN_COUNT = "token_count";
    private static final String COLUMN_IS_THINKING = "isThinking";

    // 建表语句
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_MESSAGES + " (" +
                    COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_CHAT_ID + " INTEGER NOT NULL," +
                    COLUMN_ROLE + " TEXT," +
                    COLUMN_CONTENT + " TEXT," +
                    COLUMN_REASON + " TEXT," +
                    COLUMN_TIMESTAMP + " INTEGER," +
                    COLUMN_TOKEN_COUNT + " INTEGER," +
                    COLUMN_IS_THINKING + " INTEGER)";

    public MessageDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    // 插入新消息并返回message_id
    public long insertMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHAT_ID, message.getChat_id());
        values.put(COLUMN_ROLE, message.getRole());
        values.put(COLUMN_CONTENT, message.getContent());
        values.put(COLUMN_REASON, message.getReason());
        values.put(COLUMN_TIMESTAMP, message.getTimestamp());
        values.put(COLUMN_TOKEN_COUNT, message.getToken_count());
        values.put(COLUMN_IS_THINKING, message.isThinking() ? 1 : 0);

        long newId = db.insert(TABLE_MESSAGES, null, values);
        db.close();
        return newId;
    }

    // 更新任意字段（根据message_id）
    public int updateMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHAT_ID, message.getChat_id());
        values.put(COLUMN_ROLE, message.getRole());
        values.put(COLUMN_CONTENT, message.getContent());
        values.put(COLUMN_REASON, message.getReason());
        values.put(COLUMN_TIMESTAMP, message.getTimestamp());
        values.put(COLUMN_TOKEN_COUNT, message.getToken_count());
        values.put(COLUMN_IS_THINKING, message.isThinking() ? 1 : 0);

        int rowsAffected = db.update(TABLE_MESSAGES,
                values,
                COLUMN_MESSAGE_ID + " = ?",
                new String[]{String.valueOf(message.getMessage_id())});
        return rowsAffected;
    }

    // 按chat_id获取所有消息（按message_id升序）
    public List<Message> getMessagesByChatId(long chatId) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_CHAT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(chatId)};

        Cursor cursor = db.query(TABLE_MESSAGES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_MESSAGE_ID + " ASC");

        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setMessage_id(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_ID)));
                message.setChat_id(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CHAT_ID)));
                message.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)));
                message.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                message.setReason(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REASON)));
                message.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                message.setToken_count(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOKEN_COUNT)));
                message.setThinking(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_THINKING)) == 1);

                messages.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return messages;
    }

    // 删除单个消息
    public void deleteMessage(long messageId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES,
                COLUMN_MESSAGE_ID + " = ?",
                new String[]{String.valueOf(messageId)});
        db.close();
    }

    // 删除整个聊天记录
    public void deleteChat(long chatId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES,
                COLUMN_CHAT_ID + " = ?",
                new String[]{String.valueOf(chatId)});
        db.close();
    }
}