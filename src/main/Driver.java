package main;

import exceptions.*;
import objects.Alert;
import objects.Stock;
import objects.User;
import objects.Watchlist;
import sun.java2d.pipe.SpanShapeRenderer;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class Driver {
    private Connection connection;

    /******* Initialization *******/

    public Driver() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://localhost/stokieDatabase?autoReconnect=true&useSSL=false", "stokieDatabaseRemote", Passwords.MySQLPassword);
    }

    public void close() throws SQLException {
        connection.close();
    }


    /******* Identity Flow *******/

    public String createIdentity(String username, String password) throws SQLException, InvalidCredentialsException, FileNotFoundException {
        PreparedStatement userPS = connection.prepareStatement("select userID from User where username=? and password=?");
        userPS.setString(1, username);
        userPS.setBytes(2, Hasher.hash(password));
        ResultSet userRS = userPS.executeQuery();

        String userID = "";
        while(userRS.next()) userID = userRS.getString("userID");
        if(userID.equals("")) throw new InvalidCredentialsException();

        Random rd = new Random();
        byte[] b = new byte[256];
        rd.nextBytes(b);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new java.util.Date());
        calendar.add(Calendar.DATE, 30);
        String expiration = sdf.format(calendar.getTime());

        String identityToken = DatatypeConverter.printHexBinary(b);

        PreparedStatement ps = connection.prepareStatement("insert into Identity (identityToken, userID, expiration) values (?, ?, ?)");
        ps.setBytes(1, Hasher.hash(identityToken));
        ps.setString(2, userID);
        ps.setDate(3, new Date(calendar.getTime().getTime()));

        ps.executeUpdate();

        return identityToken;
    }

    public String createIdentity(String userID) throws SQLException, ClassNotFoundException, FileNotFoundException, InvalidIdentifierException {
        validateUserID(userID);

        Random rd = new Random();
        byte[] b = new byte[256];
        rd.nextBytes(b);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new java.util.Date());
        calendar.add(Calendar.DATE, 30);
        String expiration = sdf.format(calendar.getTime());

        String identityToken = DatatypeConverter.printHexBinary(b);

        PreparedStatement ps = connection.prepareStatement("insert into Identity (identityToken, userID, expiration) values (?, ?, ?)");
        ps.setBytes(1, Hasher.hash(identityToken));
        ps.setString(2, userID);
        ps.setDate(3, new Date(calendar.getTime().getTime()));

        ps.executeUpdate();

        return identityToken;
    }

    public String getUserIDForIdentityToken(String identityToken) throws SQLException, InvalidIdentifierException, FileNotFoundException, ExpiredIdentityException {
        PreparedStatement ps = connection.prepareStatement("select * from Identity where identityToken=?");
        ps.setBytes(1, Hasher.hash(identityToken));
        ResultSet rs = ps.executeQuery();

        Date sqlDate = null;
        String userID = "";
        while(rs.next()) {
            sqlDate = rs.getDate("expiration");
            userID = rs.getString("userID");
        }

        if(sqlDate == null || userID.equals("")) throw new InvalidIdentifierException("IdentityToken");
        java.util.Date date = new java.util.Date(sqlDate.getTime());
        if((new java.util.Date()).after(date)) {
            removeIdentity(identityToken);
            throw new ExpiredIdentityException();
        }

        return userID;
    }

    public void removeIdentity(String identityToken) throws SQLException, FileNotFoundException {
        PreparedStatement ps = connection.prepareStatement("delete from Identity where identityToken=?");
        ps.setBytes(1, Hasher.hash(identityToken));

        ps.executeUpdate();
    }

    public void removeAllIdentitiesForUserID(String userID) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("delete from Identity where userID=?");
        ps.setString(1, userID);

        ps.executeUpdate();
    }


    /******* User Flow *******/

    public void verifyUsernameDoesNotExist(String username) throws SQLException, ClassNotFoundException, DuplicateObjectException {
        PreparedStatement usernameQueryPS = connection.prepareStatement("select * from User where username=?");
        usernameQueryPS.setString(1, username);
        ResultSet usernameQueryRS = usernameQueryPS.executeQuery();
        while(usernameQueryRS.next() != false) throw new DuplicateObjectException("User");
    }

    public void validateUserID(String userID) throws SQLException, ClassNotFoundException, InvalidIdentifierException {
        PreparedStatement ps = connection.prepareStatement("select username from User where userID=?");
        ps.setString(1, userID);
        ResultSet rs = ps.executeQuery();

        int count = 0;
        while(rs.next()) count++;
        if(count == 0) throw new InvalidIdentifierException("User");
    }

    public void createUser(User user, String password) throws SQLException, FileNotFoundException, ClassNotFoundException, DuplicateObjectException, DuplicateIdentifierException {
        verifyUsernameDoesNotExist(user.getUsername());

        PreparedStatement userIDQueryPS = connection.prepareStatement("select * from User where userID=?");
        userIDQueryPS.setString(1, user.getUserID());
        ResultSet userIDQueryRS = userIDQueryPS.executeQuery();
        while(userIDQueryRS.next() != false) throw new DuplicateIdentifierException("UserID");

        PreparedStatement ps = connection.prepareStatement("insert into User (userId, username, password) values (?,?,?)");
        ps.setString(1, user.getUserID());
        ps.setString(2, user.getUsername());
        ps.setBytes(3, Hasher.hash(password));

        ps.executeUpdate();
    }

    public User getUser(String userID) throws SQLException, ClassNotFoundException, InvalidCredentialsException, InvalidIdentifierException {
        PreparedStatement ps = connection.prepareStatement("select * from User where userID=?");
        ps.setString(1, userID);
        ResultSet rs = ps.executeQuery();

        User user = null;

        while(rs.next()) {
            String username = rs.getString("username");
            ArrayList<Watchlist> watchlists = getWatchlistsForUserID(userID);

            user = new User(userID, username, watchlists);
        }

        if (user == null) throw new InvalidCredentialsException();

        return user;
    }


    /******* Device Flow *******/

    public void addDeviceToken(String userID, String deviceToken) throws SQLException, ClassNotFoundException, InvalidIdentifierException {
        validateUserID(userID);

        PreparedStatement ps = connection.prepareStatement("insert into Device (userID, deviceToken) values (?, ?)");
        ps.setString(1, userID);
        ps.setString(2, deviceToken);

        ps.executeUpdate();
    }

    public void removeDeviceToken(String userID, String deviceToken) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("delete from Device where userID=? and deviceToken=?");
        ps.setString(1, userID);
        ps.setString(2, deviceToken);

        ps.executeUpdate();
    }


    /******* Watchlist Flow *******/

    public void validateWatchlist(Watchlist watchlist) throws SQLException, ClassNotFoundException, InvalidIdentifierException {
        PreparedStatement ps = connection.prepareStatement("select * from Watchlist where watchlistID=?");
        ps.setString(1, watchlist.getWatchlistID());
        ResultSet rs = ps.executeQuery();

        int count = 0;
        while(rs.next()) count++;
        if(count == 0) throw new InvalidIdentifierException("Watchlist");
    }

    public void defaultWatchlistExistsForUser(User user) throws SQLException, DuplicateObjectException {
        PreparedStatement ps = connection.prepareStatement("select name from Watchlist where userID=? and isDefault=?");
        ps.setString(1, user.getUserID());
        ps.setInt(2, 1);
        ResultSet rs = ps.executeQuery();

        while(rs.next()) throw new DuplicateObjectException("Default Watchlist");
    }

    public void createWatchlist(Watchlist watchlist, User user) throws SQLException, ClassNotFoundException, InvalidIdentifierException, DuplicateIdentifierException {
        validateUserID(user.getUserID());

        if(watchlist.isDefault()) {
            PreparedStatement defaultWatchlistExistsPS = connection.prepareStatement("select * from Watchlist where userID=? and isDefault=?");
            defaultWatchlistExistsPS.setString(1, watchlist.getWatchlistID());
            defaultWatchlistExistsPS.setBoolean(2, true);

            ResultSet defaultWatchlistExistsRS = defaultWatchlistExistsPS.executeQuery();

            while(defaultWatchlistExistsRS.next()) throw new DuplicateIdentifierException("Default Watchlist");
        }

        PreparedStatement watchlistExistsPS = connection.prepareStatement("select * from Watchlist where watchlistID=?");
        watchlistExistsPS.setString(1, watchlist.getWatchlistID());
        ResultSet watchlistExistsRS = watchlistExistsPS.executeQuery();

        while(watchlistExistsRS.next()) throw new DuplicateIdentifierException("Watchlist");

        PreparedStatement ps = connection.prepareStatement("insert into Watchlist (watchlistID, name, userID, isDefault) values (?,?,?,?)");
        ps.setString(1, watchlist.getWatchlistID());
        ps.setString(2, watchlist.getName());
        ps.setString(3, user.getUserID());
        ps.setBoolean(4, watchlist.isDefault());

        ps.executeUpdate();
    }

    public ArrayList<Watchlist> getWatchlistsForUserID(String userID) throws SQLException, ClassNotFoundException, InvalidIdentifierException {
        PreparedStatement ps = connection.prepareStatement("select * from Watchlist where userID=?");
        ps.setString(1, userID);
        ResultSet rs = ps.executeQuery();

        ArrayList<Watchlist> watchlists = new ArrayList<Watchlist>();

        while(rs.next()) {
            String watchlistID = rs.getString("watchlistID");
            String name = rs.getString("name");
            boolean isDefault = rs.getBoolean("isDefault");
            ArrayList<Stock> stocks = getStocksForWatchlistID(watchlistID);

            watchlists.add(new Watchlist(watchlistID, name, stocks, isDefault));
        }

        return watchlists;
    }


    /******* Stock Flow *******/

    public void validateStockSymbol(String stockSymbol) throws SQLException, ClassNotFoundException, InvalidIdentifierException {
        PreparedStatement stockExistsPS = connection.prepareStatement("select * from Stock where symbol=?");
        stockExistsPS.setString(1, stockSymbol);
        ResultSet stockExistsRS = stockExistsPS.executeQuery();

        int count = 0;
        while(stockExistsRS.next()) count++;
        if(count == 0) throw new InvalidIdentifierException("StockID");
    }

    public boolean symbolExists(String symbol) throws SQLException, ClassNotFoundException {
        PreparedStatement stockExistsPS = connection.prepareStatement("select * from Stock where symbol=?");
        stockExistsPS.setString(1, symbol);
        ResultSet stockExistsRS = stockExistsPS.executeQuery();

        while(stockExistsRS.next()) return true;
        return false;
    }

    public void createStock(Stock stock) throws SQLException, ClassNotFoundException, DuplicateObjectException {
        PreparedStatement duplicateStockPS = connection.prepareStatement("select * from Stock where symbol=?");
        duplicateStockPS.setString(1, stock.getSymbol());
        ResultSet duplicateStockRS = duplicateStockPS.executeQuery();

        while(duplicateStockRS.next()) throw new DuplicateObjectException("Stock");

        PreparedStatement ps = connection.prepareStatement("insert into Stock (symbol, company, lastPrice) values (?,?,?)");
        ps.setString(1, stock.getSymbol());
        ps.setString(2, stock.getCompany());
        ps.setDouble(3, stock.getLastPrice());

        ps.executeUpdate();
    }

    public void updateStock(Stock stock) throws SQLException, ClassNotFoundException, InvalidIdentifierException {
        validateStockSymbol(stock.getSymbol());

        PreparedStatement ps = connection.prepareStatement("update Stock set company=?, lastPrice=? where symbol=?");
        ps.setString(1, stock.getCompany());
        ps.setDouble(2, stock.getLastPrice());
        ps.setString(3, stock.getSymbol());

        ps.executeUpdate();
    }

    public Stock getStock(String symbol) throws SQLException, ClassNotFoundException, InvalidIdentifierException {
        PreparedStatement ps = connection.prepareStatement("select * from Stock where symbol=?");
        ps.setString(1, symbol);
        ResultSet rs = ps.executeQuery();

        Stock stock = null;

        while(rs.next()) {
            String company = rs.getString("company");
            double lastPrice = rs.getDouble("lastPrice");

            stock = new Stock(symbol, company, lastPrice);
        }

        if(stock == null) throw new InvalidIdentifierException("Stock Symbol");

        return stock;
    }


    /******* WatchlistStock Flow *******/

    public void associateSymbolWithWatchlist(String symbol, Watchlist watchlist) throws SQLException, ClassNotFoundException, InvalidIdentifierException, DuplicateIdentifierException {
        validateWatchlist(watchlist);
        if(!symbolExists(symbol)) throw new InvalidIdentifierException("Stock Symbol");

        PreparedStatement duplicateExistsPS = connection.prepareStatement("select * from WatchlistStock where watchlistID=? and symbol=?");
        duplicateExistsPS.setString(1, watchlist.getWatchlistID());
        duplicateExistsPS.setString(2, symbol);
        ResultSet duplicateExistsRS = duplicateExistsPS.executeQuery();

        while(duplicateExistsRS.next()) throw new DuplicateIdentifierException("WatchlistID or Symbol (in AssociateStockWithWatchlist)");

        PreparedStatement ps = connection.prepareStatement("insert into WatchlistStock (watchlistID, symbol) values (?,?)");
        ps.setString(1, watchlist.getWatchlistID());
        ps.setString(2, symbol);

        ps.executeUpdate();
    }

    public void removeStockFromWatchlist(String stockSymbol, Watchlist watchlist) throws SQLException, ClassNotFoundException, InvalidIdentifierException {
        validateWatchlist(watchlist);

        PreparedStatement ps = connection.prepareStatement("delete from WatchlistStock where watchlistID=? and symbol=?");
        ps.setString(1, watchlist.getWatchlistID());
        ps.setString(2, stockSymbol);

        ps.executeUpdate();
    }

    public ArrayList<Stock> getStocksForWatchlistID(String watchlistID) throws SQLException, ClassNotFoundException, InvalidIdentifierException {
        PreparedStatement ps = connection.prepareStatement("select Stock.symbol, Stock.company, Stock.lastPrice from Stock, WatchlistStock where WatchlistStock.watchlistID=? and WatchlistStock.symbol=Stock.symbol");
        ps.setString(1, watchlistID);
        ResultSet rs = ps.executeQuery();

        ArrayList<Stock> stocks = new ArrayList<Stock>();

        while(rs.next()) {
            String symbol = rs.getString("symbol");
            String company = rs.getString("company");
            double lastPrice = rs.getDouble("lastPrice");

            stocks.add(new Stock(symbol, company, lastPrice));
        }

        return stocks;
    }


    /******* Alert Flow *******/

    public void createAlert(Alert alert) throws SQLException, ClassNotFoundException, InvalidIdentifierException {
        validateUserID(alert.getUserID());
        validateStockSymbol(alert.getSymbol());

        PreparedStatement ps = connection.prepareStatement("insert into Alert (alertID, symbol, userID, price, overPrice, executed) values (?, ?, ?, ?, ?, ?)");
        ps.setString(1, alert.getAlertID());
        ps.setString(2, alert.getSymbol());
        ps.setString(3, alert.getUserID());
        ps.setDouble(4, alert.getPrice());
        ps.setBoolean(5, alert.isOverPrice());
        ps.setBoolean(6, alert.isExecuted());

        ps.executeUpdate();
    }

    public ArrayList<Alert> getAlertForStockAndUser(Stock stock, User user) throws SQLException, ClassNotFoundException, InvalidIdentifierException {
        validateUserID(user.getUserID());
        validateStockSymbol(stock.getSymbol());

        PreparedStatement ps = connection.prepareStatement("select * from Alert where symbol=? and userID=?");
        ps.setString(1, stock.getSymbol());
        ps.setString(2, user.getUserID());
        ResultSet rs = ps.executeQuery();

        ArrayList<Alert> alerts = new ArrayList<>();

        while(rs.next()) {
            String alertID = rs.getString("alertID");
            String symbol = rs.getString("symbol");
            String userID = rs.getString("userID");
            double price = rs.getDouble("price");
            boolean overPrice = rs.getBoolean("overPrice");
            boolean executed = rs.getBoolean("executed");

            alerts.add(new Alert(alertID, symbol, userID, price, overPrice, executed));
        }

        return alerts;
    }

    public ArrayList<Alert> getAlerts(User user) throws SQLException, ClassNotFoundException, InvalidIdentifierException {
        validateUserID(user.getUserID());

        PreparedStatement ps = connection.prepareStatement("select * from Alert where userID=?");
        ps.setString(1, user.getUserID());
        ResultSet rs = ps.executeQuery();

        ArrayList<Alert> alerts = new ArrayList<>();

        while(rs.next()) {
            String alertID = rs.getString("alertID");
            String symbol = rs.getString("symbol");
            String userID = rs.getString("userID");
            double price = rs.getDouble("price");
            boolean overPrice = rs.getBoolean("overPrice");
            boolean executed = rs.getBoolean("executed");

            alerts.add(new Alert(alertID, symbol, userID, price, overPrice, executed));
        }

        return alerts;
    }

    public void removeAlert(Alert alert) throws SQLException, ClassNotFoundException, InvalidIdentifierException {
        PreparedStatement ps = connection.prepareStatement("delete from Alert where alertID=?");
        ps.setString(1, alert.getAlertID());

        ps.executeUpdate();
    }
}
