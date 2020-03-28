package objects;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.Random;

public class Watchlist {
    private String watchlistID, name;
    private ArrayList<Stock> stocks;
    private boolean isDefault;

    public Watchlist(String name, boolean isDefault) {
        this.name = name;
        this.isDefault = isDefault;

        stocks = new ArrayList<Stock>();

        Random rd = new Random();
        byte[] b = new byte[256];
        rd.nextBytes(b);

        watchlistID = DatatypeConverter.printHexBinary(b);
    }

    public Watchlist(String watchlistID, String name, ArrayList<Stock> stocks, boolean isDefault) {
        this.watchlistID = watchlistID;
        this.stocks = stocks;
        this.name = name;
    }

    public String getWatchlistID() {
        return watchlistID;
    }

    public void setWatchlistID(String watchlistID) {
        this.watchlistID = watchlistID;
    }

    public ArrayList<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(ArrayList<Stock> stocks) {
        this.stocks = stocks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefault() {
        return isDefault;
    }
}
