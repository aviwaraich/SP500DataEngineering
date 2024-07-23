package cs.toronto.edu;

public class Review {
    public String content;
    public User author;
    public StockList stockList;

    public Review(String content, User author, StockList stockList) {
        this.content = content;
        this.author = author;
        this.stockList = stockList;
    }

    public String getContent() {
        return content;
    }

    public User getAuthor() {
        return author;
    }
}