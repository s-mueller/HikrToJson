package createjson.pojo;

public class Waypoint {

    private int id;
    private int altitude;
    private String name;
    private float longitude;
    private float latitude;
    private WaypointType type;
    private String link;

    public Waypoint(int id, int altitude, String name, float longitude, float latitude, WaypointType type, String link) {
        this.id = id;
        this.altitude = altitude;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.type = type;
        this.link = link;
    }

    public int getId() {
        return id;
    }

    public int getAltitude() {
        return altitude;
    }

    public String getName() {
        return name;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public WaypointType getType() {
        return type;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "{"
            + "\"id\":\"" + id + "\""
            + ", \"altitude\":\"" + altitude + "\""
            + ", \"name\":\"" + name + "\""
            + ", \"longitude\":\"" + longitude + "\""
            + ", \"latitude\":\"" + latitude + "\""
            + ", \"type\":\"" + type + "\""
            + ", \"link\":\"" + link + "\""
            + "}";
    }
}