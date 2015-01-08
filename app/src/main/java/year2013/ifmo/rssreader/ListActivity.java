package year2013.ifmo.rssreader;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class ListActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter adapter;
    private String SOURCE_NAME;
    public static final String EXTRA_SOURCE_NAME = "source_name";
    public static final String EXTRA_LINK = "link";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        SOURCE_NAME = getIntent().getStringExtra(EXTRA_SOURCE_NAME);

        String from[] = {News.Item.TITLE_NAME};
        int to[] = {android.R.id.text1};
        adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, null, from, to);

        ListView listView = (ListView) findViewById(R.id.data_list_view);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(listener);

        getLoaderManager().initLoader(0, null, this);

        if (isOnline()) loadItems();
        else
            Toast.makeText(ListActivity.this,"Check internet connection",
                    Toast.LENGTH_SHORT).show();
    }

    private void loadItems() {
        getContentResolver().delete(News.Item.CONTENT_URI,
                "(" + News.Item.SOURCE_NAME + "=\"" + SOURCE_NAME + "\")", null);
        Intent intent = new Intent(this, DownloadigData.class);
        intent.putExtra(DownloadigData.EXTRA_URL, getIntent().getStringExtra(EXTRA_LINK));
        intent.putExtra(DownloadigData.EXTRA_SOURCE_NAME, SOURCE_NAME);
        startService(intent);
    }

    private boolean isOnline() {
        String cs = Context.CONNECTIVITY_SERVICE;
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(cs);
        if (cm.getActiveNetworkInfo() == null) {
            return false;
        }
        return cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor o = ((Cursor) adapter.getItem(position));
            Intent intent = new Intent(ListActivity.this, ViewActivity.class);
            intent.putExtra(ViewActivity.EXTRA_URL,
                    o.getString(News.Item.URL_COLUMN));
            startActivity(intent);
        }
    };

    static final String[] SUMMARY_PROJECTION = new String[] {
            News.Item._ID,
            News.Item.SOURCE_NAME,
            News.Item.TITLE_NAME,
            News.Item.DESCRIPTION_NAME,
            News.Item.URL_NAME
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri baseUri = News.Item.CONTENT_URI;

        String select = "(" + News.Item.SOURCE_NAME + "=\"" + SOURCE_NAME + "\")";
        return new CursorLoader(getBaseContext(), baseUri,
                SUMMARY_PROJECTION, select, null,
                null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
