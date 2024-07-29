import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private static User loggedInUser = null;
    private static Scanner scanner = new Scanner(System.in);

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
        System.out.println("5. Analyze Portfolio");
        System.out.println("6. Add New Stock Data");
        System.out.println("7. Analyze Any Stock");
        System.out.println("8. Log out");
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
                if (loggedInUser.getPortfolios().isEmpty()) {
                    System.out.println("You don't have any portfolios to analyze.");
                } else {
                    System.out.print("Enter portfolio name to analyze: ");
                    String portfolioName = scanner.nextLine();
                    Portfolio portfolioToAnalyze = loggedInUser.getPortfolio(portfolioName);
                    if (portfolioToAnalyze != null) {
                        analyzePortfolio(portfolioToAnalyze);
                    } else {
                        System.out.println("Portfolio not found.");
                    }
                }
                break;
            case 6:
                addNewStockData();
                break;
            case 7:
                analyzeAnyStock();
                break;
            case 8:
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
                portfolio.viewDetails();
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
            System.out.println("4. Return to Main Menu");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    portfolio.viewDetails();
                    break;
                case 2:
                    buyStock(portfolio);
                    break;
                case 3:
                    sellStock(portfolio);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void buyStock(Portfolio portfolio) {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine();
        System.out.print("Enter number of shares: ");
        int quantity = scanner.nextInt();
        System.out.print("Enter price per share: $");
        double price = scanner.nextDouble();
        scanner.nextLine();

        portfolio.buyStock(symbol, quantity, price);
    }

    private static void sellStock(Portfolio portfolio) {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine();
        System.out.print("Enter number of shares: ");
        int quantity = scanner.nextInt();
        System.out.print("Enter price per share: $");
        double price = scanner.nextDouble();
        scanner.nextLine();

        portfolio.sellStock(symbol, quantity, price);
    }

    private static void showSocialMenu() {
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
        System.out.println("12. Return to Main Menu");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

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
                return;
            default:
                System.out.println("Invalid option. Please try again.");
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

        StockList newList = loggedInUser.createStockList(name, isPublic);
        if (newList != null) {
            System.out.println("Stock list '" + name + "' created.");
        } else {
            System.out.println("Failed to create stock list. Please try again.");
        }
    }

    private static void deleteStockList() {
        System.out.print("Enter the ID of the stock list to delete: ");
        int listID = scanner.nextInt();
        scanner.nextLine();

        // MORE
        System.out.println("Stock list with ID '" + listID + "' deleted.");
    }

    private static void shareStockList() {
        System.out.print("Enter the ID of the stock list to share: ");
        int listID = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter username of the user to share with: ");
        String username = scanner.nextLine();

        // MORE
        System.out.println("Stock list with ID '" + listID + "' shared with " + username + ".");
    }

    private static void viewStockList() {
        System.out.print("Enter the ID of the stock list to view: ");
        int listID = scanner.nextInt();
        scanner.nextLine();
        StockList stockList = loggedInUser.viewStockList(listID);
        if (stockList != null) {
            stockList.viewDetails();
            while (true) {
                System.out.println("\n--- Stock List Options ---");
                System.out.println("1. Write Review");
                System.out.println("2. Delete Review");
                System.out.println("3. Return to Social Menu");
                System.out.print("Choose an option: ");

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        writeReview(stockList.getListID());
                        break;
                    case 2:
                        deleteReview(stockList.getListID());
                        break;
                    case 3:
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        }
    }

    private static void writeReview(int listID) {
        System.out.print("Enter your review: ");
        String reviewText = scanner.nextLine();
        loggedInUser.writeReview(listID, reviewText);
        System.out.println("Review added to stock list.");
    }

    private static void deleteReview(int listID) {
        loggedInUser.deleteReview(listID);
    }

     private static void analyzePortfolio(Portfolio portfolio) {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusYears(1);

            System.out.println("\n--- Portfolio Analysis ---");
            System.out.println("Betas:");
            Map<String, Double> betas = portfolio.calculateBetas(startDate, endDate);
            betas.forEach((symbol, beta) -> System.out.println(symbol + ": " + beta));

            System.out.println("\nCoefficients of Variation:");
            Map<String, Double> covs = portfolio.calculateCoVs(startDate, endDate);
            covs.forEach((symbol, cov) -> System.out.println(symbol + ": " + cov));

            System.out.println("\nCorrelation Matrix:");
            Map<String, Map<String, Double>> correlationMatrix = portfolio.calculateCorrelationMatrix(startDate, endDate);
            correlationMatrix.forEach((symbol1, correlations) -> {
                correlations.forEach((symbol2, correlation) -> {
                    System.out.println(symbol1 + " - " + symbol2 + ": " + correlation);
                });
            });

            System.out.println("\nPrice Predictions (next 7 days):");
            Map<String, List<Double>> predictions = portfolio.predictFuturePrices(endDate, 7);
            predictions.forEach((symbol, prices) -> {
                System.out.println(symbol + ": " + prices);
            });

        } catch (SQLException e) {
            System.out.println("Error analyzing portfolio: " + e.getMessage());
        }
    }

    private static void addNewStockData() {
        Scanner scanner = new Scanner(System.in);
        StockAnalyzer analyzer = new StockAnalyzer();

        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine();

        System.out.print("Enter date (YYYY-MM-DD): ");
        LocalDate date = LocalDate.parse(scanner.nextLine());

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
        }
    }

    private static void analyzeAnyStock() {
        StockAnalyzer analyzer = new StockAnalyzer();
        try {
            System.out.println("\n--- Available Stocks ---");
            //List<String> symbols = analyzer.getAllStockSymbols();

            System.out.print("Enter the stock symbol to analyze: ");
            String symbol = scanner.nextLine();

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = LocalDate.of(2013, 2, 8);

            Map<String, Object> analysis = analyzer.analyzeStock(symbol, startDate, endDate);

            System.out.println("\n--- Stock Analysis for " + symbol + " ---");

            double cov = (double) analysis.get("cov");
            System.out.println("Coefficient of Variation: " + cov);
            if (cov > 1) {
                System.out.println("Explanation: The stock has high volatility relative to its average price (CoV > 1).");
            } else if (cov < 1) {
                System.out.println("Explanation: The stock has low volatility relative to its average price (CoV < 1).");
            } else {
                System.out.println("Explanation: The stock has average volatility relative to its average price (CoV = 1).");
            }

            List<Double> movingAverages = (List<Double>) analysis.get("movingAverages");
            System.out.println("10-day Moving Average: " + movingAverages.get(0));
            System.out.println("30-day Moving Average: " + movingAverages.get(1));
            if (movingAverages.get(0) > movingAverages.get(1)) {
                System.out.println("Explanation: The stock is experiencing upward momentum (10-day MA > 30-day MA).");
            } else if (movingAverages.get(0) < movingAverages.get(1)) {
                System.out.println("Explanation: The stock is experiencing downward momentum (10-day MA < 30-day MA).");
            } else {
                System.out.println("Explanation: The stock has stable momentum (10-day MA = 30-day MA).");
            }

            double[] rsi = (double[]) analysis.get("rsi");
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
            //List<Double> predictions = analyzer.predictFuturePrice(symbol, endDate, 7);
            //for (int i = 0; i < predictions.size(); i++) {
            //    System.out.println("Day " + (i+1) + ": " + predictions.get(i));
            //}

        } catch (SQLException e) {
            System.out.println("Error analyzing stock: " + e.getMessage());
        } finally {
            analyzer.closeConnection();
        }
    }
}


