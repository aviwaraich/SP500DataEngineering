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

    public double calculateBetas(String symbol, LocalDate startDate, LocalDate endDate) throws SQLException {
    // Retrieve Stock Returns
    String stockReturnsSql = "SELECT date, (close - LAG(close) OVER (ORDER BY date)) / LAG(close) OVER (ORDER BY date) AS return " +
                             "FROM Stocks " +
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

    // Retrieve Market Returns
    String marketReturnsSql = "SELECT date, (close - LAG(close) OVER (ORDER BY date)) / LAG(close) OVER (ORDER BY date) AS return " +
                              "FROM Stocks " +
                              "WHERE symbol = 'SPY' AND date BETWEEN ? AND ?";
    List<Double> marketReturns = new ArrayList<>();
    
    try (PreparedStatement pstmt = conn.prepareStatement(marketReturnsSql)) {
        pstmt.setDate(1, java.sql.Date.valueOf(startDate));
        pstmt.setDate(2, java.sql.Date.valueOf(endDate));
        
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            marketReturns.add(rs.getDouble("return"));
        }
    }
    System.out.println("Market Returns (SPY): " + marketReturns);

    // If market returns are empty, there is an issue with the market data
    if (marketReturns.isEmpty()) {
        System.out.println("Market returns are empty. Check if the 'SPY' data is present in the database for the given date range.");
        return 0.0;
    }

    // Calculate Covariance and Variance
    String covarianceSql = "WITH stock_returns AS (" +
                           "  SELECT date, (close - LAG(close) OVER (ORDER BY date)) / LAG(close) OVER (ORDER BY date) AS return " +
                           "  FROM Stocks " +
                           "  WHERE symbol = ? AND date BETWEEN ? AND ?" +
                           "), " +
                           "market_returns AS (" +
                           "  SELECT date, (close - LAG(close) OVER (ORDER BY date)) / LAG(close) OVER (ORDER BY date) AS return " +
                           "  FROM Stocks " +
                           "  WHERE symbol = 'SPY' AND date BETWEEN ? AND ?" +
                           ") " +
                           "SELECT COVAR_POP(s.return, m.return) AS covariance, VAR_POP(m.return) AS variance " +
                           "FROM stock_returns s " +
                           "JOIN market_returns m ON s.date = m.date";
    
    double covariance = 0.0;
    double variance = 0.0;

    try (PreparedStatement pstmt = conn.prepareStatement(covarianceSql)) {
        pstmt.setString(1, symbol);
        pstmt.setDate(2, java.sql.Date.valueOf(startDate));
        pstmt.setDate(3, java.sql.Date.valueOf(endDate));
        pstmt.setDate(4, java.sql.Date.valueOf(startDate));
        pstmt.setDate(5, java.sql.Date.valueOf(endDate));
        
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            covariance = rs.getDouble("covariance");
            variance = rs.getDouble("variance");
        }
    }
    System.out.println("Covariance: " + covariance);
    System.out.println("Variance: " + variance);

    // Calculate Beta
    double beta = (variance != 0) ? covariance / variance : 0.0;
    System.out.println("Beta value retrieved: " + beta);

    return beta;
}




    public double calculateCoVs(String symbol, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT STDDEV(close) / AVG(close) AS cov " +
                     "FROM Stocks " +
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

    public Map<String, Map<String, Double>> calculateCorrelationMatrix(List<String> symbols, LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<String, Map<String, Double>> correlationMatrix = new HashMap<>();

        for (String symbol1 : symbols) {
            correlationMatrix.put(symbol1, new HashMap<>());
            for (String symbol2 : symbols) {
                if (symbol1.equals(symbol2)) {
                    correlationMatrix.get(symbol1).put(symbol2, 1.0);
                } else if (!correlationMatrix.containsKey(symbol2) || !correlationMatrix.get(symbol2).containsKey(symbol1)) {
                    double correlation = calculateCorrelation(symbol1, symbol2, startDate, endDate);
                    correlationMatrix.get(symbol1).put(symbol2, correlation);
                }
            }
        }

        return correlationMatrix;
    }

    private double calculateCorrelation(String symbol1, String symbol2, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "WITH returns1 AS (" +
                     "  SELECT date, (close - LAG(close) OVER (ORDER BY date)) / LAG(close) OVER (ORDER BY date) AS return " +
                     "  FROM Stocks " +
                     "  WHERE symbol = ? AND date BETWEEN ? AND ?" +
                     "), " +
                     "returns2 AS (" +
                     "  SELECT date, (close - LAG(close) OVER (ORDER BY date)) / LAG(close) OVER (ORDER BY date) AS return " +
                     "  FROM Stocks " +
                     "  WHERE symbol = ? AND date BETWEEN ? AND ?" +
                     ") " +
                     "SELECT CORR(r1.return, r2.return) AS correlation " +
                     "FROM returns1 r1 " +
                     "JOIN returns2 r2 ON r1.date = r2.date";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol1);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));
            pstmt.setString(4, symbol2);
            pstmt.setDate(5, java.sql.Date.valueOf(startDate));
            pstmt.setDate(6, java.sql.Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("correlation");
            }
        }
        return 0.0;
    }

    public List<Double> predictFuturePrice(String symbol, LocalDate startDate, LocalDate endDate, int daysToPredict) throws SQLException {
        String sql = "SELECT AVG(close) OVER (ORDER BY date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) AS ma30 " +
                     "FROM Stocks " +
                     "WHERE symbol = ? AND date <= ? " +
                     "ORDER BY date DESC " +
                     "LIMIT 30";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double lastMA30 = rs.getDouble("ma30");
                List<Double> predictions = new ArrayList<>();
                for (int i = 0; i < daysToPredict; i++) {
                    predictions.add(lastMA30);
                }
                return predictions;
            }
        }
        return new ArrayList<>();
    }

    public void addNewStockData(String symbol, LocalDate date, double open, double high, double low, double close, int volume) throws SQLException {
        String sql = "INSERT INTO Stocks (Date, Symbol, Open, High, Low, Close, Volume) " +
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
        String sql = "SELECT DISTINCT Symbol FROM Stocks";
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
        
        analysis.put("beta", calculateBetas(symbol, startDate, endDate));
        analysis.put("cov", calculateCoVs(symbol, startDate, endDate));
        
        List<Double> movingAverages = calculateMovingAverages(symbol, startDate, endDate);
        analysis.put("movingAverages", movingAverages);
        
        double[] rsi = calculateRSI(symbol, startDate, endDate);
        analysis.put("rsi", rsi);
        
        return analysis;
    }

    private List<Double> calculateMovingAverages(String symbol, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT AVG(Close) OVER (ORDER BY Date ROWS BETWEEN 9 PRECEDING AND CURRENT ROW) AS MA10, " +
                     "AVG(Close) OVER (ORDER BY Date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) AS MA30 " +
                     "FROM Stocks " +
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
                     "  FROM Stocks " +
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

    public List<Double> predictFuturePrice(String symbol, LocalDate startDate, int daysToPredict) throws SQLException {
        String sql = "SELECT Date, Close FROM Stocks WHERE Symbol = ? AND Date <= ? ORDER BY Date DESC LIMIT 30";
        
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