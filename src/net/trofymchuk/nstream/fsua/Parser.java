package net.trofymchuk.nstream.fsua;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.trofymchuk.nstream.fsua.models.FsFolder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Parser {

	private static String login ;
	private static String pass ;
	static String ip ;

	// static String mainDir = "D:\\torrent_tv_note";
	static String mainDir = "D:\\torrent_tv_pc";
	// static String ip = "http://192.168.1.4:7777/";
	

	// private static String link =
	// "http://api.torrent-tv.ru/v2_alltranslation.php?session=E9gIVNc8uPAVjAioxPfbgzPX&type=all&typeresult=xml";

	public static ArrayList<FsFolder> main(String folderpath) throws Exception {
		mainDir = folderpath;
		login = Config.TOR_LOGIN;
		pass = Config.TOR_PASS;
		ip = Config.TOR_LOC_IP;
		String id = getSessionId();
		String link = "http://api.torrent-tv.ru/v2_alltranslation.php?session="
				+ id + "&type=all&typeresult=xml";
		URL url = new URL(link);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.connect();
		InputStream i = conn.getInputStream();
		String s = streamToString(i);
		System.out.println(s);
		ArrayList<FsFolder> res = parse(s);
		i.close();
		return res;
	}

	private static String getSessionId() throws Exception {
		String res = "";

		String u = "http://api.torrent-tv.ru/v2_auth.php?username=" + login
				+ "&password=" + pass + "&application=tsproxy&typeresult=xml";

		URL url = new URL(u);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.connect();
		InputStream i = conn.getInputStream();
		String s = streamToString(i);
		i.close();

		System.out.println("s: " + s);

		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(s));
		Document doc = db.parse(is);
		Node documentElement = doc.getDocumentElement();
		System.out.println("" + documentElement.getNodeName());
		NodeList cat = doc.getElementsByTagName("session");
		System.out.println("len: " + cat.getLength());

		for (int temp = 0; temp < cat.getLength(); temp++) {
			Node nNode = cat.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				String h = eElement.getTextContent();
				System.out.println("H: " + h);
				return h;
			}

		}

		return res;
	}

	private static ArrayList<FsFolder> parse(String s) throws Exception {
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		List<Category> cats = new ArrayList<Category>();
		List<Channel> chs = new ArrayList<Channel>();

		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(s));
		Document doc = db.parse(is);

		Node documentElement = doc.getDocumentElement();
		System.out.println("" + documentElement.getNodeName());

		NodeList cat = doc.getElementsByTagName("category");
		for (int temp = 0; temp < cat.getLength(); temp++) {
			Node nNode = cat.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				cats.add(Category.parse(eElement));
			}

		}

		NodeList ch = doc.getElementsByTagName("channel");
		for (int temp = 0; temp < ch.getLength(); temp++) {
			Node nNode = ch.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				chs.add(Channel.parse(eElement));
			}

		}

		System.out.println("chs: " + chs.size());
		return next(cats, chs);

	}

	private static String streamToString(InputStream input) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(input,
				"UTF-8"));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		return response.toString();
	}

	private static class Category {
		int id;
		String name;
		int adult;
		int position;

		public static Category parse(Element e) {
			Category res = new Category();
			res.id = Integer.parseInt(e.getAttribute("id"));
			res.adult = Integer.parseInt(e.getAttribute("adult"));
			res.name = e.getAttribute("name");
			res.position = Integer.parseInt(e.getAttribute("position"));
			System.out.println(res.name);
			return res;
		}
	}

	private static class Channel {
		int id;
		String name;
		String group;
		String type;
		String source;

		public static Channel parse(Element e) {
			Channel res = new Channel();
			res.id = Integer.parseInt(e.getAttribute("id"));
			res.group = e.getAttribute("group");
			res.name = e.getAttribute("name");
			res.type = e.getAttribute("type");
			res.source = e.getAttribute("source");
			return res;
		}
	}

	private static ArrayList<FsFolder> next(List<Category> cats,
			List<Channel> channels) throws Exception {
		ArrayList<FsFolder> res = new ArrayList<FsFolder>();
		for (Category c : cats) {
			String p = mainDir + File.separator + c.name + ".m3u";
			File f = new File(p);
			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(f), "UTF-8"));
			writer.write("#EXTM3U");
			writer.write("\n");

			for (Channel ch : channels) {
				if (String.valueOf(c.id).equals(ch.group)) {
					writer.write("#EXTINF:-1, " + ch.name);
					writer.write("\n");
					writer.write(ip + "?cmd=play&file=" + ch.id + "&type="
							+ ch.type);
					writer.write("\n");
				}
			}
			res.add(addNstream(f));
			writer.close();
		}

		String p = mainDir + File.separator + "Все" + ".m3u";
		File f = new File(p);
		if (!f.getParentFile().exists()) {
			f.mkdirs();
		}
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(f), "UTF-8"));
		writer.write("#EXTM3U");
		writer.write("\n");
		for (Channel ch : channels) {
			writer.write("#EXTINF:-1, " + ch.name);
			writer.write("\n");
			writer.write(ip + "?cmd=play&file=" + ch.id + "&type=" + ch.type);
			writer.write("\n");
		}
		res.add(addNstream(f));
		writer.close();

		return res;
	}

	private static FsFolder addNstream(File f) {
		FsFolder fs = new FsFolder(f.getName(), f.getName(), "http://tsnakeman.net.ua/widget/logo/ex.png",true);
		return fs;
	}

}
