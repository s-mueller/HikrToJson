package extractdata.pojo;

import extractdata.HikrExporter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TourLink implements Extractable {

    private final boolean parsed;
    private final String link;

    public TourLink(String link, boolean parsed) {
        this.link = link;
        this.parsed = parsed;
    }

    public static boolean isAlreadyInDatabase(String link) {
        String sql = "SELECT 1 FROM tourlinks WHERE link='" + link + "';";
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

    public PreparedStatement createPreparedStatement() throws SQLException {
        String sql = "INSERT OR IGNORE INTO tourlinks(link, parsed) VALUES(?,?)";

        PreparedStatement statement = HikrExporter.getConnection().prepareStatement(sql);
        statement.setString(1, this.link);
        statement.setBoolean(2, this.parsed);

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

    public String getLink() {
        return link;
    }
}
