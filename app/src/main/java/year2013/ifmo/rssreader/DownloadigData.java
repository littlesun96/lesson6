package year2013.ifmo.rssreader;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Юлия on 05.01.2015.
 */
public class DownloadigData extends IntentService {

    public static final String EXTRA_URL = "link";
    public static final String EXTRA_SOURCE_NAME = "source_name";
    private Handler handler = new Handler();

    public DownloadigData() {
        super("DownloadingData");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String SOURCE_NAME = intent.getStringExtra(EXTRA_SOURCE_NAME);
        try {
            URL url = new URL(intent.getStringExtra(EXTRA_URL));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream is = connection.getInputStream();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            SAXPars saxp = new SAXPars();
            parser.parse(is, saxp);

        } catch (MalformedURLException e) {
           // Toast.makeText(DownloadigData.this, "Unknowing URL address",
             //       Toast.LENGTH_SHORT).show();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DownloadigData.this,
                            "Unknowing URL address",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            });
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ContentValues cv = new ContentValues();
            /*if (resDataList.size() == 0)
                Toast.makeText(DownloadigData.this, "No feeds",
                        Toast.LENGTH_SHORT).show();*/
            for (Data aResult : resDataList) {
                cv.clear();
                cv.put(News.Item.SOURCE_NAME, SOURCE_NAME);
                cv.put(News.Item.TITLE_NAME, aResult.aTitle);
                cv.put(News.Item.DESCRIPTION_NAME, aResult.aDescription);
                cv.put(News.Item.URL_NAME, aResult.aLink);
                getContentResolver().insert(News.Item.CONTENT_URI, cv);
            }
            //Log.d("Logs", ((Integer)resDataList.size()).toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    enum listOfTargets {
        TITLE, IGNORE, LINK, DATE, DESCRIPTION
    }

    ArrayList<Data> resDataList = new ArrayList<>();

    private class SAXPars extends DefaultHandler {

        SAXPars () {
            super();
        }

        private listOfTargets target;
        private Data elementData = new Data();

        @Override
        public void startDocument() throws SAXException {
           // Log.d("Logs", "Start Parsing");
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            if (qName.equalsIgnoreCase("item")) {
                elementData = new Data();
                target = listOfTargets.IGNORE;
               // Log.d("Logs", "item");
            } else if (qName.equalsIgnoreCase("title")) {
                target = listOfTargets.TITLE;
            } else if (qName.equalsIgnoreCase("description")) {
                target = listOfTargets.DESCRIPTION;
            } else if (qName.equalsIgnoreCase("link")) {
                target = listOfTargets.LINK;
            } else target = listOfTargets.IGNORE;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String content = new String(ch, start, length);
            if (target != null)
                switch (target) {
                    case TITLE:
                        if (content.length() != 0) {
                            if (elementData.aTitle != null) {
                                elementData.aTitle += content;
                            } else {
                                elementData.aTitle = content;
                            }
                        }
                        break;
                    case LINK:
                        if (content.length() != 0) {
                            if (elementData.aLink != null) {
                                elementData.aLink += content;
                            } else {
                                elementData.aLink = content;
                            }
                        }
                        break;
                    case DESCRIPTION:
                        if (content.length() != 0) {
                            if (elementData.aDescription != null) {
                                elementData.aDescription += content;
                            } else {
                                elementData.aDescription = content;
                            }
                        }
                        break;
                }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            if (qName.equalsIgnoreCase("item")) {
                resDataList.add(elementData);
            } else {
                target = listOfTargets.IGNORE;
            }
        }

        @Override
        public void endDocument() {
           // Log.d("Logs", "End Parsing");
        }
    }
}