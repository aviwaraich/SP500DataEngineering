package cs.toronto.edu;

public class Portfolio {
    public User owner;
    public CashAccount cashAccount;

    public Portfolio(User owner) {
        this.owner = owner;
        this.cashAccount = new CashAccount(this);
    }

    public User getOwner() {
        return owner;
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }
}