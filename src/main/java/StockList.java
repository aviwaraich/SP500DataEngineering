import java.util.ArrayList;
import java.util.List;

public class StockList {
    private int listID;
    private String name;
    private boolean isPublic;
    private String creator;
    private List<StockHolding> stocks;
    private List<Review> reviews;

    public StockList(int listID, String name, boolean isPublic, String creator) {
        this.listID = listID;
        this.name = name;
        this.isPublic = isPublic;
        this.creator = creator;
        this.stocks = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    public int getListID() {
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

    public List<StockHolding> getStocks() {
        return stocks;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    // Add stock to list
    public void addStock(StockHolding stock) {
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
        for (StockHolding stock : stocks) {
            System.out.println("  " + stock);
        }
        System.out.println("Reviews:");
        for (Review review : reviews) {
            System.out.println("  " + review.getContent() + " - " + review.getUser() + " (" + review.getTimestamp() + ")");
        }
    }
}
