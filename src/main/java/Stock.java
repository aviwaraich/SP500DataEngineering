public class Stock {
    private String symbol;
    private int shares;

    public Stock(String symbol, int shares) {
        this.symbol = symbol;
        this.shares = shares;
    }

    public String getSymbol() { return symbol; }
    public int getShares() { return shares; }

    @Override
    public String toString() {
        return symbol + " (" + shares + " shares)";
    }
}

