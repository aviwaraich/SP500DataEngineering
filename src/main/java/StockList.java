import java.util.ArrayList;
import java.util.List;

public class StockList {
    private String listID;
    private String name;
    private boolean isPublic;
    private String creator;
    private List<Stock> stocks;
    private List<Review> reviews;

    public StockList(String listID, String name, boolean isPublic, String creator) {
        this.listID = listID;
        this.name = name;
        this.isPublic = isPublic;
        this.creator = creator;
        this.stocks = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    public String getListID() {
        return listID;
    }

    public String getName() {
        return name;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public String getCreator() {
        return creator;
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    // Add stock to list
    public void addStock(Stock stock) {
        stocks.add(stock);
    }

    public void setReviews(List<Review> loadStockListReviews) {
        reviews = loadStockListReviews;
    }

    // Overriding toString() method for better representation
    @Override
    public String toString() {
        return "Stock List ID: " + listID + ", Name: " + name + ", Public: " + isPublic + ", Creator: " + creator;
    }

    // Method to view details of the stock list
    public void viewDetails() {
        System.out.println(this);
        System.out.println("Stocks:");
        for (Stock stock : stocks) {
            System.out.println("  " + stock);
        }
        System.out.println("Reviews:");
        for (Review review : reviews) {
            System.out.println("  " + review.getContent() + " - " + review.getUser() + " (" + review.getTimestamp() + ")");
        }
    }
}
