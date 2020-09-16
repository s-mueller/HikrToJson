package extractdata.pojo;

public class Difficulty {

    private String difficulty;
    private ActivityType activityType;

    public Difficulty(String difficulty, ActivityType activityType) {
        this.difficulty = difficulty;
        this.activityType = activityType;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public String getDifficulty() {
        return difficulty;
    }

    @Override
    public String toString() {
        return "{difficulty:" + difficulty + "," + "activityType:" + activityType.name() + "}";
    }
}