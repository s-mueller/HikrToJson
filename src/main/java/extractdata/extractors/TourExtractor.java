package extractdata.extractors;

import createjson.pojo.Waypoint;
import extractdata.HikrExporter;
import extractdata.pojo.ActivityType;
import extractdata.pojo.Difficulty;
import extractdata.pojo.Image;
import extractdata.pojo.Tour;
import extractdata.pojo.TourLink;
import extractdata.pojo.User;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TourExtractor implements Extractor {

    @Override
    public List<Tour> extract() {
        List<TourLink> toursToParse = getTourLinks(HikrExporter.getConnection());

        List<Tour> result = new ArrayList<>();
        toursToParse.stream().forEach(t -> {
            Tour tour = createTour(t.getLink());
            if (tour != null) {
                tour.store();
                result.add(tour);
            }
        });

        return result;
    }

    private List<TourLink> getTourLinks(java.sql.Connection connection) {
        List<TourLink> result = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();
            statement.execute("select link from tourlinks WHERE parsed = 0;");
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                result.add(new TourLink(resultSet.getString(1), false));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println(result.size() + " more tours to parse");
        return result;
    }

    private Tour createTour(String link) {
        try {
            System.out.println("Start parsing: " + link);
            Connection.Response execute = Jsoup.connect("https://www.hikr.org/login.php").execute();
            Document doc = Jsoup.connect(link).get();

            String title = doc.getElementsByTag("h1").first().text();
            int id = Integer.parseInt(link.replace("https://www.hikr.org/tour/post", "").replace(".html", ""));
            String user = getUser(doc);
            long publicationOffset = getPublicationDate(doc);
            String mainText = getMainText(doc);
            double numberOfViews = getNumberOfViews(doc);
            List<User> friends = getFriends(doc);

            Tour tour = new Tour(id, link, title, user, publicationOffset, mainText, numberOfViews, friends);

            addImages(tour, doc, id);
            addGps(tour, doc, id);
            addTourDetails(tour, doc);
            return tour;
        } catch (IOException e) {
            return null;
        }
    }

    private List<User> getFriends(Document document) {
        Element select = document.getElementsByClass("div15").first();

        return select.children().stream()
            .map(c -> c.getElementsByTag("a"))
            .map(a -> a.attr("href"))
            .filter(s -> !s.isEmpty())
            .map(s -> new User(s.substring(s.indexOf("user/") + 5, s.lastIndexOf("/")), s))
            .collect(Collectors.toList());
    }

    private void addTourDetails(Tour tour, Document doc) {
        List<Difficulty> difficulties = new ArrayList<>();
        Element table = doc.getElementsByClass("fiche_rando").first();
        for (Element tableRow : table.getElementsByTag("tr")) {
            Elements columns = tableRow.getElementsByTag("td");
            if (columns.first().text().equals("Tour Datum:")) {
                String tourDateString = columns.get(1).text().trim();
                Date tourDate = parseTourDate(tourDateString);
                tour.setTourDate(tourDate.getTime());
            } else if (columns.first().text().equals("Hochtouren Schwierigkeit:")) {
                String difficulty = columns.get(1).text();
                difficulties.add(new Difficulty(difficulty.trim(), ActivityType.ALPINETOUR));
            } else if (columns.first().text().equals("Eisklettern Schwierigkeit:")) {
                String difficulty = columns.get(1).text();
                difficulties.add(new Difficulty(difficulty.trim(), ActivityType.ICECLIMB));
            } else if (columns.first().text().equals("Klettersteig Schwierigkeit:")) {
                String difficulty = columns.get(1).text();
                difficulties.add(new Difficulty(difficulty.trim(), ActivityType.VIAFERRATA));
            } else if (columns.first().text().equals("Klettern Schwierigkeit:")) {
                String text = columns.get(1).text();
                text = text.substring(0, text.indexOf("(")).trim();
                difficulties.add(new Difficulty(text, ActivityType.CLIMB));
            } else if (columns.first().text().equals("Schneeshuhtouren Schwierigkeit:")) {
                String text = columns.get(1).text();
                text = text.substring(0, 3);
                difficulties.add(new Difficulty(text.trim(), ActivityType.SNOWSHOETOUR));
            } else if (columns.first().text().equals("Ski Schwierigkeit:")) {
                String text = columns.get(1).text();
                difficulties.add(new Difficulty(text.trim(), ActivityType.SKITOUR));
            } else if (columns.first().text().equals("Mountainbike Schwierigkeit:")) {
                String text = columns.get(1).text();
                text = text.substring(0, text.indexOf(" "));
                difficulties.add(new Difficulty(text, ActivityType.MOUNTAINBIKE));
            } else if (columns.first().text().equals("Wegpunkte:")) {
                List<Waypoint> wayPoints = extractWaypoints(columns.get(1));
                tour.setWaypoints(wayPoints);
            } else if (columns.first().text().equals("Zeitbedarf:")) {
                String text = columns.get(1).text();
                if (text.contains("Tage")) {
                    tour.setMultiDayHike(true);
                    tour.setDuration(Integer.parseInt(text.substring(0, text.indexOf(" "))));
                } else {
                    tour.setMultiDayHike(false);
                    int first = Integer.parseInt(text.substring(0, text.indexOf(":")));
                    int last = Integer.parseInt(text.substring(text.indexOf(":") + 1));
                    tour.setDuration((first * 60) + last);
                }
            } else if (columns.first().text().equals("Aufstieg:")) {
                String text = columns.get(1).text();
                text = text.replace(" m", "");
                int elevationGain = Integer.parseInt(text);
                tour.setElevationGain(elevationGain);
            } else if (columns.first().text().equals("Abstieg:")) {
                String text = columns.get(1).text();
                text = text.replace(" m", "");
                int elevationLoss = Integer.parseInt(text);
                tour.setElevationLoss(elevationLoss);
            } else if (columns.first().text().equals("Strecke:")) {
                String text = columns.get(1).text();
                tour.setRoute(text);
                text = text.trim();
                text = text.replace(" ", "");
                text = text.replace("km", "");
                text = text.replace("KM", "");
                text = text.replace("Km", "");
                text = text.replace("Kilometer", "");
                text = text.replace(",", ".");
                try {
                    float distance = Float.parseFloat(text);
                    tour.setDistance(distance);
                } catch (Exception e) {
                    tour.setDistance(Float.NaN);
                }
            } else if (columns.first().text().equals("Wandern Schwierigkeit:")) {
                String text = columns.get(1).text();
                text = text.substring(0, 2);
                difficulties.add(new Difficulty(text, ActivityType.HIKE));
            } else if (columns.first().text().equals("Unterkunftmöglichkeiten:") || columns.first().text().equals("Geo-Tags:") || columns.first().text().equals("Region:") || columns.first().text().equals("Kartennummer:") || columns.first().text().equals("Zufahrt zum Ausgangspunkt:") || columns.first().text().equals("Zufahrt zum Ankunftspunkt:")) {
                //ignore
            } else {
                //System.out.println(tableRow.text());
            }
        }
        tour.setDifficulties(difficulties);
    }

    private void addGps(Tour tour, Document doc, int id) {
        List<String> gps = GPSExtractor.extract(doc, "/Volumes/Backup Fotos/HikrExport/gps/", "" + id);
        tour.setGps(gps);
    }

    private void addImages(Tour tour, Document doc, int id) {
        List<Image> images = ImageExtractor.getImageLinks(doc, "" + id);
        ImageExtractor.extract(images, "/Volumes/Backup Fotos/HikrExport/images/", "" + id);
        tour.setImages(images);
    }

    private long getPublicationDate(Document doc) {
        final String dateRegex = "[\\d]{1,}. \\D* [\\d]{4} um [\\d]*:[\\d]{2}";
        Pattern datePattern = Pattern.compile(dateRegex, Pattern.MULTILINE);
        final Matcher dateMatcher = datePattern.matcher(doc.html());
        dateMatcher.find();
        String date = dateMatcher.group(0);
        Date publicationDate = extractDate(date);
        return publicationDate.getTime();
    }

    private String getUser(Document doc) {
        final String regex = "Publiziert von [\\d\\D]+";

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(doc.html());
        matcher.find();
        String user = matcher.group(0);
        user = user.substring(user.indexOf("href=\""), user.indexOf(">"));
        user = user.replace("href=", "");
        user = user.replace("\"", "");
        return user;
    }

    private double getNumberOfViews(Document doc) {
        final String viewsRegex = "Diese Seite wurde <b.*<\\/b>";
        final Pattern viewsPattern = Pattern.compile(viewsRegex, Pattern.MULTILINE);
        final Matcher viewsMatcher = viewsPattern.matcher(doc.html());
        boolean found = viewsMatcher.find();
        double numberOfViews = Double.NaN;
        if (found) {
            String views = viewsMatcher.group(0).replace("</b>", "");
            String substring = views.substring(views.lastIndexOf(">") + 1);
            numberOfViews = Double.parseDouble(substring);
        }
        return numberOfViews;
    }

    private String getMainText(Document doc) {
        return doc.select("#main_text").html();
    }

    private Date extractDate(String date) {
        String dates[] = date.split(" ");
        int day = Integer.parseInt(dates[0].substring(0, dates[0].indexOf(".")));
        int month = getMonth(dates[1]);
        int year = Integer.parseInt(dates[2]) - 1900;

        String[] hours = dates[4].split(":");
        int hour = Integer.parseInt(hours[0]);
        int minutes = Integer.parseInt(hours[1]);

        Date endDate = new Date(year, month, day);
        endDate.setHours(hour);
        endDate.setMinutes(minutes);
        return endDate;
    }

    private Date parseTourDate(String tourDateString) {
        String dates[] = tourDateString.split(" ");
        int day = Integer.parseInt(dates[0]);
        int month = getMonth(dates[1]);
        int year = Integer.parseInt(dates[2]);
        Date date = new Date(year - 1900, month, day);
        return date;
    }

    private int getMonth(String date) {
        switch (date) {
            case "Januar":
                return 0;
            case "Februar":
                return 1;
            case "März":
                return 2;
            case "April":
                return 3;
            case "Mai":
                return 4;
            case "Juni":
                return 5;
            case "Juli":
                return 6;
            case "August":
                return 7;
            case "September":
                return 8;
            case "Oktober":
                return 9;
            case "November":
                return 10;
            case "Dezember":
                return 11;
        }
        return 12;
    }

    private List<Waypoint> extractWaypoints(Element element) {
        Elements linkElements = element.getElementsByTag("a");
        List<String> links = linkElements.stream()
            .map(e -> e.attr("href"))
            .distinct()
            .collect(Collectors.toList());

        return findWaypoints(HikrExporter.getWaypoints(), links);
    }

    private static List<createjson.pojo.Waypoint> findWaypoints(List<createjson.pojo.Waypoint> waypoints, List<String> waypointLinks) {
        return waypointLinks.stream().map(link -> waypoints.stream().filter(waypoint -> waypoint.getLink().equals(link)).findAny().orElse(null)).collect(Collectors.toList());
    }

    @Override
    public String createTableStatement() {
        return "CREATE TABLE IF NOT EXISTS tour (\n"
            + "	id int PRIMARY KEY,\n"
            + "	user text,\n"
            + " title text NOT NULL,\n"
            + " publicationDate integer NOT NULL,\n"
            + " tourDate integer NOT NULL,\n"
            + " mainText text NOT NULL,\n"
            + " multiDay integer NOT NULL,\n"
            + " requiredTime integer,\n"
            + " elevationGain integer,\n"
            + " elevationLoss integer,\n"
            + " distance integer,\n"
            + " route text,\n"
            + " difficulties text NOT NULL,\n"
            + " waypoints text NOT NULL,\n"
            + " images int NOT NULL,\n"
            + " views real NOT NULL,\n"
            + " gps text NOT NULL,\n"
            + " link text NOT NULL,\n"
            + " friends text NOT NULL,\n"
            + " UNIQUE(id)"
            + ");";
    }
}
