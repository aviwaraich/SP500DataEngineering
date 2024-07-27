import java.util.List;
import java.util.Scanner;

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
        System.out.println("4. Logout");
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
            loggedInUser.getPortfolios().addAll(User.loadPortfolios(username));
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
        System.out.print("Enter initial cash amount for the new portfolio: $");
        double initialCash = scanner.nextDouble();
        scanner.nextLine(); 
        
        int portfolioId = loggedInUser.getPortfolios().size() + 1; 
        Portfolio newPortfolio = new Portfolio(portfolioId, loggedInUser.getUsername(), initialCash);
        loggedInUser.addPortfolio(newPortfolio);
        
        if (User.savePortfolio(newPortfolio)) {
            System.out.println("New portfolio created successfully.");
        } else {
            System.out.println("Failed to create new portfolio. Please try again.");
            loggedInUser.getPortfolios().remove(newPortfolio);
        }
    }

    private static void managePortfolio() {
        System.out.print("Enter portfolio ID: ");
        int portfolioId = scanner.nextInt();
        scanner.nextLine(); 

        Portfolio portfolio = loggedInUser.getPortfolio(portfolioId);
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
}