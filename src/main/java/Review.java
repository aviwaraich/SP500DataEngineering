import java.util.Date;

public class Review {
    private String reviewID;
    private String content;
    private Date timestamp;
    private String user;
    private StockList stockList;

    public Review(String reviewID, String content, Date timestamp, String user, StockList stockList) {
        this.reviewID = reviewID;
        this.content = content;
        this.timestamp = timestamp;
        this.user = user;
        this.stockList = stockList;
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

    public StockList getStockList() {
        return stockList;
    }
}
