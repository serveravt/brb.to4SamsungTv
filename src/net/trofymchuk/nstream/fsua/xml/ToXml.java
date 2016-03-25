package net.trofymchuk.nstream.fsua.xml;

import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.trofymchuk.nstream.fsua.Config;
import net.trofymchuk.nstream.fsua.models.FsFavourite;
import net.trofymchuk.nstream.fsua.models.FsFile;
import net.trofymchuk.nstream.fsua.models.FsFolder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ToXml {
    public static String convertToString(String playListName, List<FsFolder> folders, List<FsFavourite> favorites,
            List<FsFile> files) {

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // items tag
            Document doc = docBuilder.newDocument();
            Element itemsElement = doc.createElement("items");
            doc.appendChild(itemsElement);

            // <playlist_name><![CDATA[��������������
            // ������������]]></playlist_name>
            Element nameElement = doc.createElement("playlist_name");
            // nameElement.appendChild(doc.createTextNode(playListName));
            nameElement.appendChild(doc.createCDATASection(playListName));
            itemsElement.appendChild(nameElement);

            if (files != null)
                for (FsFile file : files) {
                    itemsElement.appendChild(file.getNstreamXmlItem(doc));
                }
            if (folders != null)
                for (FsFolder folder : folders) {
                    itemsElement.appendChild(folder.getNstreamXmlItem(doc));
                }
            if (favorites != null)
                for (FsFavourite folder : favorites) {
                    itemsElement.appendChild(folder.getNstreamXmlItem(doc));
                }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            StringWriter writer = new StringWriter();
            DOMSource so = new DOMSource(doc);
            StreamResult sr = new StreamResult(writer);
            transformer.transform(so, sr);
            String output = writer.getBuffer().toString();// .replaceAll("\n|\r",
                                                          // "");
            return output;

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
        return null;
    }

}
