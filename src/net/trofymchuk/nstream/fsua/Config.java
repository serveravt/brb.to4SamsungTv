package net.trofymchuk.nstream.fsua;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

	public static final String DROP_PREFIX = "https://dl.dropboxusercontent.com/u/";

	public static boolean nstream30 = true;
	public static String userId = "";
	public static String folderName = "";
	public static String publicPath = "";

	public static String DROPBOX_FOLDER_URL = "";
	public static String LOCAL_DROPBOX_ITEMS_FOLDER_PATH = "";

	public static String DROP_BOX_FILE_NAME = "fsFav.txt";

	public static String USER_NAME = "";
	public static String PASSWORD = "";

	public static String TOR_LOGIN = "";
	public static String TOR_PASS = "";
	public static String TOR_LOC_IP = "";

	public static void readConfig() {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream("config.ini"));
			userId = p.getProperty("user_id");
			publicPath = p.getProperty("public_path", "");
			folderName = p.getProperty("folder_name");
			USER_NAME = p.getProperty("user_name");
			PASSWORD = p.getProperty("pass");

			TOR_LOGIN = p.getProperty("TOR_LOGIN");
			TOR_PASS = p.getProperty("TOR_PASS");
			TOR_LOC_IP = p.getProperty("TOR_LOC_IP");

			DROPBOX_FOLDER_URL = DROP_PREFIX + userId + "/" + folderName + "/";
			LOCAL_DROPBOX_ITEMS_FOLDER_PATH = publicPath + folderName;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
