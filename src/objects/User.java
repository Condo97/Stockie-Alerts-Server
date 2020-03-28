package objects;

import exceptions.InvalidCredentialsException;
import exceptions.InvalidIdentifierException;
import main.Driver;

import javax.xml.bind.DatatypeConverter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

public class User {
    private String username, userID;
    private ArrayList<Watchlist> watchlists;

    public User(String username) {
        this.username = username;

        watchlists = new ArrayList<Watchlist>();

        Random rd = new Random();
        byte[] b = new byte[256];
        rd.nextBytes(b);

        userID = DatatypeConverter.printHexBinary(b);
    }

    public User(String userID, String username) {
        this.userID = userID;
        this.username = username;

        watchlists = new ArrayList<Watchlist>();
    }

    public User(String userID, String username, ArrayList<Watchlist> watchlists) {
        this.username = username;
        this.userID = userID;
        this.watchlists = watchlists;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public ArrayList<Watchlist> getWatchlists() {
        return watchlists;
    }

    public void setWatchlists(ArrayList<Watchlist> watchlists) {
        this.watchlists = watchlists;
    }
}
