
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Portfolio {

    private String name;
    private String username;
    private double cashBalance;
    private List<StockHolding> holdings;
    private StockAnalyzer analyzer;

    public Portfolio(String name, String username, double cashBalance) {
        this.name = name;
        this.username = username;
        this.cashBalance = cashBalance;
        this.holdings = new ArrayList<>();
        this.analyzer = new StockAnalyzer();
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public double getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(double cashBalance) {
        this.cashBalance = cashBalance;
    }

    public List<StockHolding> getHoldings() {
        return holdings;
    }

    public void buyStock(String symbol, int quantity) {
        double latestPrice = getLatestClosePrice(symbol);
        if (latestPrice == -1) {
            System.out.println("Unable to retrieve the latest price for " + symbol);
            return;
        }

        double cost = quantity * latestPrice;
        if (cost <= cashBalance) {
            String updatePortfolioSql = "UPDATE Portfolio SET CashBalance = CashBalance - ? WHERE Name = ? AND Username = ?";
            String upsertHoldingSql = "INSERT INTO StockHolding (PortfolioName, Username, Symbol, Shares, AveragePurchasePrice) "
                    + "VALUES (?, ?, ?, ?, ?) "
                    + "ON CONFLICT (PortfolioName, Username, Symbol) DO UPDATE SET "
                    + "Shares = StockHolding.Shares + EXCLUDED.Shares, "
                    + "AveragePurchasePrice = (StockHolding.AveragePurchasePrice * StockHolding.Shares + EXCLUDED.AveragePurchasePrice * EXCLUDED.Shares) / (StockHolding.Shares + EXCLUDED.Shares)";

            try (Connection conn = DatabaseManager.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Update portfolio cash balance
                    try (PreparedStatement pstmt = conn.prepareStatement(updatePortfolioSql)) {
                        pstmt.setDouble(1, cost);
                        pstmt.setString(2, this.name);
                        pstmt.setString(3, this.username);
                        pstmt.executeUpdate();
                    }

                    // Update or insert stock holding
                    try (PreparedStatement pstmt = conn.prepareStatement(upsertHoldingSql)) {
                        pstmt.setString(1, this.name);
                        pstmt.setString(2, this.username);
                        pstmt.setString(3, symbol);
                        pstmt.setInt(4, quantity);
                        pstmt.setInt(5, quantity);
                        pstmt.executeUpdate();
                    }

                    conn.commit();
                    cashBalance -= cost;
                    updateLocalHoldings(symbol, quantity, latestPrice);
                    refreshCashBalance();
                    System.out.println("Bought " + quantity + " shares of " + symbol + " for $" + cost);
                } catch (SQLException e) {
                    conn.rollback();
                    System.out.println("Transaction failed. Rolling back.");
                    e.printStackTrace();
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.out.println("Database operation failed.");
                e.printStackTrace();
            }
        } else {
            System.out.println("Insufficient funds to buy stocks.");
        }
    }

    public void sellStock(String symbol, int quantity) {
        double latestPrice = getLatestClosePrice(symbol);
        if (latestPrice == -1) {
            System.out.println("Unable to retrieve the latest price for " + symbol);
            return;
        }

        String checkSharesSql = "SELECT Shares FROM StockHolding WHERE PortfolioName = ? AND Username = ? AND Symbol = ?";
        String updatePortfolioSql = "UPDATE Portfolio SET CashBalance = CashBalance + ? WHERE Name = ? AND Username = ?";
        String updateHoldingSql = "UPDATE StockHolding SET Shares = Shares - ?, "
                + "AveragePurchasePrice = CASE WHEN Shares - ? > 0 THEN AveragePurchasePrice ELSE 0 END "
                + "WHERE PortfolioName = ? AND Username = ? AND Symbol = ?";
        String deleteHoldingSql = "DELETE FROM StockHolding WHERE PortfolioName = ? AND Username = ? AND Symbol = ? AND Shares = 0";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Check if enough shares are available
                int availableShares = 0;
                try (PreparedStatement pstmt = conn.prepareStatement(checkSharesSql)) {
                    pstmt.setString(1, this.name);
                    pstmt.setString(2, this.username);
                    pstmt.setString(3, symbol);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        availableShares = rs.getInt("Shares");
                    }
                }

                if (availableShares >= quantity) {
                    double earnings = quantity * latestPrice;

                    // Update portfolio cash balance
                    try (PreparedStatement pstmt = conn.prepareStatement(updatePortfolioSql)) {
                        pstmt.setDouble(1, earnings);
                        pstmt.setString(2, this.name);
                        pstmt.setString(3, this.username);
                        pstmt.executeUpdate();
                    }

                    // Update stock holding
                    try (PreparedStatement pstmt = conn.prepareStatement(updateHoldingSql)) {
                        pstmt.setInt(1, quantity);
                        pstmt.setString(2, this.name);
                        pstmt.setString(3, this.username);
                        pstmt.setString(4, symbol);
                        pstmt.executeUpdate();
                    }

                    // Remove holding if shares become 0
                    try (PreparedStatement pstmt = conn.prepareStatement(deleteHoldingSql)) {
                        pstmt.setString(1, this.name);
                        pstmt.setString(2, this.username);
                        pstmt.setString(3, symbol);
                        pstmt.executeUpdate();
                    }

                    conn.commit();
                    cashBalance += earnings;
                    updateLocalHoldings(symbol, -quantity, latestPrice);
                    refreshCashBalance();
                    System.out.println("Sold " + quantity + " shares of " + symbol + " for $" + earnings);
                } else {
                    System.out.println("Insufficient shares to sell.");
                }
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Transaction failed. Rolling back.");
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Database operation failed.");
            e.printStackTrace();
        }
    }

    public double getLatestClosePrice(String symbol) {
        String sql = "SELECT close FROM Stocks WHERE symbol = ? ORDER BY timestamp DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("close");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void withdraw(double amount) {
        if (amount <= cashBalance) {
            String updatePortfolioSql = "UPDATE Portfolio SET CashBalance = CashBalance - ? WHERE Name = ? AND Username = ?";

            try (Connection conn = DatabaseManager.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Update portfolio cash balance
                    try (PreparedStatement pstmt = conn.prepareStatement(updatePortfolioSql)) {
                        pstmt.setDouble(1, amount);
                        pstmt.setString(2, this.name);
                        pstmt.setString(3, this.username);
                        pstmt.executeUpdate();
                    }

                    conn.commit();
                    cashBalance -= amount;
                    System.out.println("Withdrew $" + amount);
                } catch (SQLException e) {
                    conn.rollback();
                    System.out.println("Transaction failed. Rolling back.");
                    e.printStackTrace();
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.out.println("Database operation failed.");
                e.printStackTrace();
            }
        } else {
            System.out.println("Insufficient funds to withdraw.");
        }
    }

    private void updateLocalHoldings(String symbol, int quantityChange, double purchasePrice) {
        for (StockHolding holding : holdings) {
            if (holding.getSymbol().equals(symbol)) {
                int newShares = holding.getShares() + quantityChange;
                double newAveragePrice = (holding.getAveragePurchasePrice() * holding.getShares() + purchasePrice * quantityChange) / newShares;
                holding.setShares(newShares);
                holding.setAveragePurchasePrice(newAveragePrice);
                if (holding.getShares() == 0) {
                    holdings.remove(holding);
                }
                return;
            }
        }
        if (quantityChange > 0) {
            holdings.add(new StockHolding(symbol, quantityChange, purchasePrice));
        }
    }

    public void loadHoldings() {
        String sql = "SELECT * FROM StockHolding WHERE PortfolioName = ? AND Username = ?";
        holdings.clear();
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.name);
            pstmt.setString(2, this.username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String symbol = rs.getString("Symbol");
                    int shares = rs.getInt("Shares");
                    double averagePurchasePrice = rs.getDouble("AveragePurchasePrice");
                    holdings.add(new StockHolding(symbol, shares, averagePurchasePrice));
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to load holdings.");
            e.printStackTrace();
        }
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

    public static boolean save(Portfolio portfolio) {
        String sql = "INSERT INTO Portfolio (name, username, cashbalance) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, portfolio.getName());
            pstmt.setString(2, portfolio.getUsername());
            pstmt.setDouble(3, portfolio.getCashBalance());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Portfolio> loadPortfolios(String username) {
        List<Portfolio> portfolios = new ArrayList<>();
        String sql = "SELECT * FROM Portfolio WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String portfolioName = rs.getString("name");
                    double cashBalance = rs.getDouble("cashbalance");
                    Portfolio portfolio = new Portfolio(portfolioName, username, cashBalance);
                    portfolio.loadHoldings();
                    portfolio.refreshCashBalance();
                    portfolios.add(portfolio);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return portfolios;
    }

    public void refreshCashBalance() {
        String sql = "SELECT CashBalance FROM Portfolio WHERE Name = ? AND Username = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.name);
            pstmt.setString(2, this.username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    this.cashBalance = rs.getDouble("CashBalance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Double> calculateBetas(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<String> symbols = holdings.stream().map(StockHolding::getSymbol).collect(Collectors.toList());
        return analyzer.calculateBetass(symbols, startDate, endDate);
    }

    public Map<String, Double> calculateCoVs(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<String> symbols = holdings.stream().map(StockHolding::getSymbol).collect(Collectors.toList());
        return analyzer.calculateCoVss(symbols, startDate, endDate);
    }

    public Map<String, Map<String, Double>> calculateCorrelationMatrix(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<String> symbols = holdings.stream().map(StockHolding::getSymbol).collect(Collectors.toList());
        return analyzer.calculateCorrelationMatrix(symbols, startDate, endDate);
    }

    public Map<String, List<Double>> predictFuturePrices(LocalDate startDate, int daysToPredict) throws SQLException {
        Map<String, List<Double>> predictions = new HashMap<>();
        for (StockHolding holding : holdings) {
            List<Double> prediction = analyzer.predictFuturePrice(holding.getSymbol(), startDate, daysToPredict);
            predictions.put(holding.getSymbol(), prediction);
        }
        return predictions;
    }
}
