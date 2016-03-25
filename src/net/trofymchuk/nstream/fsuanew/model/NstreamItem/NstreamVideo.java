package net.trofymchuk.nstream.fsuanew.model.NstreamItem;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;

/**
 * Created by bohdantp on 3/16/15.
 */
public class NstreamVideo implements NstreamItem {

    private String url;
    private String title;
    private String imgUrl;

    public static NstreamVideo parseFromElement(Element element, String imgUrl) {
        NstreamVideo res = new NstreamVideo();
        Elements el = element.getElementsByAttributeValueContaining("class", "download");
        res.url = "http://fs.to" + el.get(0).attr("href");
        res.title = element.text();
        res.imgUrl = imgUrl;
//        System.out.println("url:  " + "http://fs.to" + url);
        return res;
    }

    @Override
    public org.w3c.dom.Element serialize(String path, Document doc) {
        org.w3c.dom.Element channelElement = doc.createElement("channel");
        org.w3c.dom.Element titleElement = doc.createElement("title");
        String name = title;
        titleElement.appendChild(doc.createCDATASection(name));
        channelElement.appendChild(titleElement);
        org.w3c.dom.Element streamElement = doc.createElement("stream_url");
        streamElement.appendChild(doc.createCDATASection(url));
        channelElement.appendChild(streamElement);
        org.w3c.dom.Element descriptionElement = doc.createElement("description");
        descriptionElement.appendChild(doc.createCDATASection(name + "<br> "));
        channelElement.appendChild(descriptionElement);
        org.w3c.dom.Element logoElement = doc.createElement("logo_30x30");
        logoElement.appendChild(doc.createTextNode(imgUrl));
        channelElement.appendChild(logoElement);
        return channelElement;
    }
}
