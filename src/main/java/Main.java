import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        Connection conn = null;
        Statement stmt = null;

        try {
            // Register the PostgreSQL driver
            Class.forName("org.postgresql.Driver");

            // Connect to the database
            String url = "jdbc:postgresql://34.31.41.252:5432/mydb";
            String user = "postgres";
            String password = "postgres";
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Opened database successfully");

            // Create a statement object
            stmt = conn.createStatement();

            // Execute a query to retrieve data from the Stocks table
            String sql = "SELECT * FROM Stocks";
            ResultSet rs = stmt.executeQuery(sql);

            // Print the results
            System.out.println("Symbol | Timestamp | Open | High | Low | Close | Volume");
            while (rs.next()) {
                String symbol = rs.getString("symbol");
                String timestamp = rs.getString("timestamp");
                float open = rs.getFloat("open");
                float high = rs.getFloat("high");
                float low = rs.getFloat("low");
                float close = rs.getFloat("close");
                int volume = rs.getInt("volume");
                System.out.printf("%s | %s | %.2f | %.2f | %.2f | %.2f | %d%n", symbol, timestamp, open, high, low, close, volume);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(1);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
                System.out.println("Disconnected from the database");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
