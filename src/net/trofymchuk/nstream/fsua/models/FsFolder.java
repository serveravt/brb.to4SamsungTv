package net.trofymchuk.nstream.fsua.models;

import net.trofymchuk.nstream.fsua.Config;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FsFolder implements NstreamXml {
    private final String name;
    private final String id;
    private final String fileNameForFolder;
    private final String logoImgurl;
    private final boolean originalName;

    public FsFolder(String name, String id, String logoImgurl) {
        this(name, id, logoImgurl, false);
    }

    public FsFolder(String name, String id, String logoImgurl, boolean originalName) {
        super();
        this.name = name;
        this.id = id;
        this.logoImgurl = logoImgurl;
        this.originalName = originalName;
        if (!originalName) {
            fileNameForFolder = getFileNameForFolder();
        } else {
            fileNameForFolder = name;
        }
    }

    public static List<FsFolder> parseFolders(org.jsoup.nodes.Document doc, String customLogourl) {
        List<FsFolder> results = null;
        Elements folderLIs = doc.getElementsByAttributeValueContaining("class", "folder");

        int folderCount = folderLIs.size();
        results = new ArrayList<FsFolder>(folderCount);
        for (int i = 0; i < folderCount; i++) {
            FsFolder folder = parseFolder(folderLIs.get(i), customLogourl);
            if (folder != null) {
                results.add(folder);
            } else {
                // something went wrong
            }
        }
        return results;
    }

    private static FsFolder parseFolder(org.jsoup.nodes.Element liElement, String customLogourl) {
        FsFolder result = null;
        String logoUrl = "http://tsnakeman.net.ua/widget/logo/ex.png";
        if (customLogourl != null) {
            logoUrl = customLogourl;
        }
        Elements aElements = liElement.getElementsByTag("a");
        Elements mat = liElement.getElementsByAttributeValue("class", "material-size");
        String size = "";
        if (mat.size() > 0) {
            for (int i = 0; i < mat.size(); i++) {
                size = size + mat.get(i).text() + " ";
            }
        }

        Elements matd = liElement.getElementsByAttributeValue("class", "material-details");
        String filesCount = "";
        if (matd.size() > 0) {
            for (int i = 0; i < matd.size(); i++) {
                filesCount = filesCount + matd.get(i).text();
            }
        }

        if (aElements.size() > 0) {
            // searce <a> with name
            int sizeOfA = aElements.size();
            for (int i = 0; i < sizeOfA; i++) {
                Elements aElementsWithAttributeName = aElements.get(i).getElementsByAttribute("name");
                if (aElementsWithAttributeName.size() > 0) {
                    org.jsoup.nodes.Element e = aElementsWithAttributeName.get(0);
                    String nameWithId = e.attr("name");
                    String cl = e.attr("class");
                    if (cl.length() > 0 && cl.contains("subtype")) {
                        cl = cl.substring(cl.indexOf("link"));
                        int bi = cl.indexOf("m-");
                        if (bi > -1) {
                            cl = cl.substring(bi);
                            cl = cl.substring(2);
                            cl = cl.substring(0, cl.indexOf(" "));
                            cl = " (" + cl.toUpperCase() + ") ";
                        } else {
                            cl = "";
                        }
                    } else {
                        cl = " ";
                    }

                    String name = e.text() + cl + size + filesCount;

                    result = new FsFolder(name, nameWithId.replace("fl", ""), logoUrl);
                }
            }
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileNameForFolder;
    }

    private String getFileNameForFolder() {
        return UUID.randomUUID().toString();

    }

    @Override
    public Element getNstreamXmlItem(Document doc) {
        String url = Config.DROPBOX_FOLDER_URL + getFileName();

        // channel item
        Element channelElement = doc.createElement("channel");

        // title
        Element titleElement = doc.createElement("title");
        titleElement.appendChild(doc.createCDATASection(getName()));
        channelElement.appendChild(titleElement);

        // playlist_url
        Element playListUrlElement = doc.createElement("playlist_url");
        playListUrlElement.appendChild(doc.createCDATASection(url));
        channelElement.appendChild(playListUrlElement);

        // description
        Element descriptionElement = doc.createElement("description");
        descriptionElement.appendChild(doc.createCDATASection(getName()));
        channelElement.appendChild(descriptionElement);

        // logo_30x30
        Element logoElement = doc.createElement("logo_30x30");
        logoElement.appendChild(doc.createTextNode(logoImgurl));
        channelElement.appendChild(logoElement);
        return channelElement;
    }

}
