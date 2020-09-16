package extractdata.extractors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GPSExtractor {

	private static final String GEO_TAG = "geo_table";
	private static final String HREF = "href";
	private static final String HTTP = "https:";
	private static final String LINK_TAG = "a";
	
	public static List<String> extract(Document doc, String path, String id) {
		List<String> gpsFiles = new ArrayList<>();
		try {
			Elements ls = doc.getElementById(GEO_TAG).getElementsByTag(LINK_TAG);
			for (Element link : ls) {
				String linkHref = link.attr(HREF);
				if (linkHref.startsWith(HTTP)) {
					File localFile = new File(path + id + "/" + FilenameUtils.getBaseName(linkHref) + "." + FilenameUtils.getExtension(linkHref));
					gpsFiles.add(localFile.getName());
					if (!localFile.exists()) {
						FileUtils.copyURLToFile(new URL(linkHref), localFile);
					}
				}
			}
		} catch (Exception e) {}
		return gpsFiles;
	}

}
