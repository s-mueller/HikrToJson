package extractdata.pojo;

import extractdata.HikrExporter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class User implements Extractable {

    private String name;
    private String link;

    public User(String name, String link) {
        this.name = name;
        this.link = link;
    }

    private PreparedStatement createPreparedStatement() throws SQLException {
        String sql = "INSERT OR IGNORE INTO user(link, name) VALUES(?,?)";

        PreparedStatement statement = HikrExporter.getConnection().prepareStatement(sql);
        statement.setString(1, this.link);
        statement.setString(2, this.name);

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

    @Override
    public String toString() {
        return "{" +
            "name:\"" + name + "\"" +
            ",link:\"" + link + "\"" +
            "}";
    }
}
