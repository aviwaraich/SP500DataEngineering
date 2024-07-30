
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockAnalyzer {

    private Connection conn;

    public StockAnalyzer() {
        try {
            this.conn = DatabaseManager.getConnection();
        } catch (SQLException e) {
            System.out.println("Failed to establish database connection in StockAnalyzer.");
            e.printStackTrace();
        }
    }

    public Map<String, Double> calculateBetass(List<String> symbols, LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<String, Double> betas = new HashMap<>();
        String sql = "WITH stock_returns AS ("
            + "    SELECT symbol, timestamp, (close - LAG(close) OVER (PARTITION BY symbol ORDER BY timestamp)) / LAG(close) OVER (PARTITION BY symbol ORDER BY timestamp) AS return "
            + "    FROM Stocks "
            + "    WHERE symbol IN (" + String.join(",", Collections.nCopies(symbols.size(), "?")) + ") AND timestamp BETWEEN ? AND ?"
            + "), "
            + "market_returns AS ("
            + "    SELECT timestamp, AVG(return) AS market_return "
            + "    FROM stock_returns "
            + "    GROUP BY timestamp"
            + ") "
            + "SELECT sr.symbol, "
            + "       COVAR_POP(sr.return, mr.market_return) / VAR_POP(mr.market_return) AS beta "
            + "FROM stock_returns sr "
            + "JOIN market_returns mr ON sr.timestamp = mr.timestamp "
            + "GROUP BY sr.symbol";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            for (String symbol : symbols) {
                pstmt.setString(paramIndex++, symbol);
            }
            pstmt.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
            pstmt.setDate(paramIndex, java.sql.Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                betas.put(rs.getString("symbol"), rs.getDouble("beta"));
            }
        }
        return betas;
    }

    public Map<String, Double> calculateCoVss(List<String> symbols, LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<String, Double> covs = new HashMap<>();
        String sql = "SELECT symbol, STDDEV(close) / AVG(close) AS cov "
            + "FROM Stocks "
            + "WHERE symbol IN (" + String.join(",", Collections.nCopies(symbols.size(), "?")) + ") AND timestamp BETWEEN ? AND ? "
            + "GROUP BY symbol";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            for (String symbol : symbols) {
                pstmt.setString(paramIndex++, symbol);
            }
            pstmt.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
            pstmt.setDate(paramIndex, java.sql.Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                covs.put(rs.getString("symbol"), rs.getDouble("cov"));
            }
        }
        return covs;
    }

    public Map<String, Map<String, Double>> calculateCorrelationMatrix(List<String> symbols, LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<String, Map<String, Double>> correlationMatrix = new HashMap<>();

        String sql = "WITH returns AS ("
            + "  SELECT timestamp, symbol, (close - LAG(close) OVER (PARTITION BY symbol ORDER BY timestamp)) / LAG(close) OVER (PARTITION BY symbol ORDER BY timestamp) AS return "
            + "  FROM Stocks "
            + "  WHERE symbol IN (" + String.join(",", Collections.nCopies(symbols.size(), "?")) + ") AND timestamp BETWEEN ? AND ?"
            + ") "
            + "SELECT r1.symbol AS symbol1, r2.symbol AS symbol2, CORR(r1.return, r2.return) AS correlation "
            + "FROM returns r1 "
            + "JOIN returns r2 ON r1.timestamp = r2.timestamp "
            + "GROUP BY r1.symbol, r2.symbol";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            for (String symbol : symbols) {
                pstmt.setString(paramIndex++, symbol);
            }
            pstmt.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
            pstmt.setDate(paramIndex, java.sql.Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String symbol1 = rs.getString("symbol1");
                String symbol2 = rs.getString("symbol2");
                double correlation = rs.getDouble("correlation");

                correlationMatrix.computeIfAbsent(symbol1, k -> new HashMap<>()).put(symbol2, correlation);
            }
        }
        return correlationMatrix;
    }

    public void addNewStockData(String symbol, LocalDate date, double open, double high, double low, double close, int volume) throws SQLException {
        String sql = "INSERT INTO Stocks (Date, Symbol, Open, High, Low, Close, Volume) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (Date, Symbol) DO UPDATE SET "
                + "Open = EXCLUDED.Open, High = EXCLUDED.High, Low = EXCLUDED.Low, "
                + "Close = EXCLUDED.Close, Volume = EXCLUDED.Volume";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(date));
            pstmt.setString(2, symbol);
            pstmt.setDouble(3, open);
            pstmt.setDouble(4, high);
            pstmt.setDouble(5, low);
            pstmt.setDouble(6, close);
            pstmt.setInt(7, volume);

            pstmt.executeUpdate();
        }
    }

    public List<String> getAllStockSymbols() throws SQLException {
        List<String> symbols = new ArrayList<>();
        String sql = "SELECT DISTINCT Symbol FROM Stocks";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                symbols.add(rs.getString("Symbol"));
            }
        }
        return symbols;
    }

    public Map<String, Object> analyzeStock(String symbol, LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<String, Object> analysis = new HashMap<>();

        List<String> symbols = Collections.singletonList(symbol);
        Map<String, Double> betas = calculateBetass(symbols, startDate, endDate);
        Map<String, Double> covs = calculateCoVss(symbols, startDate, endDate);

        analysis.put("beta", betas.get(symbol));
        analysis.put("cov", covs.get(symbol));

        List<Double> movingAverages = calculateMovingAverages(symbol, startDate, endDate);
        analysis.put("movingAverages", movingAverages);

        double[] rsi = calculateRSI(symbol, startDate, endDate);
        analysis.put("rsi", rsi);

        return analysis;
    }

    private List<Double> calculateMovingAverages(String symbol, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT AVG(Close) OVER (ORDER BY timestamp ROWS BETWEEN 9 PRECEDING AND CURRENT ROW) AS MA10, "
            + "AVG(Close) OVER (ORDER BY timestamp ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) AS MA30 "
            + "FROM Stocks "
            + "WHERE Symbol = ? AND timestamp BETWEEN ? AND ? "
            + "ORDER BY timestamp DESC LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Arrays.asList(rs.getDouble("MA10"), rs.getDouble("MA30"));
            }
        }
        return Arrays.asList(0.0, 0.0);
    }

    private double[] calculateRSI(String symbol, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "WITH price_changes AS ("
            + "  SELECT timestamp, Close - LAG(Close) OVER (ORDER BY timestamp) AS change "
            + "  FROM Stocks "
            + "  WHERE Symbol = ? AND timestamp BETWEEN ? AND ?"
            + "), "
            + "gains_losses AS ("
            + "  SELECT timestamp, "
            + "    CASE WHEN change > 0 THEN change ELSE 0 END AS gain, "
            + "    CASE WHEN change < 0 THEN ABS(change) ELSE 0 END AS loss "
            + "  FROM price_changes"
            + ") "
            + "SELECT AVG(gain) AS avg_gain, AVG(loss) AS avg_loss "
            + "FROM gains_losses";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double avgGain = rs.getDouble("avg_gain");
                double avgLoss = rs.getDouble("avg_loss");
                double rs_new = (avgGain / avgLoss);
                double rsi = 100 - (100 / (1 + rs_new));
                return new double[]{rs_new, rsi};
            }
        }
        return new double[]{0.0, 0.0};
    }

    public List<Double> predictFuturePrice(String symbol, LocalDate startDate, int daysToPredict) throws SQLException {
        String sql = "SELECT timestamp, Close FROM Stocks WHERE Symbol = ? AND timestamp <= ? ORDER BY timestamp DESC LIMIT 30";

        List<Double> prices = new ArrayList<>();
        List<Integer> days = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));

            ResultSet rs = pstmt.executeQuery();
            int day = 0;
            while (rs.next()) {
                prices.add(rs.getDouble("Close"));
                days.add(day++);
            }
        }

        // Reverse the lists so that the oldest data comes first
        Collections.reverse(prices);
        Collections.reverse(days);

        // Perform linear regression
        double[] coefficients = linearRegression(days, prices);
        double slope = coefficients[0];
        double intercept = coefficients[1];

        // Predict future prices
        List<Double> predictions = new ArrayList<>();
        for (int i = 1; i <= daysToPredict; i++) {
            double predictedPrice = slope * (days.size() + i) + intercept;
            predictions.add(Math.max(0, predictedPrice)); // Ensure price is not negative
        }

        return predictions;
    }

    private double[] linearRegression(List<Integer> x, List<Double> y) {
        int n = x.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += x.get(i);
            sumY += y.get(i);
            sumXY += x.get(i) * y.get(i);
            sumX2 += x.get(i) * x.get(i);
        }
        double xMean = sumX / n;
        double yMean = sumY / n;
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = yMean - slope * xMean;
        return new double[]{slope, intercept};
    }

    public void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("Failed to close database connection in StockAnalyzer.");
                e.printStackTrace();
            }
        }
    }
}
