package createjson.pojo;

public class User {

    private String name;
    private String link;

    public User(String name, String link) {
        this.name = name;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }
}
