import java.sql.*;
import java.time.LocalDate;
import java.util.*;

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

    public double calculateBeta(String symbol, LocalDate startDate, LocalDate endDate) throws SQLException {
        // Retrieve Stock Returns
        String stockReturnsSql = "SELECT date, (close - LAG(close) OVER (ORDER BY date)) / LAG(close) OVER (ORDER BY date) AS return " +
                                 "FROM DailyStockData " +
                                 "WHERE symbol = ? AND date BETWEEN ? AND ?";
        List<Double> stockReturns = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(stockReturnsSql)) {
            pstmt.setString(1, symbol);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                stockReturns.add(rs.getDouble("return"));
            }
        }
        System.out.println("Stock Returns for symbol " + symbol + ": " + stockReturns);

        // If stock returns are empty, there is an issue with the stock data
        if (stockReturns.isEmpty()) {
            System.out.println("Stock returns are empty. Check if the data is present in the database for the given date range.");
            return 0.0;
        }

        // Calculate Variance of Stock Returns
        double meanReturn = stockReturns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = stockReturns.stream().mapToDouble(r -> Math.pow(r - meanReturn, 2)).sum() / stockReturns.size();

        // If variance is zero, beta is zero
        if (variance == 0) {
            return 0.0;
        }

        return variance; // Beta calculation simplified to return variance for now
    }

    public double calculateCoV(String symbol, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT STDDEV(close) / AVG(close) AS cov " +
                     "FROM DailyStockData " +
                     "WHERE symbol = ? AND date BETWEEN ? AND ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("cov");
            }
        }
        return 0.0;
    }

    public Map<String, Map<String, Double>> calculateCovarianceMatrix(List<String> symbols, LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<String, Map<String, Double>> covarianceMatrix = new HashMap<>();

        String sql = "WITH returns AS (" +
                     "  SELECT date, symbol, (close - LAG(close) OVER (PARTITION BY symbol ORDER BY date)) / LAG(close) OVER (PARTITION BY symbol ORDER BY date) AS return " +
                     "  FROM DailyStockData " +
                     "  WHERE symbol IN (" + String.join(",", Collections.nCopies(symbols.size(), "?")) + ") AND date BETWEEN ? AND ?" +
                     ") " +
                     "SELECT r1.symbol AS symbol1, r2.symbol AS symbol2, COVAR_POP(r1.return, r2.return) AS covariance " +
                     "FROM returns r1 " +
                     "JOIN returns r2 ON r1.date = r2.date " +
                     "GROUP BY r1.symbol, r2.symbol";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int index = 1;
            for (String symbol : symbols) {
                pstmt.setString(index++, symbol);
            }
            pstmt.setDate(index++, java.sql.Date.valueOf(startDate));
            pstmt.setDate(index, java.sql.Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String symbol1 = rs.getString("symbol1");
                String symbol2 = rs.getString("symbol2");
                double covariance = rs.getDouble("covariance");

                covarianceMatrix.computeIfAbsent(symbol1, k -> new HashMap<>()).put(symbol2, covariance);
            }
        }
        return covarianceMatrix;
    }

    public Map<String, Map<String, Double>> calculateCorrelationMatrix(List<String> symbols, LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<String, Map<String, Double>> correlationMatrix = new HashMap<>();

        String sql = "WITH returns AS (" +
                     "  SELECT date, symbol, (close - LAG(close) OVER (PARTITION BY symbol ORDER BY date)) / LAG(close) OVER (PARTITION BY symbol ORDER BY date) AS return " +
                     "  FROM DailyStockData " +
                     "  WHERE symbol IN (" + String.join(",", Collections.nCopies(symbols.size(), "?")) + ") AND date BETWEEN ? AND ?" +
                     ") " +
                     "SELECT r1.symbol AS symbol1, r2.symbol AS symbol2, CORR(r1.return, r2.return) AS correlation " +
                     "FROM returns r1 " +
                     "JOIN returns r2 ON r1.date = r2.date " +
                     "GROUP BY r1.symbol, r2.symbol";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int index = 1;
            for (String symbol : symbols) {
                pstmt.setString(index++, symbol);
            }
            pstmt.setDate(index++, java.sql.Date.valueOf(startDate));
            pstmt.setDate(index, java.sql.Date.valueOf(endDate));

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

    public List<Map<String, Object>> getHistoricalPrices(String symbol, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> historicalPrices = new ArrayList<>();

        String sql = "SELECT date, close " +
                     "FROM DailyStockData " +
                     "WHERE symbol = ? AND date BETWEEN ? AND ? " +
                     "ORDER BY date";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("date", rs.getDate("date").toLocalDate());
                dataPoint.put("close", rs.getDouble("close"));
                historicalPrices.add(dataPoint);
            }
        }
        return historicalPrices;
    }

    public List<Double> predictFuturePrice(String symbol, LocalDate startDate, int daysToPredict) throws SQLException {
        String sql = "SELECT Date, Close FROM DailyStockData WHERE Symbol = ? AND Date <= ? ORDER BY Date DESC LIMIT 30";

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

        // Perform linear regression
        double[] coefficients = linearRegression(days, prices);
        double slope = coefficients[0];
        double intercept = coefficients[1];

        // Predict future prices
        List<Double> predictions = new ArrayList<>();
        for (int i = 1; i <= daysToPredict; i++) {
            predictions.add(slope * (days.size() + i) + intercept);
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

        public void addNewStockData(String symbol, LocalDate date, double open, double high, double low, double close, int volume) throws SQLException {
        String sql = "INSERT INTO DailyStockData (Date, Symbol, Open, High, Low, Close, Volume) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (Date, Symbol) DO UPDATE SET " +
                     "Open = EXCLUDED.Open, High = EXCLUDED.High, Low = EXCLUDED.Low, " +
                     "Close = EXCLUDED.Close, Volume = EXCLUDED.Volume";

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
        String sql = "SELECT DISTINCT Symbol FROM DailyStockData";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                symbols.add(rs.getString("Symbol"));
            }
        }
        return symbols;
    }

    public Map<String, Object> analyzeStock(String symbol, LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<String, Object> analysis = new HashMap<>();

        analysis.put("beta", calculateBeta(symbol, startDate, endDate));
        analysis.put("cov", calculateCoV(symbol, startDate, endDate));

        List<Double> movingAverages = calculateMovingAverages(symbol, startDate, endDate);
        analysis.put("movingAverages", movingAverages);

        double[] rsi = calculateRSI(symbol, startDate, endDate);
        analysis.put("rsi", rsi);

        return analysis;
    }

    private List<Double> calculateMovingAverages(String symbol, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT AVG(Close) OVER (ORDER BY Date ROWS BETWEEN 9 PRECEDING AND CURRENT ROW) AS MA10, " +
                     "AVG(Close) OVER (ORDER BY Date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) AS MA30 " +
                     "FROM DailyStockData " +
                     "WHERE Symbol = ? AND Date BETWEEN ? AND ? " +
                     "ORDER BY Date DESC LIMIT 1";

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
        String sql = "WITH price_changes AS (" +
                     "  SELECT Date, Close - LAG(Close) OVER (ORDER BY Date) AS change " +
                     "  FROM DailyStockData " +
                     "  WHERE Symbol = ? AND Date BETWEEN ? AND ?" +
                     "), " +
                     "gains_losses AS (" +
                     "  SELECT Date, " +
                     "    CASE WHEN change > 0 THEN change ELSE 0 END AS gain, " +
                     "    CASE WHEN change < 0 THEN ABS(change) ELSE 0 END AS loss " +
                     "  FROM price_changes" +
                     ") " +
                     "SELECT AVG(gain) AS avg_gain, AVG(loss) AS avg_loss " +
                     "FROM gains_losses";

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

