package extractdata;

import createjson.pojo.Waypoint;
import createjson.pojo.WaypointType;
import extractdata.extractors.TourExtractor;
import extractdata.extractors.TourLinkExtractor;
import extractdata.extractors.UserExtractor;
import extractdata.extractors.WaypointExtractor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class HikrExporter {

    private static Connection connection;
    private static List<Waypoint> waypoints;

    public static void main(String[] args) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:database.db");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Arrays.asList(
            //new UserExtractor(),
            new WaypointExtractor(),
            new TourLinkExtractor(),
            new TourExtractor())
            .forEach(extractor -> {
                executeStatement(extractor.createTableStatement());
                extractor.extract();
            });
    }

    private static void executeStatement(String tableStatement) {
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                statement.execute(tableStatement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Waypoint> getWaypoints() {
        if (waypoints != null) {
            return waypoints;
        }
        List<Waypoint> waypoints = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();
            statement.execute("Select * from waypoint;");
            ResultSet resultSet = statement.getResultSet();

            while (resultSet.next()) {
                waypoints.add(new Waypoint(resultSet.getInt(1), resultSet.getInt(2), resultSet.getString(3), resultSet.getFloat(4), resultSet.getFloat(5), WaypointType.valueOf(resultSet.getString(6)), resultSet.getString(7)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        HikrExporter.waypoints = waypoints;
        return waypoints;
    }

    public static Connection getConnection() {
        return connection;
    }
}