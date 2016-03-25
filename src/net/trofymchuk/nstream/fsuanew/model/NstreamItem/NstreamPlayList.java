package net.trofymchuk.nstream.fsuanew.model.NstreamItem;

import net.trofymchuk.nstream.fsua.Config;
import net.trofymchuk.nstream.fsuanew.XMLWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by bohdantp on 3/16/15.
 */
public class NstreamPlayList implements NstreamItem {

    private List<NstreamItem> items = new ArrayList<NstreamItem>();
    private String title;
    private String filename;
    private String imgUrl;
    private String description;

    public static NstreamPlayList parseFsFolder(String title, String descr, Map<String, String> cookies, String baseUrl, String folderId, String imgUrl) {
        NstreamPlayList res = new NstreamPlayList();
        res.title = title;
        res.filename = getFileNameForFolder();
        res.imgUrl = imgUrl;
        res.description = descr;
        String url = baseUrl + folderId;
        Document doc = null;
        try {
            doc = Jsoup.connect(url).cookies(cookies).get();
            Elements folders = doc.getElementsByClass("folder");
            int relsize = folders.size();
            for (int i = 0; i < relsize; i++) {
                Element folderElement = folders.get(i);
                String folderDescription = folderElement.text();
                Element titleElement = folderElement.getElementsByClass("title").get(0);
                String folderTitle = titleElement.text();
                String re = titleElement.attr("rel");
                String parentId = parseParentIdFromRel(re);

                if (parentId.length() > 2) {
                    res.items.add(NstreamPlayList.parseFsFolder(folderTitle, folderDescription, cookies, baseUrl, parentId, imgUrl));
                }
            }

            Elements files = doc.getElementsByClass("b-file-new");
            int filessize = files.size();
            for (int i = 0; i < filessize; i++) {
                Element element = files.get(i);
                res.items.add(NstreamVideo.parseFromElement(element, imgUrl));
            }
//            System.out.println("End parse folder: " + title);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    private static String parseParentIdFromRel(String rel) {
        String r = rel;
        r = r.substring(r.indexOf("parent_id") + 9);
        int i = r.indexOf(",");
        if (i > -1) {
            r = r.substring(0, i);
        }
        return cutDigits(r);
    }

    private static String cutDigits(String s) {
        String res = s.replaceAll("\\D+", "");
        return res;
    }

    private static String getFileNameForFolder() {
        return UUID.randomUUID().toString();

    }


    @Override
    public org.w3c.dom.Element serialize(String path, org.w3c.dom.Document doc) {
        String url = Config.DROPBOX_FOLDER_URL + filename;

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            org.w3c.dom.Document d = docBuilder.newDocument();
            org.w3c.dom.Element itemsElement = d.createElement("items");
            d.appendChild(itemsElement);
            for (NstreamItem i : items) {
                itemsElement.appendChild(i.serialize(path, d));
            }
            XMLWriter.serializeToFile(path + File.separator + filename, d, title);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        // channel item
        org.w3c.dom.Element channelElement = doc.createElement("channel");

        // title
        org.w3c.dom.Element titleElement = doc.createElement("title");
        titleElement.appendChild(doc.createCDATASection(title));
        channelElement.appendChild(titleElement);

        // playlist_url
        org.w3c.dom.Element playListUrlElement = doc.createElement("playlist_url");
        playListUrlElement.appendChild(doc.createCDATASection(url));
        channelElement.appendChild(playListUrlElement);

        // description
        org.w3c.dom.Element descriptionElement = doc.createElement("description");
        descriptionElement.appendChild(doc.createCDATASection(description));
        channelElement.appendChild(descriptionElement);

        // logo_30x30
        org.w3c.dom.Element logoElement = doc.createElement("logo_30x30");
        logoElement.appendChild(doc.createTextNode(imgUrl));
        channelElement.appendChild(logoElement);
        return channelElement;
    }
}
