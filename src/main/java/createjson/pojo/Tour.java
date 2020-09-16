package createjson.pojo;

import java.util.List;

public class Tour {

    private int id;
    private String title;
    private User user;
    private long publicationDate;
    private long tourDate;
    private String mainText;
    private int numberOfViews;
    private int elevationGain;
    private int elevationLoss;
    private boolean multiDayHike;
    private List<String> difficulties;
    private float minutes = 0;
    private float days = 0;
    private List<Waypoint> waypoints;
    private float distance;
    private int numberOfImages;
    private int numberOfGpsTracks;
    private String route;
    private String link;

    public Tour(int id, String title, User user, long publicationDate, long tourDate, String mainText, int numberOfViews, int elevationGain, int elevationLoss, boolean multiDayHike, List<String> difficulties, float minutes, float days, List<Waypoint> waypoints, float distance, int numberOfImages, int numberOfGpsTracks, String route, String link) {
        this.id = id;
        this.title = title;
        this.user = user;
        this.publicationDate = publicationDate;
        this.tourDate = tourDate;
        this.mainText = mainText;
        this.numberOfViews = numberOfViews;
        this.elevationGain = elevationGain;
        this.elevationLoss = elevationLoss;
        this.multiDayHike = multiDayHike;
        this.difficulties = difficulties;
        this.minutes = minutes;
        this.days = days;
        this.waypoints = waypoints;
        this.distance = distance;
        this.numberOfImages = numberOfImages;
        this.numberOfGpsTracks = numberOfGpsTracks;
        this.route = route;
        this.link = link;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public User getUser() {
        return user;
    }

    public long getPublicationDate() {
        return publicationDate;
    }

    public long getTourDate() {
        return tourDate;
    }

    public String getMainText() {
        return mainText;
    }

    public int getNumberOfViews() {
        return numberOfViews;
    }

    public int getElevationGain() {
        return elevationGain;
    }

    public int getElevationLoss() {
        return elevationLoss;
    }

    public boolean isMultiDayHike() {
        return multiDayHike;
    }

    public List<String> getDifficulties() {
        return difficulties;
    }

    public float getMinutes() {
        return minutes;
    }

    public float getDays() {
        return days;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public float getDistance() {
        return distance;
    }

    public int getNumberOfImages() {
        return numberOfImages;
    }

    public int getNumberOfGpsTracks() {
        return numberOfGpsTracks;
    }

    public String getRoute() {
        return route;
    }

    public String getLink() {
        return link;
    }
}