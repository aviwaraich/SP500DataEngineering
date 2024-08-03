public class StockListItem {
    private String symbol;
    private int shares;

    public StockListItem(String symbol, int shares) {
        this.symbol = symbol;
        this.shares = shares;
    }

    public String getSymbol() { return symbol; }
    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }

    @Override
    public String toString() {
        return symbol + " (" + shares + " shares)";
    }
}