package cs.toronto.edu;

import java.util.ArrayList;
import java.util.List;

public class User {

    public String username;
    public String password;
    public Portfolio portfolio;
    public List<User> friends;
    public List<StockList> sharedStockLists;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.portfolio = new Portfolio(this);
        this.friends = new ArrayList<>();
        this.sharedStockLists = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void sendFriendRequest(User recipient) {
        // Logic to send a friend request
    }

    public void acceptFriendRequest(User sender) {
        // Logic to accept a friend request
    }

    public void removeFriend(User friend) {
        // Logic to remove a friend
    }

    public List<User> getFriends() {
        return friends;
    }

    public void shareStockListWithFriend(StockList stockList, User friend) {
        // Logic to share a stock list with a friend
        sharedStockLists.add(stockList);
    }
}
