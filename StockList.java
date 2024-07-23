import java.util.ArrayList;
import java.util.List;

// Viewability Enum
enum Viewability {
    PRIVATE,
    PUBLIC,
    SHARED
}

public class StockList {
    public String name;
    public Viewability viewability;
    public User owner;
    public List<StockHolding> stockHoldings;
    public List<Review> reviews;

    public StockList(String name, User owner) {
        this.name = name;
        this.viewability = Viewability.public;
        this.owner = owner;
        this.stockHoldings = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Viewability getViewability() {
        return viewability;
    }

    public void setViewability(Viewability viewability) {
        this.viewability = viewability;
    }

    public User getOwner() {
        return owner;
    }

    public void addStockHolding(StockHolding stockHolding) {
        stockHoldings.add(stockHolding);
    }

    public List<StockHolding> getStockHoldings() {
        return stockHoldings;
    }

    public void addReview(Review review) {
        reviews.add(review);
    }

    public List<Review> getReviews() {
        return reviews;
    }
}

