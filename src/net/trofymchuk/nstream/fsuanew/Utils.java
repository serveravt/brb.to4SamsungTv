package net.trofymchuk.nstream.fsuanew;

import java.io.File;

/**
 * Created by bohdantp on 3/16/15.
 */
public class Utils {

    public static void clearFolder(String path) {
        File f = new File(path);
        File[] list = f.listFiles();
        if (list == null) {
            return;
        }
        for (File g : list) {
            g.delete();
        }
    }

}
