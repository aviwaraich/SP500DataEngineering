package cs.toronto.edu;
import cs.toronto.edu.StockList; 

import java.util.ArrayList;
import java.util.List;

// Stock Holding Entity
public class StockHolding {    
    public String symbol;
    public int quantity;
    public Portfolio portfolio;
    public StockList stockList;

    public StockHolding(String symbol, int quantity, Portfolio portfolio, StockList stockList) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.portfolio = portfolio;
        this.stockList = stockList;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }
}