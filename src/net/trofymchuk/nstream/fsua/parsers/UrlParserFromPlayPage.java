
package net.trofymchuk.nstream.fsua.parsers;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class UrlParserFromPlayPage {

    public static Map<String, String> parseUrls(Document doc) {
        Map<String, String> results = null;
        Elements allJavaScriptElements = doc.getElementsByAttributeValue("type", "text/javascript");

        for (int i = 0; i < allJavaScriptElements.size(); i++) {
            Element oneJavaScriptElement = allJavaScriptElements.get(i);
            String text = oneJavaScriptElement.html();
            if (text.contains("fsData:")) {
                results = sfkhf(text);
            }
        }

        return results;
    }

    public static Map<String, String> sfkhf(String javaScript) {
        Map<String, String> results = new HashMap<String, String>();
        String[] strings = javaScript.split("fsData:");
        if (strings.length > 2) {
            for (int i = 1; i < strings.length; i++) {
                String fileId = null;
                String download_url = null;

                String oneBlockOfFsData = strings[i];
                String[] allParams = oneBlockOfFsData.split(",");
                for (String string : allParams) {
                    if (string.contains("file_id")) {
                        fileId = string.split("'")[1];
                    } else {
                        if (string.contains("download_url")) {
                            download_url = string.split("'")[1];
                        }
                    }
                }

                results.put(fileId, download_url);
            }
        }

        return results;
    }

}
