package uk.ac.wlv.myblogapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

// Handles all SQLite database operations for blog messages
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "blog_database.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_MESSAGES = "messages";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_IMAGE_PATH = "image_path";
    public static final String COLUMN_DATE_TIME = "date_time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creates the messages table when the database is first created
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_MESSAGES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_CONTENT + " TEXT, " +
                COLUMN_IMAGE_PATH + " TEXT, " +
                COLUMN_DATE_TIME + " TEXT)";
        db.execSQL(createTable);
    }

    // Recreates the table if the database version changes
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    // Inserts a new blog message into SQLite database
    public boolean insertMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, message.getTitle());
        values.put(COLUMN_CONTENT, message.getContent());
        values.put(COLUMN_IMAGE_PATH, message.getImagePath());
        values.put(COLUMN_DATE_TIME, message.getDateTime());

        long result = db.insert(TABLE_MESSAGES, null, values);
        db.close();

        return result != -1;
    }

    // Gets all saved messages and shows newest messages first
    public ArrayList<Message> getAllMessages() {
        ArrayList<Message> messageList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_MESSAGES + " ORDER BY " + COLUMN_ID + " DESC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH));
                String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME));

                Message message = new Message(id, title, content, imagePath, dateTime);
                messageList.add(message);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return messageList;
    }

    // Gets one message by using its message id
    public Message getMessageById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        Message message = null;

        if (cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
            String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH));
            String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME));

            message = new Message(id, title, content, imagePath, dateTime);
        }

        cursor.close();
        db.close();

        return message;
    }

    // Deletes a single message from the database
    public boolean deleteMessage(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_MESSAGES,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        db.close();

        return result > 0;
    }

    // Updates an existing blog message
    public boolean updateMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, message.getTitle());
        values.put(COLUMN_CONTENT, message.getContent());
        values.put(COLUMN_IMAGE_PATH, message.getImagePath());
        values.put(COLUMN_DATE_TIME, message.getDateTime());

        int result = db.update(
                TABLE_MESSAGES,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(message.getId())}
        );

        db.close();

        return result > 0;
    }

    // Searches messages by matching the keyword with title or content
    public ArrayList<Message> searchMessages(String keyword) {
        ArrayList<Message> messageList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_MESSAGES +
                        " WHERE " + COLUMN_TITLE + " LIKE ? OR " + COLUMN_CONTENT + " LIKE ?" +
                        " ORDER BY " + COLUMN_ID + " DESC",
                new String[]{"%" + keyword + "%", "%" + keyword + "%"}
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH));
                String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME));

                Message message = new Message(id, title, content, imagePath, dateTime);
                messageList.add(message);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return messageList;
    }

    // Deletes multiple selected messages from the database
    public int deleteSelectedMessages(ArrayList<Message> selectedMessages) {
        SQLiteDatabase db = this.getWritableDatabase();

        int deletedCount = 0;

        for (Message message : selectedMessages) {
            int result = db.delete(
                    TABLE_MESSAGES,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(message.getId())}
            );

            deletedCount += result;
        }

        db.close();

        return deletedCount;
    }
}