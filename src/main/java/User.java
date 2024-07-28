
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {

    private String username;
    private String password;
    private List<Portfolio> portfolios;
    private List<FriendRequest> friendRequests;
    private List<String> friends;
    private List<StockList> stockLists;
    private List<Review> reviews;
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

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    public static List<String> loadFriends(String username) {
        List<String> friends = new ArrayList<>();
        String sql = "SELECT * FROM Friendship WHERE user1 = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String friendUsername = rs.getString("user2");
                    friends.add(friendUsername);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    public static List<FriendRequest> loadFriendRequests(String username) {
        List<FriendRequest> friendRequests = new ArrayList<>();
        String sql = "SELECT * FROM FriendRequests WHERE receiver_username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String senderUsername = rs.getString("sender_username");
                    FriendRequest request = new FriendRequest(senderUsername, username);
                    friendRequests.add(request);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendRequests;
    }

    public static boolean savePortfolio(Portfolio portfolio) {
        String sql = "INSERT INTO Portfolios (portfolio_id, username, cash_account) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Send friend request
    public boolean sendFriendRequest(String receiver) {
        String checkSql = "SELECT request_time FROM FriendRequests WHERE sender_username = ? AND receiver_username = ? ORDER BY request_time DESC LIMIT 1";
        String insertSql = "INSERT INTO FriendRequests (sender_username, receiver_username, status, request_time) VALUES (?, ?, 'Pending', ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {

            checkPstmt.setString(1, this.username);
            checkPstmt.setString(2, receiver);

            try (ResultSet rs = checkPstmt.executeQuery()) {
                if (rs.next()) {
                    java.sql.Timestamp lastRequestTime = rs.getTimestamp("request_time");
                    java.sql.Timestamp currentTime = new java.sql.Timestamp(new java.util.Date().getTime());

                    long timeDifference = currentTime.getTime() - lastRequestTime.getTime();
                    long fiveMinutesInMillis = 5 * 60 * 1000;

                    if (timeDifference < fiveMinutesInMillis) {
                        System.out.println("You can only send a friend request to the same person every 5 minutes.");
                        return false;
                    }
                }
            }

            try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                insertPstmt.setString(1, this.username);
                insertPstmt.setString(2, receiver);
                insertPstmt.setTimestamp(3, new java.sql.Timestamp(new java.util.Date().getTime()));

                int affectedRows = insertPstmt.executeUpdate();
                if (affectedRows > 0) {
                    FriendRequest request = new FriendRequest(this.username, receiver);
                    return true;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

        // View and accept friend requests
        public boolean acceptFriendRequest(String username) {
            for (FriendRequest request : friendRequests) {
                if (request.getReceiver().equals(this.username) && request.getSender().equals(username)) {
                    String sqlUpdateRequest = "UPDATE FriendRequests SET status = 'Accepted' WHERE sender_username = ? AND receiver_username = ?";
                    String sqlInsertFriendship = "INSERT INTO Friendships (user1_username, user2_username) VALUES (?, ?), (?, ?)";
    
                    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdateRequest); PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsertFriendship)) {
    
                        // Update friend request status
                        pstmtUpdate.setString(1, request.getSender());
                        pstmtUpdate.setString(2, request.getReceiver());
                        pstmtUpdate.executeUpdate();
    
                        // Insert friendship records
                        pstmtInsert.setString(1, request.getSender());
                        pstmtInsert.setString(2, request.getReceiver());
                        pstmtInsert.setString(3, request.getReceiver());
                        pstmtInsert.setString(4, request.getSender());
                        pstmtInsert.executeUpdate();
    
                        friends.add(request.getSender());
                        friendRequests.remove(request);
                        return true;
    
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            return false;
        }
        
        //Reject Friend Request
        public boolean rejectFriendRequest(String username) {
            for (FriendRequest request : friendRequests) {
                if (request.getReceiver().equals(this.username) && request.getSender().equals(username)) {
                    String sqlUpdateRequest = "UPDATE FriendRequests SET status = 'Declined' WHERE sender_username = ? AND receiver_username = ?";
    
                    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement pstmt = conn.prepareStatement(sqlUpdateRequest)) {
    
                        pstmt.setString(1, request.getSender());
                        pstmt.setString(2, request.getReceiver());
                        pstmt.executeUpdate();
    
                        friendRequests.remove(request);
                        return true;
    
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            return false;
        }
    

    // Remove friend
    public boolean removeFriend(String friendUsername) {
        if (friends.contains(friendUsername)) {
            String sqlDeleteFriendship = "DELETE FROM Friendships WHERE (user1_username = ? AND user2_username = ?) OR (user1_username = ? AND user2_username = ?)";
            String sqlInsertFriendRequest = "INSERT INTO FriendRequests (sender_username, receiver_username, status, request_time) VALUES (?, ?, 'Declined', ?)";

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); 
                 PreparedStatement pstmtDelete = conn.prepareStatement(sqlDeleteFriendship);
                 PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsertFriendRequest)) {

                // Delete friendship record
                pstmtDelete.setString(1, this.username);
                pstmtDelete.setString(2, friendUsername);
                pstmtDelete.setString(3, friendUsername);
                pstmtDelete.setString(4, this.username);
                int affectedRows = pstmtDelete.executeUpdate();
                if (affectedRows > 0) {
                    friends.remove(friendUsername);

                    // Add a declined friend request record
                    pstmtInsert.setString(1, this.username);
                    pstmtInsert.setString(2, friendUsername);
                    pstmtInsert.setTimestamp(3, new java.sql.Timestamp(new java.util.Date().getTime()));
                    pstmtInsert.executeUpdate();

                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // Share stock list with friend
    public void shareStockList(StockList list, User friend) {
        String sql = "INSERT INTO SharedStockLists (username, list_id) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, friend.getUsername());
            pstmt.setString(2, list.getListID());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                friend.getStockLists().add(list);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Create stock list
    public StockList createStockList(String listID, String name, boolean isPublic) {
        String sql = "INSERT INTO StockLists (list_id, name, is_public, creator_username) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, listID);
            pstmt.setString(2, name);
            pstmt.setBoolean(3, isPublic);
            pstmt.setString(4, this.username);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                StockList list = new StockList(listID, name, isPublic, this.username);
                stockLists.add(list);
                return list;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public StockList viewStockList(String listID, String currentUsername) {
        String sql = "SELECT * FROM StockLists WHERE list_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, listID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    boolean isPublic = rs.getBoolean("is_public");
                    String creatorUsername = rs.getString("creator_username");
                    StockList stockList = new StockList(listID, name, isPublic, creatorUsername);

                    // Check if the stock list is public or shared with the current user
                    if (isPublic || isStockListSharedWithUser(listID, currentUsername)) {
                        // Load reviews for the stock list
                        stockList.setReviews(loadStockListReviews(listID));
                        return stockList;
                    } else {
                        System.out.println("You do not have permission to view this stock list.");
                        return null;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    

    // Method to load reviews for a stock list
    public List<Review> loadStockListReviews(String listID) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM Reviews WHERE list_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, listID);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String reviewID = rs.getString("review_id");
                    String content = rs.getString("content");
                    java.util.Date timestamp = new java.util.Date(rs.getTimestamp("timestamp").getTime());
                    String username = rs.getString("username");
                    Review review = new Review(reviewID, content, timestamp, username, listID); // Assuming the StockList will be set later
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    private boolean isStockListSharedWithUser(String listID, String username) {
        String sql = "SELECT * FROM SharedStockLists WHERE list_id = ? AND username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, listID);
            pstmt.setString(2, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Write review on stock list
    public void writeReview(String listID, String content) {
        String sql = "INSERT INTO Reviews (review_id, content, timestamp, username, list_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String reviewID = UUID.randomUUID().toString();
            pstmt.setString(1, reviewID);
            pstmt.setString(2, content);
            pstmt.setTimestamp(3, new java.sql.Timestamp(new java.util.Date().getTime()));
            pstmt.setString(4, this.username);
            pstmt.setString(5, listID);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                Review review = new Review(reviewID, content, new java.util.Date(), this.username, listID);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<FriendRequest> getFriendRequests() {
        return friendRequests;
    }

    public List<String> getFriends() {
        return friends;
    }

    public List<StockList> getStockLists() {
        return stockLists;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    // View friends
    public void viewFriends() {
        if (friends.isEmpty()) {
            System.out.println("You have no friends yet.");
            return;
        }
        System.out.println("\n--- Your Friends ---");
        for (String friendUsername : friends) {
            System.out.println(friendUsername);
        }
    }

    // View incoming friend requests
    public void viewIncomingFriendRequests() {
        String sql = "SELECT sender_username FROM FriendRequests WHERE receiver_username = ? AND status = 'Pending'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("You have no incoming friend requests.");
                    return;
                }
                System.out.println("\n--- Incoming Friend Requests ---");
                do {
                    String senderUsername = rs.getString("sender_username");
                    System.out.println(senderUsername + " has sent you a friend request.");
                } while (rs.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // View outgoing friend requests
    public void viewOutgoingFriendRequests() {
        String sql = "SELECT receiver_username FROM FriendRequests WHERE sender_username = ? AND status = 'Pending'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("You have no outgoing friend requests.");
                    return;
                }
                System.out.println("\n--- Outgoing Friend Requests ---");
                do {
                    String receiverUsername = rs.getString("receiver_username");
                    System.out.println("You have sent a friend request to " + receiverUsername + ".");
                } while (rs.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete review for a specific stock list
    public void deleteReview(String listID) {
        String sql = "DELETE FROM Reviews WHERE list_id = ? AND username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, listID);
            pstmt.setString(2, this.username);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Your review deleted from stock list '" + listID + "'.");
            } else {
                System.out.println("You have not written a review for this stock list.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
