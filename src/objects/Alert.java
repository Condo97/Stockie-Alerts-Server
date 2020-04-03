package objects;

import javax.xml.bind.DatatypeConverter;
import java.util.Random;

public class Alert {
    private String symbol, userID, alertID;
    private double price;
    private boolean overPrice, executed;

    public Alert(String symbol, String userID, double price, boolean overPrice, boolean executed) {
        this.symbol = symbol;
        this.userID = userID;
        this.price = price;
        this.overPrice = overPrice;

        Random rd = new Random();
        byte[] b = new byte[256];
        rd.nextBytes(b);

        alertID = DatatypeConverter.printHexBinary(b);
    }

    public Alert(String alertID, String symbol, String userID, double price, boolean overPrice, boolean executed) {
        this.alertID = alertID;
        this.symbol = symbol;
        this.userID = userID;
        this.price = price;
        this.overPrice = overPrice;
        this.executed = executed;
    }

    public String getAlertID() {
        return alertID;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getUserID() {
        return userID;
    }

    public double getPrice() {
        return price;
    }

    public boolean isOverPrice() {
        return overPrice;
    }

    public boolean isExecuted() {
        return executed;
    }
}
