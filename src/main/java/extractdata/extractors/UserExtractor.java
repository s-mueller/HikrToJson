package extractdata.extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import extractdata.pojo.User;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserExtractor implements Extractor {

    @Override
    public List<User> extract() {
        List<User> result = new ArrayList<>();

        int skip = 0;
        boolean end = false;
        int before = result.size();

        while (!end) {
            try {
                Document doc = getDocument(skip);
                Elements links = doc.select("a[href]");
                links.stream()
                    .map(link -> link.attr("href"))
                    .filter(link -> link.startsWith("https://www.hikr.org/user/"))
                    .map(link -> new User(link.replace("https://www.hikr.org/user/", "").replace("/", ""), link))
                    .peek(user -> {
                        if (user != null) {
                            user.store();
                        }
                    })
                    .collect(Collectors.toCollection(() -> result));

                skip += 50;
                if (before == result.size()) {
                    end = true;
                }
                before = result.size();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public String createTableStatement() {
        return "CREATE TABLE IF NOT EXISTS user (\n"
            + "	link text PRIMARY KEY,\n"
            + " name text NOT NULL,\n"
            + " UNIQUE(link, name)"
            + ");";
    }

    private Document getDocument(int skip) throws IOException {
        String url = "https://www.hikr.org/view_users.php?skip=" + skip;
        System.out.println("Parse " + url + " for user");
        return Jsoup.connect(url).get();
    }
}
