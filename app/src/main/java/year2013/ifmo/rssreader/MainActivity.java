package year2013.ifmo.rssreader;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{

    private SimpleCursorAdapter adapter;
    private ListView listView;
    private boolean deleteButton = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String from[] = { News.Source.TITLE_NAME, News.Source.URL_NAME };
        int to[] = { android.R.id.text1, android.R.id.text2 };
        adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, null, from, to);

        listView = (ListView) findViewById(R.id.source_list);
        listView.setBackgroundColor(Color.LTGRAY);
        Button button = (Button) findViewById(R.id.open_button);
        button.setBackgroundColor(Color.LTGRAY);
        button = (Button) findViewById(R.id.del_button);
        button.setBackgroundColor(Color.WHITE);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(listener);
        getLoaderManager().initLoader(0, null, this);
    }

    private AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor o = ((Cursor) adapter.getItem(position));
            if (deleteButton) {
                Uri uri = ContentUris.withAppendedId(News.Source.CONTENT_URI,
                        o.getLong(News.Source.ID_COLUMN));
                getContentResolver().delete(uri, null, null);
               // listView.setBackgroundColor(Color.LTGRAY);
                //deleteButton = false;
            } else {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                intent.putExtra(ListActivity.EXTRA_SOURCE_NAME,
                        o.getString(News.Source.TITLE_COLUMN));
                intent.putExtra(ListActivity.EXTRA_LINK,
                        o.getString(News.Source.URL_COLUMN));
                startActivity(intent);
            }
        }
    };

    static final String[] SUMMARY_PROJECTION = new String[] {
            News.Source._ID,
            News.Source.TITLE_NAME,
            News.Source.URL_NAME
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri baseUri = News.Source.CONTENT_URI;

        return new CursorLoader(getBaseContext(), baseUri,
                SUMMARY_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public void DeleteSource(View view) {
        listView.setBackgroundColor(Color.WHITE);
        deleteButton = true;
    }

    public void OpenSource(View view) {
        deleteButton = false;
        listView.setBackgroundColor(Color.LTGRAY);
    }

    public void addNewSource(View view) {
        deleteButton = false;
        listView.setBackgroundColor(Color.LTGRAY);
        EditText editText1 = (EditText) findViewById(R.id.edit_source);
        String title = editText1.getText().toString();
        EditText editText2 = (EditText) findViewById(R.id.edit_url);
        String url = editText2.getText().toString();
        if (title.isEmpty()) {
            Toast.makeText(MainActivity.this, "Enter name",
                    Toast.LENGTH_SHORT).show();
        } else if (url.isEmpty()) {
            Toast.makeText(MainActivity.this, "Enter url",
                    Toast.LENGTH_SHORT).show();
        } else {
            editText1.setText("");
            editText2.setText("");
            ContentValues cv = new ContentValues();
            cv.put(News.Source.TITLE_NAME, title);
            cv.put(News.Source.URL_NAME, url);
            getContentResolver().insert(News.Source.CONTENT_URI, cv);
        }
    }

}
