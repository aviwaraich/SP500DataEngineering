package cs.toronto.edu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		Connection conn = null;
		Statement stmt = null;

		try {
			// 
			// Register the PostgreSQL driver
			//
			//
			Class.forName("org.postgresql.Driver");

			//
			// Connect to the database
			//
			//
			conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/mydb", "postgres", "postgres");
			System.out.println("Opened database successfully");

			//
			// Create a statement object
			//
			//
			stmt = conn.createStatement();

			//
			// Create SQL statement to insert a tuple
			//
			//
			String sqlInsert = "INSERT INTO testtbl (name, value) " +
				"VALUES ('world', 1024);";
			stmt.executeUpdate(sqlInsert);
			System.out.println("Tuple inserted successfully");

			//
			// Create SQL statement to query all tuples
			//
			//
			String sqlSelect = "SELECT name, value FROM testtbl;";
			ResultSet rs = stmt.executeQuery(sqlSelect);

			//
			// Print the queried tuples
			//
			//
			System.out.println("Table testtbl contains the following tuples:\nname \tvalue");
			while (rs.next()) {
				String name = rs.getString("name");
				int value = rs.getInt("value");
				System.out.println(name + " \t" + value);
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

	private static void handleCreateCommand(String[] parts, Scanner scanner, Connection connection) {
        if (parts.length < 4) {
            System.out.println("Invalid command. Usage: create user <username> <password>");
            return;
        }

        if (parts[1].equals("user")) {
            String username = parts[2];
            String password = parts[3];
			//CREATE USER
            } 
        else if (parts[1].equals("stocklist")) {
            if (parts.length < 4) {
                System.out.println("Invalid command. Usage: create stocklist <name> <owner>");
                return;
            }
            String name = parts[2];
            User owner = findUser(parts[3]);
            if (owner == null) {
                System.out.println("User not found.");
                return;
            }
			//CREATE STOCK LIST
        } else {
            System.out.println("Invalid command. Usage: create user <username> <password> or create stocklist <name> <owner>");
        }
    }

	private static void handleAddCommand(String[] parts, Scanner scanner, Connection connection) {
        if (parts.length < 5) {
            System.out.println("Invalid command. Usage: add stock <symbol> <quantity> <stocklist>");
            return;
        }

        String symbol = parts[2];
        int quantity;
        try {
            quantity = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity.");
            return;
        }

        StockList stockList = findStockList(parts[4]);
        if (stockList == null) {
            System.out.println("Stock list not found.");
            return;
        }

        User owner = stockList.getOwner();
		//ADD STOCK HOLDING TO STOCK LIST
    }

    private static void handleShareCommand(String[] parts, Scanner scanner, Connection connection) {
        if (parts.length < 3) {
            System.out.println("Invalid command. Usage: share <stocklist> <friend>");
            return;
        }

        StockList stockList = findStockList(parts[1]);
        if (stockList == null) {
            System.out.println("Stock list not found.");
            return;
        }

        User friend = findUser(parts[2]);
        if (friend == null) {
            System.out.println("Friend not found.");
            return;
        }
		//ADD TO SHARE STOCK LIST
    }

    private static void handleReviewCommand(String[] parts, Scanner scanner, Connection connection) {
        if (parts.length < 4) {
            System.out.println("Invalid command. Usage: review <stocklist> <content>");
            return;
        }

        StockList stockList = findStockList(parts[1]);
        if (stockList == null) {
            System.out.println("Stock list not found.");
            return;
        }

        String content = parts[2];
        User author = findUser(parts[3]);
        if (author == null) {
            System.out.println("User not found.");
            return;
        }
		//INSERT REVIEW INTO STOCKLIST
    }

    private static void handleListCommand(String[] parts, Connection connection) {
        if (parts.length < 2) {
            System.out.println("Invalid command. Usage: list users or list stocklists");
            return;
        }
        if (parts[1].equals("users")) {
			//LIST USERS
        } else if (parts[1].equals("stocklists")) {
			//LIST STOCKLIST
        } else {
            System.out.println("Invalid command. Usage: list users or list stocklists");
        }
    }







}
