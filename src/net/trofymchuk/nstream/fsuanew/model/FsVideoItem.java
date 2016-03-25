package net.trofymchuk.nstream.fsuanew.model;

import net.trofymchuk.nstream.fsua.Config;
import net.trofymchuk.nstream.fsuanew.XMLWriter;
import net.trofymchuk.nstream.fsuanew.model.NstreamItem.NstreamItem;
import net.trofymchuk.nstream.fsuanew.model.NstreamItem.NstreamPlayList;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Created by bohdantp on 3/16/15.
 */
public class FsVideoItem {
    private static ExecutorService exec = Executors.newFixedThreadPool(1);
    private final String title;
    private final String url;
    private final String coverUrl;
    private final String fileName;
    List<NstreamItem> items = new ArrayList<NstreamItem>();
    private Semaphore sem;
    private Object sync = new Object();

    public FsVideoItem(String title, String url, String coverUrl) {
        this.title = title;
        this.url = url;
        this.coverUrl = coverUrl;
        this.fileName = getFileNameForFolder();
    }

    private static String getFileNameForFolder() {
        return UUID.randomUUID().toString();

    }

    public void parseIntoNstreamModel(final Map<String, String> cookies) {
        Document document = null;
        try {
            System.out.println("Start parse: " + title);
//            System.out.println("go to:" + url);
            document = Jsoup.connect(url).cookies(cookies).get();
            Elements folderLIs = document.getElementsByAttributeValue("class", "b-files-folders-link");
            String rel = folderLIs.get(0).attr("rel");
            String relId = parseRelId(rel);
            String rightUrl = buildFolderUrl(url, relId, "0");
            document = Jsoup.connect(rightUrl).cookies(cookies).get();
            Elements rels = document.getElementsByAttribute("rel");
            int relsize = rels.size();
            final String base = buildFolderUrl(url, relId, "");
            sem = new Semaphore(relsize);
            try {
                sem.acquire(relsize);
                for (int i = 0; i < relsize; i++) {
                    final Element relEl = rels.get(i);
                    String re = relEl.attr("rel");
                    final String parentId = parseParentIdFromRel(re);
                    exec.execute(new Runnable() {
                        @Override
                        public void run() {
                            NstreamPlayList list = NstreamPlayList.parseFsFolder(relEl.text(), relEl.text(), cookies, base, parentId, coverUrl);
                            synchronized (sync) {
                                items.add(list);
                            }
                            sem.release();
                        }
                    });
                }
                sem.acquire(relsize);
                System.out.println("END parse: " + title);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildFolderUrl(String url, String itemId, String folderId) {
        return url + "?ajax&id=" + itemId + "&view=1&folder_lang=null&folder=" + folderId;
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

    private String parseRelId(String rel) {
        String r = rel;
        r = r.substring(r.indexOf("item_id") + 10);
        r = r.substring(0, r.indexOf("'"));
        return r;
    }

    public org.w3c.dom.Element serialize(String path, org.w3c.dom.Document doc) throws ParserConfigurationException {
        String url = Config.DROPBOX_FOLDER_URL + fileName;

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
            XMLWriter.serializeToFile(path + File.separator + fileName, d, title);
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
        descriptionElement.appendChild(doc.createCDATASection(title));
        channelElement.appendChild(descriptionElement);

        // logo_30x30
        org.w3c.dom.Element logoElement = doc.createElement("logo_30x30");
        logoElement.appendChild(doc.createTextNode(coverUrl));
        channelElement.appendChild(logoElement);
        return channelElement;

    }

}
