
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User {

    private String username;
    private String password;
    private List<Portfolio> portfolios;
    private List<FriendRequest> friendRequests;
    private List<String> friends;
    private List<StockList> stockLists;
    private List<Review> reviews;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.portfolios = new ArrayList<>();
        this.stockLists = loadStockLists(username);
        this.friends = loadFriends(username);
        this.reviews = loadReviews(username);
    }

    public static boolean register(String username, String password) {
        String sql = "INSERT INTO Users (username, password) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(rs.getString("username"), rs.getString("password"));
                    user.loadUserData();
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadUserData() {
        this.portfolios = Portfolio.loadPortfolios(this.username);
        this.friends = loadFriends(this.username);
        this.friendRequests = loadFriendRequests(this.username);
        this.stockLists = loadStockLists(username);
        this.reviews = loadReviews();
    }

    public void addPortfolio(Portfolio portfolio) {
        portfolios.add(portfolio);
    }

    public List<Portfolio> getPortfolios() {
        return portfolios;
    }

    public Portfolio getPortfolio(String portfolioName) {
        for (Portfolio portfolio : portfolios) {
            if (portfolio.getName().equals(portfolioName)) {
                return portfolio;
            }
        }
        return null;
    }

    public String getUsername() {
        return username;
    }

    public List<StockList> getViewableStockLists() {
        return loadStockLists(this.username);
    }

    public static List<Portfolio> loadPortfolios(String username) {
        List<Portfolio> portfolios = new ArrayList<>();
        String sql = "SELECT * FROM Portfolio WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String portfolioName = rs.getString("name");
                    double cashBalance = rs.getDouble("cashbalance");
                    Portfolio portfolio = new Portfolio(portfolioName, username, cashBalance);
                    portfolios.add(portfolio);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return portfolios;
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

    private List<String> loadFriends(String Username) {
        List<String> friends = new ArrayList<>();
        String sql = "SELECT username2 FROM Friendship WHERE username1 = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, Username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    friends.add(rs.getString("username2"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    public static List<FriendRequest> loadFriendRequests(String username) {
        List<FriendRequest> friendRequests = new ArrayList<>();
        String sql = "SELECT * FROM FriendRequest WHERE Sender = ? OR Receiver = ?";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String Sender = rs.getString("Sender");
                    String Receiver = rs.getString("Receiver");
                    Date requestTime = new Date(rs.getTimestamp("requesttime").getTime());
                    String status = rs.getString("status");

                    FriendRequest request = new FriendRequest(Sender, Receiver);
                    request.setRequestTime(requestTime);
                    request.setStatus(status);
                    friendRequests.add(request);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendRequests;
    }

    public static StockList loadStockList(String username, int listID) {
    String sql = "SELECT * FROM StockList WHERE (ispublic = TRUE OR listid IN (SELECT listid FROM SharedStockList WHERE username = ?)) AND listid = ?";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, username);
        pstmt.setInt(2, listID);

        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                int id = rs.getInt("listid");
                String name = rs.getString("name");
                boolean isPublic = rs.getBoolean("ispublic");
                String creator = rs.getString("Creator");
                return new StockList(id, name, isPublic, creator);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

    public static List<Review> loadReviews(String username) {
        List<Review> userReviews = new ArrayList<>();
        String sql = "SELECT * FROM Review WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int reviewID = rs.getInt("reviewid");
                    String content = rs.getString("content");
                    Date timestamp = new Date(rs.getTimestamp("timestamp").getTime());
                    int listID = rs.getInt("listid");
                    Review review = new Review(reviewID, content, timestamp, username, listID);
                    userReviews.add(review);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userReviews;
    }

    public static boolean savePortfolio(Portfolio portfolio) {
        String sql = "INSERT INTO Portfolio (name, username, cashbalance) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, portfolio.getName());
            pstmt.setString(2, portfolio.getUsername());
            pstmt.setDouble(3, portfolio.getCashBalance());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean sendFriendRequest(String receiver) {
        String checkSql = "SELECT requesttime FROM FriendRequest WHERE Sender = ? AND Receiver = ? ORDER BY requesttime DESC LIMIT 1";
        String insertSql = "INSERT INTO FriendRequest (Sender, Receiver, status, requesttime) VALUES (?, ?, 'Pending', ?)";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {

            checkPstmt.setString(1, this.username);
            checkPstmt.setString(2, receiver);

            try (ResultSet rs = checkPstmt.executeQuery()) {
                if (rs.next()) {
                    java.sql.Timestamp lastRequestTime = rs.getTimestamp("requesttime");
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
                    loadFriendRequests(username);
                    return true;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean acceptFriendRequest(String username) {
        for (FriendRequest request : friendRequests) {
            if (request.getReceiver().equals(this.username) && request.getSender().equals(username)) {
                String sqlUpdateRequest = "UPDATE FriendRequest SET status = 'Accepted' WHERE Sender = ? AND Receiver = ?";
                String sqlInsertFriendship = "INSERT INTO Friendship (username1, username2) VALUES (?, ?), (?, ?)";

                try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdateRequest); PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsertFriendship)) {

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

    public boolean rejectFriendRequest(String username) {
        for (FriendRequest request : friendRequests) {
            if (request.getReceiver().equals(this.username) && request.getSender().equals(username)) {
                String sqlUpdateRequest = "UPDATE FriendRequest SET status = 'Declined' WHERE Sender = ? AND Receiver = ?";

                try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlUpdateRequest)) {

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

    public boolean removeFriend(String friendUsername) {
        if (friends.contains(friendUsername)) {
            String sqlDeleteFriendship = "DELETE FROM Friendship WHERE (username1 = ? AND username2 = ?) OR (username1 = ? AND username2 = ?)";
            String sqlInsertFriendRequest = "INSERT INTO FriendRequest (Sender, Receiver, status, requesttime) VALUES (?, ?, 'Declined', ?)";

            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmtDelete = conn.prepareStatement(sqlDeleteFriendship); PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsertFriendRequest)) {

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
    public void shareStockList(int listID, String friend) {
        // Fetch the stock list from the database based on listID
        StockList list = getStockList(listID);

        if (list != null) {
            if (list.getCreator().equals(this.username)) { // Check if the current user is the creator
                String sql = "INSERT INTO SharedStockList (username, listid) VALUES (?, ?)";
                try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setString(1, friend);
                    pstmt.setInt(2, listID);

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        System.out.println("Stock list '" + listID + "' shared with " + friend + ".");
                    } else {
                        System.out.println("Failed to share stock list. Please try again.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("You do not own this stock list and cannot share it.");
            }
        } else {
            System.out.println("Stock list with ID '" + listID + "' not found.");
        }
    }

// Helper method to fetch a stock list from the database
    private StockList getStockList(int listID) {
        String sql = "SELECT * FROM StockList WHERE listid = ?";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    boolean isPublic = rs.getBoolean("ispublic");
                    String Creator = rs.getString("Creator");
                    return new StockList(listID, name, isPublic, Creator);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createStockList(String name, boolean isPublic) {
        String sql = "INSERT INTO StockList (name, ispublic, Creator) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setBoolean(2, isPublic);
            pstmt.setString(3, this.username);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int listID = generatedKeys.getInt(1);
                        StockList list = new StockList(listID, name, isPublic, this.username);
                        stockLists.add(list);
                        return true;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public StockList viewStockList(int listID) {
    String sql = "SELECT * FROM StockList WHERE listid = ?";

    try (Connection conn = DatabaseManager.getConnection(); 
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, listID);

        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                String name = rs.getString("name");
                boolean isPublic = rs.getBoolean("ispublic");
                String creator = rs.getString("Creator");
                StockList stockList = new StockList(listID, name, isPublic, creator);

                // Check if the user has permission to view this stock list
                if (isPublic || creator.equals(this.username) || isStockListSharedWithUser(listID, this.username)) {
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

    private List<Review> loadStockListReviews(int listID) {
        List<Review> listReviews = new ArrayList<>();
        String sql = "SELECT * FROM Review WHERE listid = ?";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listID);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int reviewID = rs.getInt("reviewid");
                    String content = rs.getString("content");
                    Date timestamp = new Date(rs.getTimestamp("timestamp").getTime());
                    String username = rs.getString("username");
                    Review review = new Review(reviewID, content, new Date(timestamp.getTime()), username, listID);
                    listReviews.add(review);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listReviews;
    }

    private boolean isStockListSharedWithUser(int listID, String username) {
        String sql = "SELECT * FROM SharedStockList WHERE listid = ? AND username = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listID);
            pstmt.setString(2, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void writeReview(int listID, String content) {
        String sql = "INSERT INTO Review (content, timestamp, username, listid) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, content);
            pstmt.setTimestamp(2, new java.sql.Timestamp(new java.util.Date().getTime()));
            pstmt.setString(3, this.username);
            pstmt.setInt(4, listID);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int reviewID = generatedKeys.getInt(1);
                        Review review = new Review(reviewID, content, new Date(), this.username, listID);
                        reviews.add(review);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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

    public void viewIncomingFriendRequests() {
        String sql = "SELECT Sender FROM FriendRequest WHERE Receiver = ? AND status = 'Pending'";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("You have no incoming friend requests.");
                    return;
                }
                System.out.println("\n--- Incoming Friend Requests ---");
                do {
                    String Sender = rs.getString("Sender");
                    System.out.println(Sender + " has sent you a friend request.");
                } while (rs.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void viewOutgoingFriendRequests() {
        String sql = "SELECT Receiver FROM FriendRequest WHERE Sender = ? AND status = 'Pending'";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("You have no outgoing friend requests.");
                    return;
                }
                System.out.println("\n--- Outgoing Friend Requests ---");
                do {
                    String Receiver = rs.getString("Receiver");
                    System.out.println("You have sent a friend request to " + Receiver + ".");
                } while (rs.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteReview(int listID) {
        String sql = "DELETE FROM Review WHERE listid = ? AND username = ?";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listID);
            pstmt.setString(2, this.username);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Your review deleted from stock list '" + listID + "'.");
                reviews.removeIf(review -> review.getListID() == listID && review.getUser().equals(this.username));
            } else {
                System.out.println("You have not written a review for this stock list.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete a stock list
    public boolean deleteStockList(int listID) {
        // Check if the user owns the stock list
        StockList list = getStockList(listID);
        if (list != null && list.getCreator().equals(this.username)) {
            String sql = "DELETE FROM StockList WHERE listid = ?";

            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, listID);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    // Update the stock list in the User object
                    stockLists.removeIf(stockList -> stockList.getListID() == listID);
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("You do not own this stock list and cannot delete it.");
        }
        return false;
    }

    private static List<StockList> loadStockLists(String username) {
    List<StockList> stockLists = new ArrayList<>();
    String sql = "SELECT * FROM StockList WHERE Creator = ? OR listid IN (SELECT listid FROM SharedStockList WHERE username = ?) OR ispublic = TRUE";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, username);
        pstmt.setString(2, username);

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int listID = rs.getInt("listid");
                String name = rs.getString("name");
                boolean isPublic = rs.getBoolean("ispublic");
                String creator = rs.getString("Creator");
                StockList stockList = new StockList(listID, name, isPublic, creator);
                stockLists.add(stockList);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return stockLists;
}

    private List<Review> loadReviews() {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM Review WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, this.username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int reviewID = rs.getInt("reviewid");
                    String content = rs.getString("content");
                    Date timestamp = new Date(rs.getTimestamp("timestamp").getTime());
                    int listID = rs.getInt("listid");
                    Review review = new Review(reviewID, content, timestamp, this.username, listID);
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    // Add stock to a stock list
    public boolean addStockToList(int listID, String symbol, int share) {
        // Check if the user owns the stock list
        StockList list = getStockList(listID);
        if (list != null && list.getCreator().equals(this.username)) {
            String sql = "INSERT INTO StockListItem (ListID, Symbol, Shares) VALUES (?, ?, ?)";

            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, listID);
                pstmt.setString(2, symbol);
                pstmt.setInt(3, share);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    // Update the stock list in the User object
                    list.addStock(new Stock(symbol));
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("You do not own this stock list and cannot add stocks to it.");
        }
        return false;
    }

    // Delete stock from a stock list
    public boolean deleteStockFromList(int listID, String symbol) {
        // Check if the user owns the stock list
        StockList list = getStockList(listID);
        if (list != null && list.getCreator().equals(this.username)) {
            String sql = "DELETE FROM StockListItem WHERE ListID = ? AND Symbol = ?";

            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, listID);
                pstmt.setString(2, symbol);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    // Update the stock list in the User object
                    list.getStocks().removeIf(stock -> stock.getSymbol().equals(symbol));
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("You do not own this stock list and cannot delete stocks from it.");
        }
        return false;
    }
}
