package uk.ac.wlv.myblogapp;

// Model class used to store blog message data
public class Message {

    private int id;
    private String title;
    private String content;
    private String imagePath;
    private String dateTime;
    private boolean selected;

    public Message() {
    }

    // Constructor used when reading messages from SQLite database
    public Message(int id, String title, String content, String imagePath, String dateTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imagePath = imagePath;
        this.dateTime = dateTime;
        this.selected = false;
    }

    // Constructor used when creating a new message
    public Message(String title, String content, String imagePath, String dateTime) {
        this.title = title;
        this.content = content;
        this.imagePath = imagePath;
        this.dateTime = dateTime;
        this.selected = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }


    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }


    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}