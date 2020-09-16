package extractdata.extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import extractdata.pojo.TourLink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class TourLinkExtractor implements Extractor {

    @Override
    public List<TourLink> extract() {
        List<TourLink> result = new ArrayList<>();

        int skip = 0;
        AtomicBoolean end = new AtomicBoolean(false);
        int before = result.size();

        while (!end.get()) {
            try {
                Document doc = getDocument(skip);
                Elements links = doc.select("a[href]");
                List<String> hrefs = links.stream()
                    .map(link -> link.attr("href"))
                    .filter(link -> link.startsWith("https://www.hikr.org/tour/post"))
                    .distinct()
                    .collect(Collectors.toList());

                for (String href : hrefs) {
                    if (TourLink.isAlreadyInDatabase(href)) {
                        System.out.println("Tour " + href + " is already in DB. Not going to the next page.");
                        end.set(true);
                        break;
                    }
                }

                hrefs.stream()
                    .map(link -> new TourLink(link, false))
                    .peek(tour -> {
                        if (tour != null) {
                            tour.store();
                        }
                    })
                    .collect(Collectors.toCollection(() -> result));

                skip += 20;
                if (before == result.size()) {
                    end.set(true);
                }
                before = result.size();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private Document getDocument(int skip) throws IOException {
        String url = "https://www.hikr.org/tour?skip=" + skip;
        Document doc = Jsoup.connect(url).get();
        System.out.println(url);
        return doc;
    }

    @Override
    public String createTableStatement() {
        return "CREATE TABLE IF NOT EXISTS tourlinks (\n"
            + "	link text PRIMARY KEY,\n"
            + " parsed integer NOT NULL,\n"
            + " UNIQUE(link)"
            + ");";
    }
}
