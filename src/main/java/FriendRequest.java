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

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void accept() {
        this.status = "Accepted";
    }

    public void decline() {
        this.status = "Declined";
    }
}
