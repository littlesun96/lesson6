package year2013.ifmo.rssreader;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Юлия on 04.01.2015.
 */
public class ItemsContentProvider extends ContentProvider {

    public static final String NEWS = "news";
    public static final String SOURCES_TABLE_NAME = "sources";
    public static final String ITEMS_TABLE_NAME = "items";

    public ItemsContentProvider() {}

    private NewsDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new NewsDbHelper(getContext());
        return true;
    }

    private static final int SOURCES = 1;
    private static final int SOURCES_ID = 2;
    private static final int ITEMS = 3;
    private static final int ITEMS_ID = 4;
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(News.AUTHORITY, News.Source.SOURCE_NAME, SOURCES);
        uriMatcher.addURI(News.AUTHORITY, News.Source.SOURCE_NAME + "/#", SOURCES_ID);
        uriMatcher.addURI(News.AUTHORITY, News.Item.ITEM_NAME, ITEMS);
        uriMatcher.addURI(News.AUTHORITY, News.Item.ITEM_NAME + "/#", ITEMS_ID);
    }

    private static class NewsDbHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = NEWS + ".db";
        private static int DATABASE_VERSION = 5;

        private NewsDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            String s = "CREATE TABLE " + SOURCES_TABLE_NAME + " ("
                    + News.Source._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + News.Source.TITLE_NAME + " TEXT, "
                    + News.Source.URL_NAME + " TEXT" + ");";
            sqLiteDatabase.execSQL(s);
            ContentValues cv = new ContentValues();
            cv.put(News.Source.TITLE_NAME, "BBC");
            cv.put(News.Source.URL_NAME, "http://feeds.bbci.co.uk/news/rss.xml");
            sqLiteDatabase.insert(SOURCES_TABLE_NAME, null, cv);
            s = "CREATE TABLE " + ITEMS_TABLE_NAME + " ("
                    + News.Item._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + News.Item.SOURCE_NAME + " TEXT, "
                    + News.Item.TITLE_NAME + " TEXT, "
                    + News.Item.DESCRIPTION_NAME + " TEXT, "
                    + News.Item.URL_NAME + " TEXT" + ");";
            sqLiteDatabase.execSQL(s);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldv, int newv) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SOURCES_TABLE_NAME + ";");
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ITEMS_TABLE_NAME + ";");
            onCreate(sqLiteDatabase);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        int u = uriMatcher.match(uri);
        if (u != SOURCES && u != ITEMS) {
            throw new IllegalArgumentException("Wrong URI: " + uri);
        }
       // Log.d("LogsLL", ((Integer) u).toString());
        ContentValues values;
        if (initialValues != null) {
            values = initialValues;
        } else {
            values = new ContentValues();
        }

        if (u == SOURCES) {
            long rowID = dbHelper.getWritableDatabase().insert(SOURCES_TABLE_NAME, null, values);
            if (rowID > 0) {
                Uri resultUri = ContentUris.withAppendedId(News.Source.CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);
                return resultUri;
            }
        } else {
            long rowID = dbHelper.getWritableDatabase().insert(ITEMS_TABLE_NAME, null, values);
            if (rowID > 0) {
                Uri resultUri = ContentUris.withAppendedId(News.Item.CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);
                return resultUri;
            }
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int match = uriMatcher.match(uri);
        int affected;

        switch (match) {
            case SOURCES:
                affected = dbHelper.getWritableDatabase().delete(SOURCES_TABLE_NAME,
                        (!TextUtils.isEmpty(selection) ?
                                " (" + selection + ')' : ""),
                        selectionArgs);
                break;
            case SOURCES_ID:
                long sourceId = ContentUris.parseId(uri);
                affected = dbHelper.getWritableDatabase().delete(SOURCES_TABLE_NAME,
                        News.Source._ID + "=" + sourceId
                                + (!TextUtils.isEmpty(selection) ?
                                " (" + selection + ')' : ""),
                        selectionArgs);
                break;
            case ITEMS:
                affected = dbHelper.getWritableDatabase().delete(ITEMS_TABLE_NAME,
                        (!TextUtils.isEmpty(selection) ?
                                " (" + selection + ')' : ""),
                        selectionArgs);
                break;
            case ITEMS_ID:
                long itemId = ContentUris.parseId(uri);
                affected = dbHelper.getWritableDatabase().delete(ITEMS_TABLE_NAME,
                        News.Item._ID + "=" + itemId
                                + (!TextUtils.isEmpty(selection) ?
                                " (" + selection + ')' : ""),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("unknown feed element: " +
                        uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return affected;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        String TABLE_NAME;
        Uri CONTENT_URI;
        switch (uriMatcher.match(uri)) {
            case SOURCES:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = News.Source.TITLE_NAME + " ASC";
                }
                TABLE_NAME = SOURCES_TABLE_NAME;
                CONTENT_URI = News.Source.CONTENT_URI;
                break;
            case SOURCES_ID: {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = News.Source._ID + " = " + id;
                } else {
                    selection = selection + " AND " + News.Source._ID + " = " + id;
                }
                TABLE_NAME = SOURCES_TABLE_NAME;
                CONTENT_URI = News.Source.CONTENT_URI;
            }
            break;
            case ITEMS:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = News.Item.TITLE_NAME + " ASC";
                }
                TABLE_NAME = ITEMS_TABLE_NAME;
                CONTENT_URI = News.Item.CONTENT_URI;
                break;
            case ITEMS_ID: {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = News.Item._ID + " = " + id;
                } else {
                    selection = selection + " AND " + News.Item._ID + " = " + id;
                }
                TABLE_NAME = ITEMS_TABLE_NAME;
                CONTENT_URI = News.Item.CONTENT_URI;
            }
            break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        Cursor cursor = dbHelper.getWritableDatabase().query(TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int affected;

        switch (uriMatcher.match(uri)) {
            case SOURCES:
                affected = dbHelper.getWritableDatabase().update(SOURCES_TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case SOURCES_ID:
                String feedId = uri.getPathSegments().get(1);
                affected = dbHelper.getWritableDatabase().update(SOURCES_TABLE_NAME, values,
                        News.Source._ID + "=" + feedId
                                + (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""),
                        selectionArgs);
                break;
            case ITEMS:
                affected = dbHelper.getWritableDatabase().update(ITEMS_TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case ITEMS_ID:
                String postId = uri.getPathSegments().get(1);
                affected = dbHelper.getWritableDatabase().update(ITEMS_TABLE_NAME, values,
                        News.Item._ID + "=" + postId
                                + (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return affected;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case SOURCES:
                return News.Source.CONTENT_TYPE;
            case SOURCES_ID:
                return News.Source.CONTENT_ITEM_TYPE;
            case ITEMS:
                return News.Item.CONTENT_TYPE;
            case ITEMS_ID:
                return News.Item.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown type: " + uri);
        }
    }

}