package net.trofymchuk.nstream.fsuanew.model.NstreamItem;

import org.w3c.dom.Document;

/**
 * Created by bohdantp on 3/16/15.
 */
public interface NstreamItem {

    org.w3c.dom.Element serialize(String path, Document doc);

}
