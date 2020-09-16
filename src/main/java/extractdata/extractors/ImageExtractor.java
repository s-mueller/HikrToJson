package extractdata.extractors;

import com.google.gson.Gson;
import extractdata.pojo.ImageWaypointPojo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import extractdata.pojo.Image;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageExtractor {

    private static final String FILES_URL = "https://f.hikr.org/files/";
    private static final String PDF = ".pdf";
    private static final String HTML = ".html";
    private static final String PHOTO = "photo";
    private static final String PDF_LOGO_URL = "https://s.hikr.org/images/pdflogo.png";
    private static final String GALLERY_TAG = "new_gallery";
    private static final String SRC_TAG = "src";
    private static final String IMAGE_TAG = "img";
    private static final String QT_LOGO_URL = "https://s.hikr.org/images/qtlogo.png";
    private static final String MOV = ".mov";

    public static List<Image> getImageLinks(Document doc, String postId) {
        List<Image> imageIDs = new ArrayList<>();
        Element galleryElement = doc.getElementById(GALLERY_TAG);
        Elements images = galleryElement == null ? new Elements() : galleryElement.select(IMAGE_TAG);
        Element linkElement = doc.getElementById(GALLERY_TAG);
        Elements hrefs = linkElement == null ? new Elements() : linkElement.select("a");

        if (!images.isEmpty()) {
            int imageCounter = 1;
            for (Element image : images) {
                String url = image.absUrl(SRC_TAG);
                if (url.equals(PDF_LOGO_URL)) {
                    Element href = hrefs.get(imageCounter - 1);
                    String newURL = href.toString();
                    newURL = newURL.substring(newURL.indexOf(PHOTO) + PHOTO.length(), newURL.indexOf(HTML));
                    url = FILES_URL + newURL + PDF;
                    System.out.println("Fixed URL to: " + url);
                } else if (url.equals(QT_LOGO_URL)) {
                    Element href = hrefs.get(imageCounter - 1);
                    String newURL = href.toString();
                    newURL = newURL.substring(newURL.indexOf(PHOTO) + PHOTO.length(), newURL.indexOf(HTML));
                    url = FILES_URL + newURL + MOV;
                    System.out.println("Fixed URL to: " + url);
                } else {
                    url = url.substring(0, url.length() - 5) + url.substring(url.length() - 4);
                }
                imageIDs.add(getImage(url, postId));
                imageCounter++;
            }
        }
        return imageIDs;
    }

    private static Image getImage(String url, String id) {
        try {
            String imageID = getImageID(url);
            String imageUrl = "https://www.hikr.org/gallery/photo" + imageID + ".html?post_id=" + id;
            Document document = Jsoup.connect(imageUrl).get();

            List<ImageWaypoint> waypoints = new ArrayList<>();

            final String regex = "annotations.push\\(.*\\)";
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(document.html());
            while (matcher.find()) {
                String group = matcher.group(0);
                group = group.replace("annotations.push(", "").replace(")", "");
                Gson gson = new Gson();
                ImageWaypointPojo imageWaypointPojo = gson.fromJson(group, ImageWaypointPojo.class);

                ImageWaypoint imageWaypoint = new ImageWaypoint(imageWaypointPojo);

                waypoints.add(imageWaypoint);
            }

            String description = document.getElementById("photo_caption").text();
            return new Image(imageID, url, FilenameUtils.getName(url), description, waypoints);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void extract(List<Image> images, String path, String postID) {
        images.forEach(image -> {
            String url = image.getUrl();
            File localFile = new File(path + postID + "/" + FilenameUtils.getName(url));
            if (!localFile.exists()) {
                try {
                    FileUtils.copyURLToFile(new URL(url), localFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static String getImageID(String imageURL) {
        return imageURL.substring(imageURL.indexOf("/files/") + "/files/".length(), imageURL.lastIndexOf("."));
    }

}
