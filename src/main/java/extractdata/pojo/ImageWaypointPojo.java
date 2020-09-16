package extractdata.pojo;

import com.google.gson.annotations.SerializedName;

public class ImageWaypointPojo {

    private double x;
    private double y;
    @SerializedName("piz_id")
    private String id;

    public ImageWaypointPojo(String id, double x, double y) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getId() {
        return id;
    }
}
