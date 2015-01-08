package year2013.ifmo.rssreader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;


public class ViewActivity extends Activity {

    public static final String EXTRA_URL = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        String url = getIntent().getStringExtra(EXTRA_URL);
        WebView webView = (WebView) this.findViewById(R.id.web_view);
//        webView.loadData(aDescription, "text/html; charset=utf-8", "utf-8");
        webView.loadUrl(url);
    }
}
