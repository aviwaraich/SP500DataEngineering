import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class StockList {
    private int listID;
    private String owner;
    private String name;
    private boolean isPublic;
    private List<String> symbols;

    // Constructor and other methods

    public Map<String, Double> calculateBetas(LocalDate startDate, LocalDate endDate) throws SQLException {
        StockAnalyzer analyzer = new StockAnalyzer();
        Map<String, Double> betas = new HashMap<>();

        for (String symbol : symbols) {
            betas.put(symbol, analyzer.calculateBeta(symbol, startDate, endDate));
        }

        analyzer.closeConnection();
        return betas;
    }

    public Map<String, Double> calculateCoVs(LocalDate startDate, LocalDate endDate) throws SQLException {
        StockAnalyzer analyzer = new StockAnalyzer();
        Map<String, Double> covs = new HashMap<>();

        for (String symbol : symbols) {
            covs.put(symbol, analyzer.calculateCoV(symbol, startDate, endDate));
        }

        analyzer.closeConnection();
        return covs;
    }

    public Map<String, Map<String, Double>> calculateCorrelationMatrix(LocalDate startDate, LocalDate endDate) throws SQLException {
        StockAnalyzer analyzer = new StockAnalyzer();
        Map<String, Map<String, Double>> correlationMatrix = analyzer.calculateCorrelationMatrix(symbols, startDate, endDate);
        analyzer.closeConnection();
        return correlationMatrix;
    }

    public Map<String, List<Double>> predictFuturePrices(LocalDate startDate, int daysToPredict) throws SQLException {
        StockAnalyzer analyzer = new StockAnalyzer();
        Map<String, List<Double>> predictions = new HashMap<>();

        for (String symbol : symbols) {
            predictions.put(symbol, analyzer.predictFuturePrice(symbol, startDate, daysToPredict));
        }

        analyzer.closeConnection();
        return predictions;
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

    public String getCreatorUsername() {
        return creatorUsername;
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
        return "Stock List ID: " + listID + ", Name: " + name + ", Public: " + isPublic + ", Creator: " + creatorUsername;
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