import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

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
        this.friendRequests = new ArrayList<>();
        this.friends = new ArrayList<>();
        this.stockLists = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    public static boolean register(String username, String password) {
        String sql = "INSERT INTO Users (username, password) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
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

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
    this.friends = loadFriends();
    this.friendRequests = loadFriendRequests();
    this.stockLists = loadStockLists();
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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private List<String> loadFriends() {
    List<String> friends = new ArrayList<>();
    String sql = "SELECT username2 FROM Friendship WHERE username1 = ?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, this.username);
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

    private List<FriendRequest> loadFriendRequests() {
        List<FriendRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM FriendRequest WHERE receiverusername = ? AND status = 'Pending'";
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String senderUsername = rs.getString("senderusername");
                    requests.add(new FriendRequest(senderUsername, this.username));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public boolean sendFriendRequest(String receiver) {
        String checkSql = "SELECT requesttime FROM FriendRequest WHERE senderusername = ? AND receiverusername = ? ORDER BY requesttime DESC LIMIT 1";
        String insertSql = "INSERT INTO FriendRequest (senderusername, receiverusername, status, requesttime) VALUES (?, ?, 'Pending', ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {

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
                    FriendRequest request = new FriendRequest(this.username, receiver);
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
                String sqlUpdateRequest = "UPDATE FriendRequest SET status = 'Accepted' WHERE senderusername = ? AND receiverusername = ?";
                String sqlInsertFriendship = "INSERT INTO Friendship (username1, username2) VALUES (?, ?), (?, ?)";

                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdateRequest);
                     PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsertFriendship)) {

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
                String sqlUpdateRequest = "UPDATE FriendRequest SET status = 'Declined' WHERE senderusername = ? AND receiverusername = ?";

                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sqlUpdateRequest)) {

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
            String sqlInsertFriendRequest = "INSERT INTO FriendRequest (senderusername, receiverusername, status, requesttime) VALUES (?, ?, 'Declined', ?)";

            try (Connection conn = DatabaseManager.getConnection();
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

    public void shareStockList(StockList list, User friend) {
        String sql = "INSERT INTO SharedStockList (username, listid) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, friend.getUsername());
            pstmt.setInt(2, list.getListID());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                friend.getStockLists().add(list);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public StockList createStockList(String name, boolean isPublic) {
        String sql = "INSERT INTO StockList (name, ispublic, creatorusername) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

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
                        return list;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
                    String creatorUsername = rs.getString("creatorusername");
                    StockList stockList = new StockList(listID, name, isPublic, creatorUsername);

                    if (isPublic || isStockListSharedWithUser(listID, this.username)) {
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
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM Review WHERE listid = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, listID);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int reviewID = rs.getInt("reviewid");
                    String content = rs.getString("content");
                    Date timestamp = new Date(rs.getTimestamp("timestamp").getTime());
                    String username = rs.getString("username");
                    Review review = new Review(reviewID, content, timestamp, username, listID);
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    private boolean isStockListSharedWithUser(int listID, String username) {
        String sql = "SELECT * FROM SharedStockList WHERE listid = ? AND username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

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
        String sql = "SELECT senderusername FROM FriendRequest WHERE receiverusername = ? AND status = 'Pending'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("You have no incoming friend requests.");
                    return;
                }
                System.out.println("\n--- Incoming Friend Requests ---");
                do {
                    String senderUsername = rs.getString("senderusername");
                    System.out.println(senderUsername + " has sent you a friend request.");
                } while (rs.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void viewOutgoingFriendRequests() {
        String sql = "SELECT receiverusername FROM FriendRequest WHERE senderusername = ? AND status = 'Pending'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("You have no outgoing friend requests.");
                    return;
                }
                System.out.println("\n--- Outgoing Friend Requests ---");
                do {
                    String receiverUsername = rs.getString("receiverusername");
                    System.out.println("You have sent a friend request to " + receiverUsername + ".");
                } while (rs.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteReview(int listID) {
        String sql = "DELETE FROM Review WHERE listid = ? AND username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    private List<StockList> loadStockLists() {
        List<StockList> stockLists = new ArrayList<>();
        String sql = "SELECT * FROM StockList WHERE creatorusername = ? OR listid IN (SELECT listid FROM SharedStockList WHERE username = ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, this.username);
            pstmt.setString(2, this.username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int listID = rs.getInt("listid");
                    String name = rs.getString("name");
                    boolean isPublic = rs.getBoolean("ispublic");
                    String creatorUsername = rs.getString("creatorusername");
                    StockList stockList = new StockList(listID, name, isPublic, creatorUsername);
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

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
}