package com.incitorrent.radyo.menemen.pro;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.incitorrent.radyo.menemen.pro.utils.Menemen;

public class Licence extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        final TextView textView = (TextView) findViewById(R.id.text_about);
        String html = "\n" +
                "Login\n" +
                "<div>Icons made by <a href=\"http://www.freepik.com\" title=\"Freepik\">Freepik</a> from <a href=\"http://www.flaticon.com\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>\n" +
                "chat\n" +
                "<div>Icons made by <a href=\"http://www.flaticon.com/authors/gregor-cresnar\" title=\"Gregor Cresnar\">Gregor Cresnar</a> from <a href=\"http://www.flaticon.com\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>\n" +
                "shout\n" +
                "<div>Icons made by <a href=\"http://www.freepik.com\" title=\"Freepik\">Freepik</a> from <a href=\"http://www.flaticon.com\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>\n" +
                "radio\n" +
                "<div>Icons made by <a href=\"http://www.flaticon.com/authors/epiccoders\" title=\"EpicCoders\">EpicCoders</a> from <a href=\"http://www.flaticon.com\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>\n" +
                "podcast\n" +
                "<div>Icons made by <a href=\"http://www.freepik.com\" title=\"Freepik\">Freepik</a> from <a href=\"http://www.flaticon.com\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>\n" +
                "announce\n" +
                "<div>Icons made by <a href=\"http://www.freepik.com\" title=\"Freepik\">Freepik</a> from <a href=\"http://www.flaticon.com\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>\n" +
                "credits\n" +
                "<div>Icons made by <a href=\"http://www.freepik.com\" title=\"Freepik\">Freepik</a> from <a href=\"http://www.flaticon.com\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>\n" +
                "notification\n" +
                "<div>Icons made by <a href=\"http://www.flaticon.com/authors/google\" title=\"Google\">Google</a> from <a href=\"http://www.flaticon.com\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>\n" +
                "emoji\n" +
                "<div>Icons made by <a href=\"http://www.freepik.com\" title=\"Freepik\">Freepik</a> from <a href=\"http://www.flaticon.com\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>" +
                "default art\n" +
                "<div>Icons made by <a href=\"http://www.freepik.com\" title=\"Freepik\">Freepik</a> from <a href=\"http://www.flaticon.com\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>\n" +
                "logout\n" +
                "<div>Icons made by <a href=\"http://www.flaticon.com/authors/dave-gandy\" title=\"Dave Gandy\">Dave Gandy</a> from <a href=\"http://www.flaticon.com\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>\n" +
                "send\n" +
                "<div>Icons made by <a href=\"http://www.flaticon.com/authors/josh-somers\" title=\"Josh Somers\">Josh Somers</a> from <a href=\"http://www.flaticon.com\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>\n" +
                "onair notify\n" +
                "<div>Icons made by <a href=\"http://www.freepik.com\" title=\"Freepik\">Freepik</a> from <a href=\"http://www.flaticon.com\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>at\n" + " settings <div>Icons made by <a href=\"http://www.flaticon.com/authors/pavel-kozlov\" title=\"Pavel Kozlov\">Pavel Kozlov</a> from <a href=\"http://www.flaticon.com\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>" +
                "Topic\n" +
                "<div>Icons made by <a href=\"http://www.freepik.com\" title=\"Freepik\">Freepik</a> from <a href=\"http://www.flaticon.com\" title=\"Flaticon\">www.flaticon.com</a> is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a></div>";
        if (textView != null) textView.setText(Menemen.fromHtmlCompat(html));
        final TextView version = (TextView) findViewById(R.id.version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versions = pInfo.versionName;//Version bilgisi çek
            if (version != null)
                version.setText(String.format("%s%s", getString(R.string.version), versions));//Version bilgisi yazdır
        }catch (Exception e){e.printStackTrace();}

    }

}
