public class StockHolding {
    private String symbol;
    private int shares;
    private double averagePurchasePrice;

    public StockHolding(String symbol, int shares, double averagePurchasePrice) {
        this.symbol = symbol;
        this.shares = shares;
        this.averagePurchasePrice = averagePurchasePrice;
    }

    public double getAveragePurchasePrice() {
        return averagePurchasePrice;
    }

    public void setAveragePurchasePrice(double averagePurchasePrice) {
        this.averagePurchasePrice = averagePurchasePrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getShares() { 
        return shares; 
    }

    public void setShares(int shares) { 
        this.shares = shares; 
    }

    @Override
    public String toString() {
        return symbol + ": " + shares + " shares (Avg. Price: $" + String.format("%.2f", averagePurchasePrice) + ")";
    }
}