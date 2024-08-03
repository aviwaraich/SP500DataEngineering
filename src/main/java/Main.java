
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static User loggedInUser = null;
    private static Scanner scanner = new Scanner(System.in);
    private static StockAnalyzer analyzer = new StockAnalyzer();

    public static void main(String[] args) {
        while (true) {
            if (loggedInUser == null) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private static void showLoginMenu() {
        System.out.println("\n--- Login Menu ---");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                register();
                break;
            case 2:
                login();
                break;
            case 3:
                System.out.println("Goodbye!");
                scanner.close();
                System.exit(0);
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private static void showMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. View Portfolios");
        System.out.println("2. Create New Portfolio");
        System.out.println("3. Manage Portfolio");
        System.out.println("4. Social Features");
        System.out.println("5. Add New Stock Data");
        System.out.println("6. Analyze Any Stock");
        System.out.println("7. Log out");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                viewPortfolios();
                break;
            case 2:
                createPortfolio();
                break;
            case 3:
                managePortfolio();
                break;
            case 4:
                showSocialMenu();
                break;
            case 5:
                addNewStockData();
                break;
            case 6:
                analyzeAnyStock();
                break;
            case 7:
                loggedInUser = null;
                System.out.println("Logged out successfully.");
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private static void register() {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine();
        System.out.print("Enter new password: ");
        String password = scanner.nextLine();

        if (User.register(username, password)) {
            System.out.println("Registration successful!");
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }
    }

    private static void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = User.login(username, password);
        if (user != null) {
            loggedInUser = user;
            System.out.println("Login successful! Welcome, " + loggedInUser.getUsername());
        } else {
            System.out.println("Login failed. Invalid username or password.");
        }
    }

    private static void viewPortfolios() {
        List<Portfolio> portfolios = loggedInUser.getPortfolios();
        if (portfolios.isEmpty()) {
            System.out.println("You don't have any portfolios yet.");
        } else {
            System.out.println("\n--- Your Portfolios ---");
            for (Portfolio portfolio : portfolios) {
                double totalValue = portfolio.getCashBalance();
                double totalCost = 0;
                System.out.println("Portfolio Name: " + portfolio.getName());
                System.out.printf("Cash Balance: $%.2f\n", portfolio.getCashBalance());
                System.out.println("Holdings:");

                for (StockHolding holding : portfolio.getHoldings()) {
                    String symbol = holding.getSymbol();
                    int shares = holding.getShares();
                    double currentPrice = portfolio.getLatestClosePrice(symbol);
                    double purchasePrice = holding.getAveragePurchasePrice();
                    double currentValue = shares * currentPrice;
                    double cost = shares * purchasePrice;

                    totalValue += currentValue;
                    totalCost += cost;

                    System.out.printf("  %s: %d shares\n", symbol, shares);
                    System.out.printf("    Purchase Price: $%.2f\n", purchasePrice);
                    if (currentPrice != -1) {
                        double percentChange = ((currentPrice - purchasePrice) / purchasePrice) * 100;
                        System.out.printf("    Current Price: $%.2f\n", currentPrice);
                        System.out.printf("    Current Value: $%.2f\n", currentValue);
                        System.out.printf("    Performance: %.2f%% %s\n", Math.abs(percentChange),
                                percentChange > 0 ? "↑" : (percentChange < 0 ? "↓" : "−"));
                    } else {
                        System.out.println("    Current Price: Not available");
                        System.out.println("    Current Value: Not available");
                        System.out.println("    Performance: Not available");
                    }
                }

                if (totalCost > 0) {
                    double portfolioPerformance = totalCost > 0 ? ((totalValue - totalCost) / totalCost) * 100 : 0;
                    System.out.printf("Total Portfolio Value: $%.2f\n", totalValue);
                    System.out.printf("Total Portfolio Performance: %.2f%% %s\n",
                            Math.abs(portfolioPerformance),
                            portfolioPerformance > 0 ? "↑" : (portfolioPerformance < 0 ? "↓" : "−"));
                } else {
                    System.out.println("Total Portfolio Value: $" + totalValue);
                    System.out.println("Total Portfolio Performance: N/A (no investments)");
                }
                System.out.println();
            }
        }
    }

    private static void createPortfolio() {
        System.out.print("Enter name for the new portfolio: ");
        String name = scanner.nextLine();
        System.out.print("Enter initial cash amount for the new portfolio: $");
        double initialCash = scanner.nextDouble();
        scanner.nextLine();

        Portfolio newPortfolio = new Portfolio(name, loggedInUser.getUsername(), initialCash);
        loggedInUser.addPortfolio(newPortfolio);

        if (Portfolio.save(newPortfolio)) {
            System.out.println("New portfolio created successfully.");
        } else {
            System.out.println("Failed to create new portfolio. Please try again.");
            loggedInUser.getPortfolios().remove(newPortfolio);
        }
    }

    private static void managePortfolio() {
    System.out.print("Enter portfolio name: ");
    String name = scanner.nextLine();

    Portfolio portfolio = loggedInUser.getPortfolio(name);
    if (portfolio == null) {
        System.out.println("Portfolio not found.");
        return;
    }

    while (true) {
        System.out.println("\n--- Manage Portfolio ---");
        System.out.println("1. View Portfolio Details");
        System.out.println("2. Buy Stock");
        System.out.println("3. Sell Stock");
        System.out.println("4. Deposit Cash");
        System.out.println("5. Withdraw Cash");
        System.out.println("6. View Stock History");
        System.out.println("7. Predict Future Prices");
        System.out.println("8. Analyze Portfolio");
        System.out.println("9. Return to Main Menu");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine();


            switch (choice) {
                case 1:
                    viewMyPortfolio(portfolio);
                    break;
                case 2:
                    buyStock(portfolio);
                    break;
                case 3:
                    sellStock(portfolio);
                    break;
                case 4:
                    depositCash(portfolio);
                    break;
                case 5:
                    withdrawCash(portfolio);
                    break;
                case 6:
                    viewStockHistory(portfolio);
                    break;
                case 7:
                    try {
                        portfolio.predictFuturePrices();
                    } catch (SQLException e) {
                        System.out.println("Error predicting future prices: " + e.getMessage());
                    }
                    break;
                case 8:
                portfolio.analyzePortfolio();
                break;
            case 9:
                return;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }
}

    private static void depositCash(Portfolio portfolio) {
        System.out.print("Enter amount to deposit: $");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        portfolio.setCashBalance(amount);
        System.out.println("Deposited $" + amount);
    }

    private static void withdrawCash(Portfolio portfolio) {
        System.out.print("Enter amount to withdraw: $");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        portfolio.withdraw(amount);
    }

    private static void buyStock(Portfolio portfolio) {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine();
        System.out.print("Enter number of shares: ");
        int quantity = scanner.nextInt();
        scanner.nextLine();

        portfolio.buyStock(symbol, quantity);
    }

    private static void sellStock(Portfolio portfolio) {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine();
        System.out.print("Enter number of shares: ");
        int quantity = scanner.nextInt();
        scanner.nextLine();

        portfolio.sellStock(symbol, quantity);
    }

    private static void showSocialMenu() {
        while (true) {
            System.out.println("\n--- Social Features ---");
            System.out.println("1. View Friends");
            System.out.println("2. View Incoming Friend Requests");
            System.out.println("3. View Outgoing Friend Requests");
            System.out.println("4. Send Friend Request");
            System.out.println("5. Accept Friend Request");
            System.out.println("6. Reject Friend Request");
            System.out.println("7. Remove Friend");
            System.out.println("8. Create Stock List");
            System.out.println("9. Delete Stock List");
            System.out.println("10. Share Stock List");
            System.out.println("11. View Stock List");
            System.out.println("12. View Stock List Statistics");
            System.out.println("13. Return to Main Menu");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    loggedInUser.viewFriends();
                    break;
                case 2:
                    loggedInUser.viewIncomingFriendRequests();
                    break;
                case 3:
                    loggedInUser.viewOutgoingFriendRequests();
                    break;
                case 4:
                    sendFriendRequest();
                    break;
                case 5:
                    acceptFriendRequest();
                    break;
                case 6:
                    rejectFriendRequest();
                    break;
                case 7:
                    removeFriend();
                    break;
                case 8:
                    createStockList();
                    break;
                case 9:
                    deleteStockList();
                    break;
                case 10:
                    shareStockList();
                    break;
                case 11:
                    viewStockList();
                    break;
                case 12:
                    viewStockListStatistics();
                    break;
                case 13:
                    return; // This will exit the method and return to the main menu
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void sendFriendRequest() {
        System.out.print("Enter username of the user to send a friend request to: ");
        String username = scanner.nextLine();
        if (loggedInUser.sendFriendRequest(username)) {
            System.out.println("Friend request sent to " + username + ".");
        } else {
            System.out.println("User not found.");
        }
    }

    private static void acceptFriendRequest() {
        System.out.print("Enter username of the user whose friend request you want to accept: ");
        String username = scanner.nextLine();
        if (loggedInUser.acceptFriendRequest(username)) {
            System.out.println("Friend request from " + username + " accepted.");
        } else {
            System.out.println("User not found.");
        }
    }

    private static void rejectFriendRequest() {
        System.out.print("Enter username of the user whose friend request you want to reject: ");
        String username = scanner.nextLine();
        if (loggedInUser.rejectFriendRequest(username)) {
            System.out.println("Friend request from " + username + " rejected.");
        } else {
            System.out.println("User not found.");
        }
    }

    private static void removeFriend() {
        System.out.print("Enter username of the friend you want to remove: ");
        String username = scanner.nextLine();
        if (loggedInUser.removeFriend(username)) {
            System.out.println("Friend " + username + " removed.");
        } else {
            System.out.println("User not found.");
        }
    }

    private static void createStockList() {
        System.out.print("Enter name for the new stock list: ");
        String name = scanner.nextLine();
        System.out.print("Is the stock list public? (true/false): ");
        boolean isPublic = scanner.nextBoolean();
        scanner.nextLine();
        if (loggedInUser.createStockList(name, isPublic)) {
            System.out.println("Stock list '" + name + "' created.");
        } else {
            System.out.println("Failed to create stock list. Please try again.");
        }
    }

    private static void deleteStockList() {
        System.out.print("Enter the ID of the stock list to delete: ");
        int listID = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        if (loggedInUser.deleteStockList(listID)) {
            System.out.println("Stock list with ID '" + listID + "' deleted.");
        } else {
            System.out.println("Failed to delete stock list. Please try again.");
        }
    }

    private static void shareStockList() {
        List<StockList> shareableStockLists = getShareableStockLists(loggedInUser.getUsername());

        if (shareableStockLists.isEmpty()) {
            System.out.println("You don't have any private stock lists to share.");
            return;
        }

        System.out.println("\n--- Your Private Stock Lists ---");
        for (StockList list : shareableStockLists) {
            System.out.printf("ID: %d, Name: %s\n", list.getListID(), list.getName());
        }

        System.out.print("Enter the ID of the stock list to share: ");
        int listID = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        StockList selectedList = shareableStockLists.stream()
                .filter(list -> list.getListID() == listID)
                .findFirst()
                .orElse(null);

        if (selectedList == null) {
            System.out.println("Invalid stock list ID. Please try again.");
            return;
        }

        System.out.print("Enter username of the user to share with: ");
        String username = scanner.nextLine();

        loggedInUser.shareStockList(listID, username);
        System.out.println("Stock list '" + selectedList.getName() + "' shared with " + username + ".");
    }

    private static List<StockList> getShareableStockLists(String username) {
        List<StockList> shareableStockLists = new ArrayList<>();
        String sql = "SELECT * FROM StockList WHERE Creator = ? AND ispublic = FALSE";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int listID = rs.getInt("listid");
                    String name = rs.getString("name");
                    boolean isPublic = rs.getBoolean("ispublic");
                    String creator = rs.getString("Creator");
                    StockList stockList = new StockList(listID, name, isPublic, creator);
                    shareableStockLists.add(stockList);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shareableStockLists;
    }

    private static void viewStockList() {
        List<StockList> viewableStockLists = loggedInUser.getViewableStockLists();

        while (true) {
            System.out.println("\n--- Available Stock Lists ---");
            for (StockList list : viewableStockLists) {
                System.out.printf("ID: %d, Name: %s, Creator: %s, Public: %s\n",
                        list.getListID(),
                        list.getName(),
                        list.getCreator(),
                        list.isPublic() ? "Yes" : "No");
            }
            System.out.print("Enter the ID of the stock list to view (or 0 to return): ");
            int listID = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (listID == 0) {
                return;
            }

            StockList selectedList = viewableStockLists.stream()
                    .filter(list -> list.getListID() == listID)
                    .findFirst()
                    .orElse(null);

            if (selectedList != null) {
                StockList detailedList = loggedInUser.viewStockList(selectedList.getListID());
                if (detailedList != null) {
                    detailedList.viewDetails();
                    while (true) {
                        System.out.println("\n--- Stock List Options ---");
                        System.out.println("1. Write Review");
                        System.out.println("2. Delete Review");
                        System.out.println("3. Add Stock");
                        System.out.println("4. Delete Stock");
                        System.out.println("5. Return to Stock List Selection");
                        System.out.print("Choose an option: ");

                        int choice = scanner.nextInt();
                        scanner.nextLine();

                        switch (choice) {
                            case 1:
                                writeReview(listID);
                                break;
                            case 2:
                                deleteReview(listID);
                                break;
                            case 3:
                                addStockToList(listID);
                                break;
                            case 4:
                                deleteStockFromList(listID);
                                break;
                            case 5:
                                break;
                            default:
                                System.out.println("Invalid option. Please try again.");
                        }
                        if (choice == 5) {
                            break;
                        }
                    }
                } else {
                    System.out.println("Error: Unable to load details for the selected stock list.");
                }
            } else {
                System.out.println("Invalid stock list ID. Please enter a valid ID.");
            }
        }
    }

    private static void writeReview(int listID) {
    System.out.print("Enter your review: ");
    String reviewText = scanner.nextLine();
    loggedInUser.writeReview(listID, reviewText);
}

    private static void deleteReview(int listID) {
        System.out.print("Are you sure you want to delete your review? (yes/no): ");
        String confirmation = scanner.nextLine();
        if (confirmation.equalsIgnoreCase("yes")) {
            loggedInUser.deleteReview(listID);
            System.out.println("Review deleted from stock list.");
        } else {
            System.out.println("Review deletion cancelled.");
        }
    }

    private static void addStockToList(int listID) {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine();
        System.out.print("Enter number of shares: ");
        int shares = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        // Check if the stock exists in the database
        if (loggedInUser.addStockToList(listID, symbol, shares)) {
            System.out.println("Stock '" + symbol + "' added to stock list '" + listID + "'.");
        } else {
            System.out.println("Failed to add stock. Please try again.");
        }
    }

    private static void deleteStockFromList(int listID) {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine();
        // Delete the stock from the stock list
        if (loggedInUser.deleteStockFromList(listID, symbol)) {
            System.out.println("Stock '" + symbol + "' deleted from stock list '" + listID + "'.");
        } else {
            System.out.println("Failed to delete stock. Please try again.");
        }
    }

    
// Helper methods

    private static double getLatestClosePrice(String symbol) throws SQLException {
        String sql = "SELECT close FROM Stocks WHERE symbol = ? ORDER BY timestamp DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("close");
            }
        }
        return 0.0; // Fallback to 0 if no data found
    }

    private List<String> getPortfolioSymbols(String portfolioName, String username) throws SQLException {
        List<String> symbols = new ArrayList<>();
        String sql = "SELECT Symbol FROM StockHolding WHERE PortfolioName = ? AND Username = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, portfolioName);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                symbols.add(rs.getString("Symbol"));
            }
        }
        return symbols;
    }

    private static LocalDate getEarliestStockDate(String symbol) throws SQLException {
        String sql = "SELECT MIN(timestamp) as earliest_date FROM Stocks WHERE symbol = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDate("earliest_date").toLocalDate();
            }
        }
        return LocalDate.now(); // Fallback to current date if no data found
    }

    private static LocalDate getLatestStockDate(String symbol) throws SQLException {
        String sql = "SELECT MAX(timestamp) as latest_date FROM Stocks WHERE symbol = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDate("latest_date").toLocalDate();
            }
        }
        return LocalDate.now(); // Fallback to current date if no data found
    }

    private static int getShares(String portfolioName, String username, String symbol) throws SQLException {
        String sql = "SELECT Shares FROM StockHolding WHERE PortfolioName = ? AND Username = ? AND Symbol = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, portfolioName);
            pstmt.setString(2, username);
            pstmt.setString(3, symbol);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("Shares");
            }
        }
        return 0; // Fallback to 0 if no data found
    }

    private static double getCashBalance(String portfolioName, String username) throws SQLException {
        String sql = "SELECT CashBalance FROM Portfolio WHERE Name = ? AND Username = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, portfolioName);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("CashBalance");
            }
        }
        return 0.0; // Fallback to 0 if no data found
    }

    private static void addNewStockData() {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine();

        System.out.print("Enter date (YYYY-MM-DD): ");
        LocalDate date = LocalDate.parse(scanner.nextLine());

        if (date.isBefore(LocalDate.of(2018, 2, 8))) {
            System.out.println("Cannot add data before 2018-02-08. Historical data is read-only.");
            return;
        }

        System.out.print("Enter open price: ");
        double open = scanner.nextDouble();

        System.out.print("Enter high price: ");
        double high = scanner.nextDouble();

        System.out.print("Enter low price: ");
        double low = scanner.nextDouble();

        System.out.print("Enter close price: ");
        double close = scanner.nextDouble();

        System.out.print("Enter volume: ");
        int volume = scanner.nextInt();

        try {
            analyzer.addNewStockData(symbol, date, open, high, low, close, volume);
            System.out.println("New stock data added successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding new stock data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void analyzeAnyStock() {
    try {
        List<String> symbols = analyzer.getAllStockSymbols();

        System.out.print("Enter the stock symbol to analyze: ");
        String symbol = scanner.nextLine();

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = LocalDate.of(2013, 2, 8);

        System.out.print("Do you want to specify a custom date range for analysis? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.print("Enter start date for analysis (YYYY-MM-DD): ");
            startDate = LocalDate.parse(scanner.nextLine());
            System.out.print("Enter end date for analysis (YYYY-MM-DD): ");
            endDate = LocalDate.parse(scanner.nextLine());
        }

        System.out.println("\n--- Stock Analysis for " + symbol + " ---");
        System.out.println("Analyzing data from " + startDate + " to " + endDate);

        double cov = analyzer.calculateCoV(symbol, startDate, endDate);
        System.out.println("Coefficient of Variation: " + cov);
        if (cov > 1) {
            System.out.println("Explanation: The stock has high volatility relative to its average price (CoV > 1).");
        } else if (cov < 1) {
            System.out.println("Explanation: The stock has low volatility relative to its average price (CoV < 1).");
        } else {
            System.out.println("Explanation: The stock has average volatility relative to its average price (CoV = 1).");
        }

        double beta = analyzer.calculateBeta(symbol, startDate, endDate);
        System.out.println("Beta: " + beta);
        if (beta > 1) {
            System.out.println("Explanation: The stock is more volatile than the market (Beta > 1).");
        } else if (beta < 1) {
            System.out.println("Explanation: The stock is less volatile than the market (Beta < 1).");
        } else {
            System.out.println("Explanation: The stock has the same volatility as the market (Beta = 1).");
        }

        List<Double> movingAverages = analyzer.calculateMovingAverages(symbol, startDate, endDate);
        System.out.println("10-day Moving Average: " + movingAverages.get(0));
        System.out.println("30-day Moving Average: " + movingAverages.get(1));
        if (movingAverages.get(0) > movingAverages.get(1)) {
            System.out.println("Explanation: The stock is experiencing upward momentum (10-day MA > 30-day MA).");
        } else if (movingAverages.get(0) < movingAverages.get(1)) {
            System.out.println("Explanation: The stock is experiencing downward momentum (10-day MA < 30-day MA).");
        } else {
            System.out.println("Explanation: The stock has stable momentum (10-day MA = 30-day MA).");
        }

        double[] rsi = analyzer.calculateRSI(symbol, startDate, endDate);
        System.out.println("Relative Strength: " + rsi[0]);
        System.out.println("Relative Strength Index (RSI): " + rsi[1]);
        if (rsi[1] > 70) {
            System.out.println("Explanation: The stock is overbought (RSI > 70).");
        } else if (rsi[1] < 30) {
            System.out.println("Explanation: The stock is oversold (RSI < 30).");
        } else {
            System.out.println("Explanation: The stock is in a neutral state (30 <= RSI <= 70).");
        }

        System.out.println("\nPrice Predictions (next 7 days):");
        List<Double> predictions = analyzer.predictFuturePrice(symbol, endDate, 7);
        for (int i = 0; i < predictions.size(); i++) {
            System.out.printf("Day %d: $%.2f\n", i + 1, predictions.get(i));
        }

    } catch (SQLException e) {
        System.out.println("Error analyzing stock: " + e.getMessage());
    }
}
    private static void viewStockHistory(Portfolio portfolio) {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine();
        try {
            portfolio.viewStockHistory(symbol);
        } catch (SQLException e) {
            System.out.println("Error viewing stock history: " + e.getMessage());
        }
    }

    private static void viewStockListStatistics() {
    System.out.print("Enter the ID of the stock list to analyze: ");
    int listID = scanner.nextInt();
    scanner.nextLine(); 
    loggedInUser.viewStockListStatistics(listID, scanner);
}
    private static void viewMyPortfolio(Portfolio portfolio) {
        if (portfolio == null) {
            System.out.println("You don't have any portfolios yet.");
        } else {
            System.out.println("\n--- Your Portfolio ---");
                double totalValue = portfolio.getCashBalance();
                double totalCost = 0;
                System.out.println("Portfolio Name: " + portfolio.getName());
                System.out.printf("Cash Balance: $%.2f\n", portfolio.getCashBalance());
                System.out.println("Holdings:");

                for (StockHolding holding : portfolio.getHoldings()) {
                    String symbol = holding.getSymbol();
                    int shares = holding.getShares();
                    double currentPrice = portfolio.getLatestClosePrice(symbol);
                    double purchasePrice = holding.getAveragePurchasePrice();
                    double currentValue = shares * currentPrice;
                    double cost = shares * purchasePrice;

                    totalValue += currentValue;
                    totalCost += cost;

                    System.out.printf("  %s: %d shares\n", symbol, shares);
                    System.out.printf("    Purchase Price: $%.2f\n", purchasePrice);
                    if (currentPrice != -1) {
                        double percentChange = ((currentPrice - purchasePrice) / purchasePrice) * 100;
                        System.out.printf("    Current Price: $%.2f\n", currentPrice);
                        System.out.printf("    Current Value: $%.2f\n", currentValue);
                        System.out.printf("    Performance: %.2f%% %s\n", Math.abs(percentChange),
                                percentChange > 0 ? "↑" : (percentChange < 0 ? "↓" : "−"));
                    } else {
                        System.out.println("    Current Price: Not available");
                        System.out.println("    Current Value: Not available");
                        System.out.println("    Performance: Not available");
                    }
                }

                if (totalCost > 0) {
                    double portfolioPerformance = totalCost > 0 ? ((totalValue - totalCost) / totalCost) * 100 : 0;
                    System.out.printf("Total Portfolio Value: $%.2f\n", totalValue);
                    System.out.printf("Total Portfolio Performance: %.2f%% %s\n",
                            Math.abs(portfolioPerformance),
                            portfolioPerformance > 0 ? "↑" : (portfolioPerformance < 0 ? "↓" : "−"));
                } else {
                    System.out.println("Total Portfolio Value: $" + totalValue);
                    System.out.println("Total Portfolio Performance: N/A (no investments)");
                }
                System.out.println();
        }
    }

}
