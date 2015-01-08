package year2013.ifmo.rssreader;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Юлия on 04.01.2015.
 */
public class News {

    public static final String AUTHORITY =
            "year2013.ifmo.rssreader.provider.news";

    public static final class Source implements BaseColumns{
        public static final int ID_COLUMN = 0;
        public static final int TITLE_COLUMN = 1;
        public static final int URL_COLUMN = 2;

        private Source() {}

        public static final String SOURCE_NAME = "source";

        public static final Uri SOURCE_URI = Uri.parse("content://" +
                AUTHORITY + "/" + Source.SOURCE_NAME);

        public static final Uri CONTENT_URI = SOURCE_URI;

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.source.data";

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.source.data";

        public static final String TITLE_NAME = "title";

        public static final String URL_NAME = "url";
    }

    public static final class Item implements BaseColumns {
        public static final int ID_COLUMN = 0;
        public static final int FEED_COLUMN = 1;
        public static final int TITLE_COLUMN = 2;
        public static final int DESCRIPTION_COLUMN = 3;
        public static final int URL_COLUMN = 4;

        private Item() {}

        public static final String ITEM_NAME = "item";

        public static final Uri ITEM_URI = Uri.parse("content://" +
                AUTHORITY + "/" + Item.ITEM_NAME);

        public static final Uri CONTENT_URI = ITEM_URI;

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.item.data";

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.item.data";

        public static final String SOURCE_NAME = "source";

        public static final String TITLE_NAME = "title";

        public static final String DESCRIPTION_NAME = "description";

        public static final String URL_NAME = "url";
    }

}
