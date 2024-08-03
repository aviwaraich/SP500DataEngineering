
import java.util.ArrayList;
import java.util.List;

public class StockList {

    private int listID;
    private String name;
    private boolean isPublic;
    private String creator;
    private List<Stock> stocks;
    private List<Review> reviews;
    private List<StockListItem> stockItems;

    public StockList(int listID, String name, boolean isPublic, String creator) {
        this.listID = listID;
        this.name = name;
        this.isPublic = isPublic;
        this.creator = creator;
        this.stocks = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.stockItems = new ArrayList<>();
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

    public List<Stock> getStocks() {
        return stocks;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public List<StockListItem> getStockItems() {
        return stockItems;
    }

    public void setStockItems(List<StockListItem> stockItems) {
        this.stockItems = stockItems;
    }

    public void addStockItem(StockListItem item) {
        stockItems.add(item);
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
        if (stockItems.isEmpty()) {
            System.out.println("  No stocks in this list.");
        } else {
            for (StockListItem item : stockItems) {
                System.out.println("  " + item);
            }
        }
        System.out.println("Reviews:");
        if (reviews.isEmpty()) {
            System.out.println("  No reviews for this list.");
        } else {
            for (Review review : reviews) {
                System.out.println("  " + review.getContent() + " - " + review.getUser() + " (" + review.getTimestamp() + ")");
            }
        }
    }

// Add this method to StockList class
    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }
}
