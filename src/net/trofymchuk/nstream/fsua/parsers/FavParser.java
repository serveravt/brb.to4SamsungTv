package net.trofymchuk.nstream.fsua.parsers;

import java.util.ArrayList;
import java.util.List;

import net.trofymchuk.nstream.fsua.models.FsFavourite;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FavParser {
	public static List<FsFavourite> parseFavs(Document doc) {
		List<FsFavourite> results = null;
		Elements folderLIs = doc.getElementsByClass("b-poster-thin__wrapper");

		int folderCount = folderLIs.size();

		System.out.println(folderCount);
		results = new ArrayList<FsFavourite>(folderCount);
		for (int i = 0; i < folderCount; i++) {
			FsFavourite folder = parseFav(folderLIs.get(i));
			if (folder != null) {
				results.add(folder);
			} else {
				System.out.println("not found");
				// something went wrong

			}
		}

		return results;
	}

	private static String parseImgUrlFromStyle(String style) {
		String res = "";
		res = style.substring(style.indexOf("url('"));
		res = res.substring(res.indexOf("http"));
		res = res.substring(0, res.indexOf("'"));
		return res;
	}

	private static FsFavourite parseFav(Element liElement) {
		FsFavourite result = null;
		Elements aElements = liElement.getElementsByTag("a");
		if (aElements.size() > 0) {
			// searce <a> with name

			Element e = aElements.get(0);

			String url = e.attr("href");
			String img = e.attr("style");
			img = parseImgUrlFromStyle(img);
			System.out.println("img:" + img);
			String name = e.text();

			result = new FsFavourite(name, "http://brb.to" + url, img);
		}

		return result;

	}
}
