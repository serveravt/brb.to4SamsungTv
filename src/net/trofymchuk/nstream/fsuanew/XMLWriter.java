package net.trofymchuk.nstream.fsuanew;

import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by bohdantp on 3/16/15.
 */
public class XMLWriter {

    public static void serializeToFile(String path, Document doc, String name) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            DOMSource so = new DOMSource(doc);
            StreamResult sr = new StreamResult(new FileOutputStream(new File(path)));
            transformer.transform(so, sr);
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


}
