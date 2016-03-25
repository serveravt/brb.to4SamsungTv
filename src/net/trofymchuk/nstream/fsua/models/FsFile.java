package net.trofymchuk.nstream.fsua.models;

import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class FsFile implements NstreamXml {
    private final String name;
    private final String streamUrl;
    private final String playUrl;
    private final String size;
    private final String fileId;
    private final String logoImgurl;
    private String mp4Url;

    public FsFile(String name, String streamUrl, String playUrl, String size, String fileId, String logoImgurl) {
        super();
        this.name = name;
        this.streamUrl = streamUrl;
        this.playUrl = playUrl;
        this.size = size;
        this.fileId = fileId;
        this.logoImgurl = logoImgurl;
    }

    public static List<FsFile> parseFiles(org.jsoup.nodes.Document doc, String customLogourl) {
        List<FsFile> results = null;
        Elements fileLIs = doc.getElementsByAttributeValue("class", "b-file-new m-file-new_type_video");
//        System.out.println("" + doc.toString());
        int filesCount = fileLIs.size();
        results = new ArrayList<FsFile>(filesCount);
        for (int i = 0; i < filesCount; i++) {
            FsFile file = parseFile(fileLIs.get(i), customLogourl);
            if (file != null) {
                results.add(file);
            } else {
                // something went wrong
            }
        }
        return results;
    }

    private static FsFile parseFile(org.jsoup.nodes.Element liElement, String customLogourl) {
        String logoUrl = "http://tsnakeman.net.ua/widget/logo/ex.png";
        if (customLogourl != null) {
            logoUrl = customLogourl;
        }
        FsFile result = null;
        String url = null;
        String name = null;
        String playUrl = null;
        String size = null;
        String fileId = null;

        Elements aElements = liElement.getElementsByTag("a");
        if (aElements.size() > 0) {
            int sizeOfA = aElements.size();
            for (int i = 0; i < sizeOfA; i++) {
                Elements aElementsWithAttributeName = aElements.get(i).getElementsByAttributeValue("class",
                        "b-file-new__link-material-download");
                if (aElementsWithAttributeName.size() > 0) {
                    url = aElementsWithAttributeName.get(0).attr("href");
                    size = aElementsWithAttributeName.get(0).text();
                    fileId = aElementsWithAttributeName.get(0).attr("id").replace("dl_", "");
                }

                Elements aElementsOfPlayurl = aElements.get(i).getElementsByAttributeValue("class",
                        "b-file-new__link-material");
                if (aElementsOfPlayurl.size() > 0) {
                    playUrl = aElementsOfPlayurl.get(0).attr("href");
                }

            }
        }
        Elements span = liElement.getElementsByAttributeValue("class", "b-file-new__link-material-filename-text");
        if (span.size() > 0) {
            name = span.get(0).text();
        }
        if (url != null && name != null) {
            result = new FsFile(name, url, playUrl, size, fileId, logoUrl);
        }
        return result;
    }

    public void setMp4Url(String mp4Url) {
        this.mp4Url = mp4Url;
    }

    public String getName() {
        return name;
    }

    public String getStreamUrl() {

        if (mp4Url != null)
            return mp4Url;
        else
            return streamUrl;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public String getSize() {
        return size;
    }

    public String getFileId() {
        return fileId;
    }

    @Override
    public Element getNstreamXmlItem(Document doc) {
        String url = "http://brb.to" + getStreamUrl();
        // channel item
        Element channelElement = doc.createElement("channel");

        // title
        Element titleElement = doc.createElement("title");
        String name = getName();

        // if (name.length() > 5) {
        // name = name.substring(0, 5);
        // }

        titleElement.appendChild(doc.createCDATASection(name));
        channelElement.appendChild(titleElement);

        // playlist_url
        Element streamElement = doc.createElement("stream_url");
        streamElement.appendChild(doc.createCDATASection(url));
        channelElement.appendChild(streamElement);

        // description
        Element descriptionElement = doc.createElement("description");
        descriptionElement.appendChild(doc.createCDATASection(name + "<br> " + getSize()));
        channelElement.appendChild(descriptionElement);

        // logo_30x30
        Element logoElement = doc.createElement("logo_30x30");
        logoElement.appendChild(doc.createTextNode(logoImgurl));
        channelElement.appendChild(logoElement);
        return channelElement;
    }

}
