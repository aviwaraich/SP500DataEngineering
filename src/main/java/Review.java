
import java.util.Date;

public class Review {

    private int reviewID;
    private String content;
    private Date timestamp;
    private String user;
    private int listID;

    public Review(int reviewID, String content, Date timestamp, String user, int listID) {
        this.reviewID = reviewID;
        this.content = content;
        this.timestamp = timestamp;
        this.user = user;
        this.listID = listID;
    }

    public int getReviewID() {
        return reviewID;
    }

    public int getListID() {
        return listID;
    }

    public String getContent() {
        return content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getUser() {
        return user;
    }

    // Overriding toString() method for better representation
    @Override
    public String toString() {
        return "Review ID: " + reviewID + ", Content: " + content + ", User: " + user + ", Timestamp: " + timestamp;
    }
}
