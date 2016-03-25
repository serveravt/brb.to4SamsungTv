package net.trofymchuk.nstream.fsua.models;

import java.util.UUID;

import net.trofymchuk.nstream.fsua.Config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FsFavourite implements NstreamXml {
    private final String name;
    private final String url;
    private final String fileNameForFolder;
    private final String imgUrl;

    public FsFavourite(String name, String streamUrl, String imgUrl) {
        super();
        this.name = name;
        this.url = streamUrl;
        this.imgUrl = imgUrl;
        fileNameForFolder = getFileNameForFolder();
    }

    public String getName() {
        return name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getStreamUrl() {
        return url;
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
        // String logoUrl = "http://tsnakeman.net.ua/widget/logo/ex.png";
        String logoUrl = getImgUrl();

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
        logoElement.appendChild(doc.createTextNode(logoUrl));
        channelElement.appendChild(logoElement);
        return channelElement;
    }

}
