public class StockHolding {
    private String symbol;
    private int shares;

    public StockHolding(String symbol, int shares) {
        this.symbol = symbol;
        this.shares = shares;
    }

    public String getSymbol() { return symbol; }
    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }

    @Override
    public String toString() {
        return symbol + ": " + shares + " shares";
    }
}