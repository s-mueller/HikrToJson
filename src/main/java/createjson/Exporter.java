package createjson;

import com.fasterxml.jackson.databind.ObjectMapper;
import createjson.pojo.Tour;
import createjson.pojo.User;
import createjson.pojo.Waypoint;
import createjson.pojo.WaypointType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Exporter {

    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:database.db");

        List<User> users = getUsers(connection);
        List<Waypoint> waypoints = getWaypoints(connection).stream().collect(Collectors.toList());

        Statement statement = connection.createStatement();
        statement.execute("Select * from tour;");
        ResultSet resultSet = statement.getResultSet();

        List<Tour> tours = new ArrayList<>();

        while (resultSet.next()) {
            boolean multiDayHike = resultSet.getInt(7) == 1 ? true : false;
            int duration = resultSet.getInt(8);
            float durationValue = resultSet.wasNull() ? Float.NaN : duration;
            float minutes = multiDayHike ? Float.NaN : durationValue;
            float days = multiDayHike ? durationValue : Float.NaN;
            String[] difficulties = resultSet.getString(13).replace("[", "").replace("]", "").split(",");
            String[] waypointsList = resultSet.getString(14).replace("[", "").replace("]", "").split(",");
            float distance = resultSet.getFloat(11);
            float distanceValue = resultSet.wasNull() ? Float.NaN : distance;
            String userString = resultSet.getString(2);
            Optional<User> userOptional = users.stream().filter(user -> user.getLink().equals(userString)).findFirst();

            Tour tour = new Tour(
                resultSet.getInt(1),
                resultSet.getString(3),
                userOptional.orElse(null),
                resultSet.getLong(4),
                resultSet.getLong(5),
                resultSet.getString(6),
                resultSet.getInt(16),
                resultSet.getInt(9),
                resultSet.getInt(10),
                multiDayHike,
                Arrays.asList(difficulties).stream().map(String::trim).collect(Collectors.toList()),
                minutes,
                days,
                findWaypoints(waypoints, Arrays.asList(waypointsList).stream().map(String::trim).collect(Collectors.toList())),
                distanceValue,
                resultSet.getInt(15),
                resultSet.getInt(17),
                resultSet.getString(12),
                resultSet.getString(18)
            );
            tours.add(tour);
        }

        List<Tour> collect = tours.stream().limit(10).collect(Collectors.toList());

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(collect);
        FileUtils.writeStringToFile(new File("test/output.json"), json, Charset.defaultCharset());

        collect.stream().forEach(tour -> moveImagesAndGps(tour));
    }

    private static void moveImagesAndGps(Tour tour) {
        File imgDir = new File("images");

        Arrays.stream(imgDir.listFiles())
            .filter(dir -> dir.getName().equals(String.valueOf(tour.getId())))
            .forEach(dir -> {
                try {
                    FileUtils.copyDirectory(dir, new File("test/" + dir.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        File gpsDir = new File("gps");

        Arrays.stream(gpsDir.listFiles())
            .filter(dir -> dir.getName().equals(String.valueOf(tour.getId())))
            .forEach(dir -> {
                try {
                    FileUtils.copyDirectory(dir, new File("test/" + dir.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

    }

    private static List<Waypoint> findWaypoints(List<Waypoint> waypoints, List<String> waypointLinks) {
        return waypointLinks.stream().map(link -> waypoints.stream().filter(waypoint -> waypoint.getLink().equals(link)).findAny().orElse(null)).collect(Collectors.toList());
    }

    private static List<Waypoint> getWaypoints(Connection connection) throws SQLException {
        List<Waypoint> waypoints = new ArrayList<>();

        Statement statement = connection.createStatement();
        statement.execute("Select * from waypoint;");
        ResultSet resultSet = statement.getResultSet();

        while (resultSet.next()) {
            waypoints.add(new Waypoint(resultSet.getInt(1), resultSet.getInt(2), resultSet.getString(3), resultSet.getFloat(4), resultSet.getFloat(5), WaypointType.valueOf(resultSet.getString(6)), resultSet.getString(7)));
        }

        return waypoints;
    }

    private static List<User> getUsers(Connection connection) throws SQLException {
        List<User> users = new ArrayList<>();

        Statement statement = connection.createStatement();
        statement.execute("Select * from user;");
        ResultSet resultSet = statement.getResultSet();

        while (resultSet.next()) {
            users.add(new User(resultSet.getString(2), resultSet.getString(1)));
        }

        return users;
    }

}