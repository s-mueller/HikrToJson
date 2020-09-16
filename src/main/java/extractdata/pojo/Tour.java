package extractdata.pojo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import createjson.pojo.Waypoint;
import extractdata.HikrExporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Tour implements Extractable {

    private static int counter = 1;

    private final List<User> friends;
    private int id;
    private String title;
    private String user;
    private long publicationDate;
    private String mainText;
    private double numberOfViews;
    private int elevationGain = -1;
    private int elevationLoss = -1;
    private List<Difficulty> difficulties = new ArrayList<>();
    private boolean multiDayHike;
    private int duration = -1;
    private List<createjson.pojo.Waypoint> waypoints = new ArrayList<>();
    private float distance = -1;
    private long tourDate;
    private List<Image> images = new ArrayList<>();
    private List<String> gps = new ArrayList<>();
    private String route;
    private String link;

    public Tour(int id, String link, String title, String user, long publicationDate, String mainText, double numberOfViews, List<User> friends) {
        this.link = link;
        this.id = id;
        this.title = title;
        this.user = user;
        this.publicationDate = publicationDate;
        this.mainText = mainText;
        this.numberOfViews = numberOfViews;
        this.friends = friends;
    }

    public PreparedStatement createPreparedStatement() throws SQLException {
        String sql = "INSERT OR IGNORE INTO tour(id, user, title, publicationDate, tourDate, mainText, multiDay, requiredTime," +
                "elevationGain, elevationLoss, distance, route, difficulties, waypoints, images, views, gps, link, friends)" +
                " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        PreparedStatement statement = HikrExporter.getConnection().prepareStatement(sql);
        statement.setInt(1, this.id);
        statement.setString(2, this.user);
        statement.setString(3, this.title);
        statement.setFloat(4, this.publicationDate);
        statement.setFloat(5, this.tourDate);
        statement.setString(6, this.mainText);
        statement.setInt(7, this.multiDayHike ? 1 : 0);
        if (this.duration == -1) {
            statement.setNull(8, Types.INTEGER);
        } else {
            statement.setInt(8, this.duration);
        }
        if (this.elevationGain == -1) {
            statement.setNull(9, Types.INTEGER);
        } else {
            statement.setInt(9, this.elevationGain);
        }
        if (this.elevationLoss == -1) {
            statement.setNull(10, Types.INTEGER);
        } else {
            statement.setInt(10, this.elevationLoss);
        }
        if (this.distance == -1) {
            statement.setNull(11, Types.FLOAT);
        } else {
            statement.setFloat(11, this.distance);
        }

        statement.setString(12, this.route);
        statement.setString(13, this.difficulties.toString());
        statement.setString(14, this.waypoints.toString());
        statement.setInt(15, this.images.size());
        statement.setDouble(16, this.numberOfViews);
        statement.setString(17, this.gps.toString());
        statement.setString(18, this.link);
        statement.setString(19, this.friends.toString());

        return statement;
    }

    public void store() {
        try {
            PreparedStatement preparedStatement = createPreparedStatement();
            preparedStatement.executeUpdate();

            Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
            String jsonFile = "/Volumes/Backup Fotos/HikrExport/" + this.id + ".json";
            FileWriter writer = new FileWriter(jsonFile);
            gson.toJson(this, writer);
            writer.flush();
            writer.close();

            List<File> filesToRemove = zip();
            filesToRemove.forEach(f -> f.delete());

            Statement updateStatement = HikrExporter.getConnection().createStatement();
            updateStatement.execute("UPDATE tourlinks\n" +
                    "SET parsed = 1\n" +
                    "WHERE link = '" + this.link + "';");

            System.out.println("Store tour (" + counter++ + "): " + link);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<File> zip() throws IOException {
        List<File> imageFiles = this.images.stream().map(image -> new File("/Volumes/Backup Fotos/HikrExport/images/" + id + "/" + image.getFileName())).collect(Collectors.toList());
        List<File> gpsFiles = this.gps.stream().map(g -> new File("/Volumes/Backup Fotos/HikrExport/gps/" + id + "/" + g)).collect(Collectors.toList());

        imageFiles.addAll(gpsFiles);
        imageFiles.add(new File("/Volumes/Backup Fotos/HikrExport/" + this.id + ".json"));

        FileOutputStream fos = new FileOutputStream("/Volumes/Backup Fotos/HikrExport/output/" + this.id + ".zip");
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (File srcFile : imageFiles) {
            FileInputStream fis = new FileInputStream(srcFile);
            ZipEntry zipEntry = new ZipEntry(srcFile.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        fos.close();

        return imageFiles;
    }

    public void setElevationGain(int elevationGain) {
        this.elevationGain = elevationGain;
    }

    public void setElevationLoss(int elevationLoss) {
        this.elevationLoss = elevationLoss;
    }

    public void setDifficulties(List<Difficulty> difficulties) {
        this.difficulties = difficulties;
    }

    public void setMultiDayHike(boolean multiDayHike) {
        this.multiDayHike = multiDayHike;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setWaypoints(List<Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public void setTourDate(long tourDate) {
        this.tourDate = tourDate;
    }

    @Override
    public String toString() {
        return "Tour{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", user='" + user + '\'' +
                ", publicationDate=" + publicationDate +
                ", mainText='" + mainText + '\'' +
                ", numberOfViews=" + numberOfViews +
                ", elevationGain=" + elevationGain +
                ", elevationLoss=" + elevationLoss +
                ", difficulties=" + difficulties +
                ", multiDayHike=" + multiDayHike +
                ", duration=" + duration +
                ", waypoints=" + waypoints +
                ", distance=" + distance +
                ", tourDate=" + tourDate +
                '}';
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public void setGps(List<String> gps) {
        this.gps = gps;
    }

    public void setRoute(String route) {
        this.route = route;
    }
}
