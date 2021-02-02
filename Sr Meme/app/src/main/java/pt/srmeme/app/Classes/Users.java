package pt.srmeme.app.Classes;

public class Users {
    public String username;
    public int points;

    public Users() {
    }

    public Users(String username, int points) {
        this.username = username;
        this.points = points;
    }

    public String getUsername() {
        return username;
    }

    public int getPoints() {
        return points;
    }
}
