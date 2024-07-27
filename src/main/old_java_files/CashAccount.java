package cs.toronto.edu;

public class CashAccount {

    public double cash;
    public Portfolio portfolio;

    public CashAccount(Portfolio portfolio) {
        this.cash = 0.0;
        this.portfolio = portfolio;
    }

    public double getCash() {
        return cash;
    }

    public void deposit(double amount) {
        cash += amount;
    }

    public void withdraw(double amount) {
        if (cash >= amount) {
            cash -= amount;
        } else {
            // idk what to do for insufficient funds
        }
    }
}
