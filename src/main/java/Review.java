import java.util.Date;

public class Review {
    private String reviewID;
    private String content;
    private Date timestamp;
    private String user;
    private String listID;

    public Review(String reviewID, String content, Date timestamp, String user, String listID) {
        this.reviewID = reviewID;
        this.content = content;
        this.timestamp = timestamp;
        this.user = user;
        this.listID = listID;
    }

    public String getReviewID() {
        return reviewID;
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

    public String getStockListID() {
        return listID;
    }

    // Overriding toString() method for better representation
    @Override
    public String toString() {
        return "Review ID: " + reviewID + ", Content: " + content + ", User: " + user + ", Timestamp: " + timestamp;
    }
}
