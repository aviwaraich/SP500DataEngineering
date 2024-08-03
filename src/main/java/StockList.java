
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class StockList {

    private int listID;
    private String name;
    private boolean isPublic;
    private String creator;
    private List<Stock> stocks;
    private List<Review> reviews;
    private List<StockListItem> stockItems;
    private StockAnalyzer analyzer;

    public StockList(int listID, String name, boolean isPublic, String creator) {
        this.listID = listID;
        this.name = name;
        this.isPublic = isPublic;
        this.creator = creator;
        this.stockItems = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.analyzer = new StockAnalyzer();
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

    public void analyzeStockList(Scanner scanner) {
        try {
            System.out.println("\n--- Stock List Analysis: " + this.name + " ---");

            LocalDate startDate = getDateInput(scanner, "Enter start date (YYYY-MM-DD) or press enter for 1st given data: ");
            var endDate = getDateInput(scanner, "Enter end date (YYYY-MM-DD) or press enter for today: ");

            if (startDate == null) {
                startDate = LocalDate.of(2013, 2, 8);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            System.out.println("Analyzing data from " + startDate + " to " + endDate);

            List<String> symbols = stockItems.stream()
                    .map(StockListItem::getSymbol)
                    .collect(Collectors.toList());

            Map<String, Double> covs = analyzer.calculateCoVs(symbols, startDate, endDate);
            Map<String, Double> betas = analyzer.calculateBetas(symbols, startDate, endDate);

            System.out.println("\nStock Statistics:");
            for (StockListItem item : stockItems) {
                String symbol = item.getSymbol();
                System.out.printf("%s:\n", symbol);
                System.out.printf("  Coefficient of Variation: %.4f\n", covs.getOrDefault(symbol, Double.NaN));
                System.out.printf("  Beta: %.4f\n", betas.getOrDefault(symbol, Double.NaN));
            }

            if (symbols.size() > 1) {
                System.out.println("\nCorrelation Matrix:");
                Map<String, Map<String, Double>> correlationMatrix = analyzer.calculateCorrelationMatrix(symbols, startDate, endDate);
                
                System.out.printf("%-8s", "");
                for (String symbol : symbols) {
                    System.out.printf("%-8s", symbol);
                }
                System.out.println();

                for (String symbol1 : symbols) {
                    System.out.printf("%-8s", symbol1);
                    for (String symbol2 : symbols) {
                        Double correlation = correlationMatrix.getOrDefault(symbol1, Collections.emptyMap()).getOrDefault(symbol2, Double.NaN);
                        if (Double.isNaN(correlation) || Double.isInfinite(correlation)) {
                            System.out.printf("%-8s", "N/A");
                        } else {
                            System.out.printf("%-8.2f", correlation);
                        }
                    }
                    System.out.println();
                }
            }

        } catch (SQLException e) {
            System.out.println("Error analyzing stock list: " + e.getMessage());
        }
    }

    private LocalDate getDateInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }

// Add this method to StockList class
    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }
}
