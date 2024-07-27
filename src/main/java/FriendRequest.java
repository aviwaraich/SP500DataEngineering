import java.util.Date;

public class FriendRequest {
    private String sender;
    private String receiver;
    private Date requestTime;
    private String status;

    public FriendRequest(String sender, String receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.requestTime = new Date();
        this.status = "Pending";
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public String getStatus() {
        return status;
    }

    public void accept() {
        this.status = "Accepted";
    }

    public void decline() {
        this.status = "Declined";
    }
}
