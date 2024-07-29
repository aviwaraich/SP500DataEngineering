import java.util.ArrayList;
import java.util.List;

public class Portfolio {
    private String name;
    private String username;
    private double cashBalance;
    private List<StockHolding> holdings;

    public Portfolio(String name, String username, double cashBalance) {
        this.name = name;
        this.username = username;
        this.cashBalance = cashBalance;
        this.holdings = new ArrayList<>();
    }

    public String getName() { return name; }
    public String getUsername() { return username; }
    public double getCashBalance() { return cashBalance; }
    public void setCashBalance(double cashBalance) { this.cashBalance = cashBalance; }
    public List<StockHolding> getHoldings() { return holdings; }

    public void addHolding(StockHolding holding) {
        holdings.add(holding);
    }

    public void removeHolding(StockHolding holding) {
        holdings.remove(holding);
    }

    public void buyStock(String symbol, int quantity, double price) {
        double cost = quantity * price;
        if (cost <= cashBalance) {
            cashBalance -= cost;
            for (StockHolding holding : holdings) {
                if (holding.getSymbol().equals(symbol)) {
                    holding.setShares(holding.getShares() + quantity);
                    return;
                }
            }
            holdings.add(new StockHolding(symbol, quantity));
            System.out.println("Bought " + quantity + " shares of " + symbol + " for $" + cost);
        } else {
            System.out.println("Insufficient funds to buy stocks.");
        }
    }

    public void sellStock(String symbol, int quantity, double price) {
        for (StockHolding holding : holdings) {
            if (holding.getSymbol().equals(symbol) && holding.getShares() >= quantity) {
                holding.setShares(holding.getShares() - quantity);
                double earnings = quantity * price;
                cashBalance += earnings;
                System.out.println("Sold " + quantity + " shares of " + symbol + " for $" + earnings);
                if (holding.getShares() == 0) {
                    holdings.remove(holding);
                }
                return;
            }
        }
        System.out.println("Insufficient shares to sell.");
    }

    @Override
    public String toString() {
        return "Portfolio Name: " + name + ", Cash: $" + String.format("%.2f", cashBalance);
    }

    public void viewDetails() {
        System.out.println(this);
        System.out.println("Holdings:");
        for (StockHolding holding : holdings) {
            System.out.println("  " + holding);
        }
    }
}
