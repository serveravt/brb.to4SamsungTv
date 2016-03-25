package net.trofymchuk.nstream.fsuanew;

import net.trofymchuk.nstream.fsua.Config;
import net.trofymchuk.nstream.fsuanew.model.FsVideoItem;
import net.trofymchuk.nstream.fsuanew.parser.FavouriteParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Created by bohdantp on 3/16/15.
 */
public class BodoCool {

    private static final String DOMEN = "http://fs.to";
    private static final String url_plus = "?ajax&folder=";
    private static final String LOGIN_URL = DOMEN + "/login.aspx";
    private static final String FAVOURITES_URL = DOMEN + "/myfavourites.aspx";
    private static Semaphore sem;
    private static ExecutorService exec = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        System.out.println("new cool parser FS.UA");
        Config.readConfig();
        System.out.println("config is read");
        Utils.clearFolder(Config.LOCAL_DROPBOX_ITEMS_FOLDER_PATH);
        System.out.println("Folder: " + Config.LOCAL_DROPBOX_ITEMS_FOLDER_PATH + " is cleaned");
        Map<String, String> cookies = null;
        try {
            cookies = loginAndGetCookies(Config.USER_NAME, Config.PASSWORD);
//            System.out.println("cookies:" + cookies);
            describeFavs(cookies);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Problem!!!!");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("All work is done in " + ((endTime - startTime) / 1000) + " seconds");
        System.exit(0);
    }

    private static Map<String, String> loginAndGetCookies(String login, String password) throws IOException {
        Map<String, String> creds = new HashMap<String, String>();
        creds.put("login", login);
        creds.put("passwd", password);
        System.out.println("Try to login as " + login);
        Connection.Response res = Jsoup.connect(LOGIN_URL).data(creds).method(Connection.Method.POST).execute();
        Document soc1 = res.parse();
        boolean isLoginOk = soc1.getElementsByClass("b-header__user-profile-title").size() > 0;
        System.out.println("Login ok = " + isLoginOk);
        return res.cookies();
    }

    private static void describeFavs(final Map<String, String> cookies) throws IOException, ParserConfigurationException {
        final Document doc = Jsoup.connect(FAVOURITES_URL).cookies(cookies).get();
        final List<FsVideoItem> items = FavouriteParser.parseFavourites(doc);
        int permits = items.size();
        System.out.println("Favourites size = " + permits);
        sem = new Semaphore(permits);
        try {
            sem.acquire(permits);
            for (final FsVideoItem i : items) {
                exec.execute(new Runnable() {
                    @Override
                    public void run() {
                        i.parseIntoNstreamModel(cookies);
                        sem.release();
                    }
                });
            }
            sem.acquire(permits);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        org.w3c.dom.Document d = docBuilder.newDocument();
        Element itemsElement = d.createElement("items");
        d.appendChild(itemsElement);

        for (FsVideoItem i : items) {
            itemsElement.appendChild(i.serialize(Config.LOCAL_DROPBOX_ITEMS_FOLDER_PATH, d));
        }

        XMLWriter.serializeToFile(Config.LOCAL_DROPBOX_ITEMS_FOLDER_PATH + File.separator + Config.DROP_BOX_FILE_NAME, d, "NAME");

    }
}
