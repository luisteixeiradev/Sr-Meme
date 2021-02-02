package pt.srmeme.app.Classes;

public class Category {
    private String name;
    private String category_id;
    private String user_id;

    public Category() {
    }

    public Category(String name, String category_id, String user_id) {
        this.name = name;
        this.category_id = category_id;
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public String getCategory_id() {
        return category_id;
    }

    public String getUser_id() {
        return user_id;
    }
}
