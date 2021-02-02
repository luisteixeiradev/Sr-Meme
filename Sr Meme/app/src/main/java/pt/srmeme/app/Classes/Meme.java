package pt.srmeme.app.Classes;

public class Meme {
    public String user;
    public String description;
    public String image;
    public String username;
    public String meme_id;

    public Meme() {
    }

    public Meme(String user, String description, String image, String username, String meme_id) {
        this.user = user;
        this.description = description;
        this.image = image;
        this.username = username;
        this.meme_id = meme_id;
    }

    public String getMeme_id() {
        return meme_id;
    }

    public String getUser() {
        return user;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public String getUsername() {
        return username;
    }

}
