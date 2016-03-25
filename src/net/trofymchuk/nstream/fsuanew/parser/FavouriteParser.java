package net.trofymchuk.nstream.fsuanew.parser;

import net.trofymchuk.nstream.fsuanew.model.FsVideoItem;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bohdantp on 3/16/15.
 */
public class FavouriteParser {

    public static List<FsVideoItem> parseFavourites(Document doc) {
        List<FsVideoItem> res = null;
        Elements folderLIs = doc.getElementsByClass("b-poster-thin__wrapper");
        int folderCount = folderLIs.size();
        res = new ArrayList<FsVideoItem>(folderCount);
        for (int i = 0; i < folderCount; i++) {
            FsVideoItem folder = parseFav(folderLIs.get(i));
            if (folder != null) {
                res.add(folder);
            } else {
                System.out.println("not found");
            }
        }
        return res;
    }

    private static String parseImgUrlFromStyle(String style) {
        String res = "";
        res = style.substring(style.indexOf("url('"));
        res = res.substring(res.indexOf("//"));
        res = res.substring(0, res.indexOf("'"));
        res += "http";
        return res;
    }

    private static FsVideoItem parseFav(Element liElement) {
        FsVideoItem result = null;
        Elements aElements = liElement.getElementsByTag("a");
        if (aElements.size() > 0) {
            Element e = aElements.get(0);
            String url = e.attr("href");
            String img = e.attr("style");
            img = parseImgUrlFromStyle(img);
//            System.out.println("img:" + img);
            String name = e.text();
            result = new FsVideoItem(name, "http://brb.to" + url, img);
        }
        return result;
    }

}
