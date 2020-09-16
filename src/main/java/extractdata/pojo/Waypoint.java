package extractdata.pojo;

import com.google.gson.annotations.SerializedName;
import extractdata.HikrExporter;

import java.sql.*;

public class Waypoint implements Extractable {

    @SerializedName("piz_name")
    private String name;
    @SerializedName("piz_height")
    private int altitude;
    @SerializedName("piz_lon")
    private float longitude;
    @SerializedName("piz_lat")
    private float latitude;
    @SerializedName("piz_id")
    private int id;
    @SerializedName("piz_type")
    private WaypointType type;
    private String link;

    public static boolean isAlreadyInDb(String link) {
        String sql = "SELECT 1 FROM waypoint WHERE link='" + link + "';";
        try {
            PreparedStatement preparedStatement = HikrExporter.getConnection().prepareStatement(sql);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            return resultSet.next();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getAltitude() {
        return altitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public String getType() {
        return type.name();
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", altitude=" + altitude +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", id=" + id +
                ", type=" + type +
                '}';
    }

    public PreparedStatement createPreparedStatement() throws SQLException {
        String sql = "INSERT OR IGNORE INTO waypoint(id, altitude, name, longitude, latitude, type, link) VALUES(?,?,?,?,?,?,?)";

        PreparedStatement statement = HikrExporter.getConnection().prepareStatement(sql);
        statement.setInt(1, this.getId());
        statement.setInt(2, this.getAltitude());
        statement.setString(3, this.getName());
        statement.setFloat(4, this.getLongitude());
        statement.setFloat(5, this.getLatitude());
        statement.setString(6, this.getType());
        statement.setString(7, this.getLink());

        return statement;
    }

    public void store() {
        try {
            PreparedStatement preparedStatement = createPreparedStatement();
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }
}
