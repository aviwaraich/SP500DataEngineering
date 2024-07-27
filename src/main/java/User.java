import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private List<Portfolio> portfolios;
    private static final String DB_URL = "jdbc:postgresql://34.42.244.201:5432/mydb";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.portfolios = new ArrayList<>();
    }

    public static boolean register(String username, String password) {
        String sql = "INSERT INTO Users (username, password) VALUES (?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password); 
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static User login(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password); 
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getString("username"), rs.getString("password"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addPortfolio(Portfolio portfolio) {
        portfolios.add(portfolio);
    }

    public List<Portfolio> getPortfolios() {
        return portfolios;
    }

    public Portfolio getPortfolio(int portfolioId) {
        for (Portfolio portfolio : portfolios) {
            if (portfolio.getPortfolioId() == portfolioId) {
                return portfolio;
            }
        }
        return null;
    }

    public static List<Portfolio> loadPortfolios(String username) {
        List<Portfolio> portfolios = new ArrayList<>();
        String sql = "SELECT * FROM Portfolios WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int portfolioId = rs.getInt("portfolio_id");
                    double cashAccount = rs.getDouble("cash_account");
                    Portfolio portfolio = new Portfolio(portfolioId, username, cashAccount);
                    portfolios.add(portfolio);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return portfolios;
    }

    public static boolean savePortfolio(Portfolio portfolio) {
        String sql = "INSERT INTO Portfolios (portfolio_id, username, cash_account) VALUES (?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, portfolio.getPortfolioId());
            pstmt.setString(2, portfolio.getUsername());
            pstmt.setDouble(3, portfolio.getCashAccount());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}