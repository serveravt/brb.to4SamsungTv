package net.trofymchuk.nstream.fsua;

import net.trofymchuk.nstream.fsua.models.FsFavourite;
import net.trofymchuk.nstream.fsua.models.FsFile;
import net.trofymchuk.nstream.fsua.models.FsFolder;
import net.trofymchuk.nstream.fsua.parsers.FavParser;
import net.trofymchuk.nstream.fsua.parsers.UrlParserFromPlayPage;
import net.trofymchuk.nstream.fsua.xml.ToXml;
import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Ololo {

    private static final String DOMEN = "http://fs.to";
    private static final String url_plus = "?ajax&folder=";
    private static final String LOGIN_URL = DOMEN + "/login.aspx";
    private static final String FAVOURITES_URL = DOMEN + "/myfavourites.aspx";
    private static Object o = new Object();
    private static int permits = 30;
    static Semaphore s = new Semaphore(permits);
    private static ExecutorService ex = Executors.newFixedThreadPool(permits);

    private static void clearFolder(String path) {
        File f = new File(path);
        File[] list = f.listFiles();
        if (list == null) {
            return;
        }
        for (File g : list) {
            g.delete();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Config.readConfig();
        clearFolder(Config.LOCAL_DROPBOX_ITEMS_FOLDER_PATH);
        Map<String, String> cookies;
        try {
            cookies = loginAndGetCookies(Config.USER_NAME, Config.PASSWORD);
            describeFavs(cookies);
            System.out.println("DONE by "
                    + ((System.currentTimeMillis() - start) / 1000) + " secs");
        } catch (IOException e) {
            System.out.println("Problem!!!!");
        }

        System.exit(0);
    }

    private static Map<String, String> loginAndGetCookies(String login,
                                                          String password) throws IOException {
        Map<String, String> creds = new HashMap<String, String>();
        creds.put("login", login);
        creds.put("passwd", password);
        System.out.println("Try to login as " + login);
        Connection.Response res = Jsoup.connect(LOGIN_URL).data(creds)
                .method(Method.POST).execute();
        Document soc1 = res.parse();
        boolean isLoginOk = soc1.getElementsByClass(
                "b-header__user-profile-title").size() > 0;
        System.out.println("Login ok = " + isLoginOk);
        return res.cookies();
    }

    private static void describeUrl(final String baseUrl, final String url, final String fileName, final Map<String, String> cookies,
                                    final String rootName, final String customImgUrl) {
        ex.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    s.acquire();
                    Document doc;
                    System.out.println(rootName);
                    doc = Jsoup.connect(url).cookies(cookies).get();
                    List<FsFolder> folders = FsFolder.parseFolders(doc, customImgUrl);
                    List<FsFile> files = FsFile.parseFiles(doc, customImgUrl);

                    if (!files.isEmpty()) {
                        // load special urls for files
                        Document docPlay = Jsoup.connect(DOMEN + files.get(0).getPlayUrl()).cookies(cookies).get();
                        Map<String, String> fileIdAndsUrls = UrlParserFromPlayPage.parseUrls(docPlay);
                        for (FsFile fsFile : files) {
                            String mp4Url = fileIdAndsUrls.get(fsFile.getFileId());
                            if (mp4Url != null) {
                                // System.out.println("MP4Url found = " +
                                // mp4Url);
                                fsFile.setMp4Url(mp4Url);
                            }
                        }
                    }
                    String result = ToXml.convertToString(rootName, folders, null, files);
                    // System.out.println(result);
                    File f = new File(Config.LOCAL_DROPBOX_ITEMS_FOLDER_PATH + File.separator + fileName);
                    FileUtils.write(f, result, "UTF-8");
                    for (FsFolder folder : folders) {
                        String newUrl = baseUrl + url_plus + folder.getId();
                        describeUrl(baseUrl, newUrl, folder.getFileName(), cookies, folder.getName(), customImgUrl);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    s.release();
                }
            }
        });

    }

    private static void describeFavs(Map<String, String> cookies)
            throws IOException {
        Document doc;
        doc = Jsoup.connect(FAVOURITES_URL).cookies(cookies).get();
        // System.out.println(doc.html());
        List<FsFavourite> folders = FavParser.parseFavs(doc);
        System.out.println("Favourites size = " + folders.size());
        String result = ToXml
                .convertToString("Favourites", null, folders, null);
        // System.out.println(result);
        File f = new File(Config.LOCAL_DROPBOX_ITEMS_FOLDER_PATH
                + File.separator + Config.DROP_BOX_FILE_NAME);
        FileUtils.write(f, result, "UTF-8");
        try {
//            List<FsFolder> list = Parser.main(Config.LOCAL_DROPBOX_ITEMS_FOLDER_PATH);
//            String r = ToXml.convertToString("torrent", list, null, null);
//            File f2 = new File(Config.LOCAL_DROPBOX_ITEMS_FOLDER_PATH + File.separator + "torrent.txt");
//            FileUtils.write(f2, r, "UTF-8");

        } catch (Exception e1) {
            e1.printStackTrace();
        }

        for (FsFavourite folder : folders) {
            String newUrl = folder.getStreamUrl() + url_plus + "0";
            describeUrl(folder.getStreamUrl(), newUrl, folder.getFileName(), cookies, folder.getName(), folder.getImgUrl());
        }

        try {
            Thread.sleep(500);
            s.acquire(permits);
            s.release(permits);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
