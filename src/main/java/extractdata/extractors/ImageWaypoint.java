package extractdata.extractors;

import createjson.pojo.Waypoint;
import createjson.pojo.WaypointType;
import extractdata.HikrExporter;
import extractdata.pojo.ImageWaypointPojo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ImageWaypoint {

    private double x;
    private double y;
    private int id;
    private int altitude;
    private String name;
    private float longitude;
    private float latitude;
    private WaypointType type;
    private String link;

    public ImageWaypoint(ImageWaypointPojo imageWaypointPojo) {
        this.x = imageWaypointPojo.getX();
        this.y = imageWaypointPojo.getY();
        this.id = Integer.parseInt(imageWaypointPojo.getId());

        Optional<Waypoint> waypointOptional = findWaypoints(HikrExporter.getWaypoints(), this.id);
        waypointOptional.ifPresent(waypoint -> {
            this.altitude = waypoint.getAltitude();
            this.name = waypoint.getName();
            this.latitude = waypoint.getLatitude();
            this.longitude = waypoint.getLongitude();
            this.type = waypoint.getType();
            this.link = waypoint.getLink();
        });
    }

    private Optional<Waypoint> findWaypoints(List<createjson.pojo.Waypoint> waypoints, int id) {
        return waypoints.stream().filter(waypoint -> waypoint.getId() == id).findFirst();
    }

}
