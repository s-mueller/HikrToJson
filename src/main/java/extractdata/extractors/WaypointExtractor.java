package extractdata.extractors;

import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import extractdata.pojo.Waypoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WaypointExtractor implements Extractor {

    @Override
    public List<Waypoint> extract() {
        List<Waypoint> result = new ArrayList<>();

        AtomicBoolean end = new AtomicBoolean(false);
        int skip = 0;

        while (!end.get()) {
            try {
                Document waypointDocument = getDocument(skip);
                Elements waypointElements = waypointDocument.select("div.content-list-intern");
                waypointElements.stream()
                    .flatMap(link -> link.select("a[href]").stream())
                    .map(link -> link.attr("href"))
                    .filter(link -> link.startsWith("https://www.hikr.org/dir/"))
                    .filter(link -> !link.startsWith("https://www.hikr.org/dir/tag/"))
                    .peek(link -> {
                        if (Waypoint.isAlreadyInDb(link)) {
                            end.set(true);
                        }
                    })
                    .filter(link -> !Waypoint.isAlreadyInDb(link))
                    .map(link -> extractWaypoint(link))
                    .peek(w -> {
                        if (w != null) {
                            w.store();
                        }
                    })
                    .collect(Collectors.toCollection(() -> result));

                System.out.println(result.size() + " new waypoints to parse.");

                skip += 10;
                if (waypointDocument.html().contains("Kein Ergebnis")) {
                    end.set(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private Document getDocument(int skip) throws IOException {
        String url = "https://www.hikr.org/dir/?adv=1&piz_order=piz_id&skip=" + skip;
        System.out.println("Get waypoints from: " + url);
        return Jsoup.connect(url).get();
    }

    private Waypoint extractWaypoint(String link) {
        try {
            Document waypointDocument = Jsoup.connect(link).get();
            String html = waypointDocument.html();
            html = html.replace("\n", "");
            html = html.replace("\r", "");
            html = html.replace("\t", "");
            html = html.replace(" ", "");
            final String regex = "pizs.push\\(\\{[\\s\\S]*\\}\\)vargps";
            Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                String json = matcher.group(0);
                json = json.replace("pizs.push(", "");
                json = json.replace(")vargps", "");

                Gson gson = new Gson();
                Waypoint waypoint = gson.fromJson(json, Waypoint.class);
                waypoint.setLink(link);
                return waypoint;
            } else {
                System.out.println(link);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String createTableStatement() {
        return "CREATE TABLE IF NOT EXISTS waypoint (\n"
            + "	id integer PRIMARY KEY,\n"
            + " altitude integer NOT NULL,\n"
            + "	name text NOT NULL,\n"
            + "	longitude real,\n"
            + "	latitude real,\n"
            + "	type text,\n"
            + "	link text,\n"
            + " UNIQUE(id, altitude,name,longitude,latitude,type,link)"
            + ");";
    }
}
