package main;

import com.sun.net.httpserver.*;
import exceptions.*;
import objects.Alert;
import objects.Stock;
import objects.User;
import objects.Watchlist;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import yahoofinance.YahooFinance;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.security.*;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class Main {
    public static int getErrorNumber(Exception e) {
        if(e instanceof MissingKeyException)
            return 40;
        if(e instanceof InvalidValueException)
            return 41;
        if(e instanceof InvalidCredentialsException)
            return 50;
        if(e instanceof InvalidIdentifierException)
            return 51;
        if(e instanceof DuplicateObjectException)
            return 53;
        if(e instanceof DuplicateIdentifierException)
            return 54;
        if(e instanceof AssociationException)
            return 55;
        if(e instanceof DateTimeParseException)
            return 56;
        if(e instanceof ExpiredIdentityException)
            return 57;
        if(e instanceof SQLException) {
            e.printStackTrace();
            return 59;
        }

        e.printStackTrace();
        return 1;
    }

    public static String extractString(JSONObject json, String key) throws InvalidValueException, MissingKeyException {
        if(!json.containsKey(key)) throw new MissingKeyException(key);
        if(!(json.get(key) instanceof String)) throw new InvalidValueException(key);

        return (String)json.get(key);
    }

    public static Long extractLong(JSONObject json, String key) throws InvalidValueException, MissingKeyException {
        if(!json.containsKey(key)) throw new MissingKeyException(key);
        if(!(json.get(key) instanceof Long)) throw new InvalidValueException(key);

        return (Long)json.get(key);
    }

    public static Double extractDouble(JSONObject json, String key) throws InvalidValueException, MissingKeyException {
        if(!json.containsKey(key)) throw new MissingKeyException(key);
        if((json.get(key) instanceof Double)) return (Double)json.get(key);
        if((json.get(key) instanceof Long)) return new Double((Long)json.get(key));
        throw new InvalidValueException(key);
    }

    public static Boolean extractBoolean(JSONObject json, String key) throws InvalidValueException, MissingKeyException {
        if(!json.containsKey(key)) throw new MissingKeyException(key);
        if((json.get(key) instanceof Boolean)) return (Boolean)json.get(key);
        throw new InvalidValueException(key);
    }

    public static JSONArray extractJSONArray(JSONObject json, String key) throws InvalidValueException, MissingKeyException {
        if(!json.containsKey(key)) throw new MissingKeyException(key);
        if(!(json.get(key) instanceof JSONArray)) throw new InvalidValueException(key);

        return (JSONArray)json.get(key);
    }

    public static JSONObject extractJSONObject(JSONObject json, String key) throws InvalidValueException, MissingKeyException {
        if(!json.containsKey(key)) throw new MissingKeyException(key);
        if(!(json.get(key) instanceof JSONObject)) throw new InvalidValueException(key);

        return (JSONObject)json.get(key);
    }


    /**
     * CreateUser
     * @author alexcoundouriotis
     *
     * Functionality:
     * - Creates a User in the database and returns an Authentication token
     *
     * Parameters:
     * (none)
     *
     * POST JSON Contents:
     * "Username": User's username
     * "Password": User's password
     *
     * Response JSON Contents:
     * "IdentityToken": Identity Token to store as "cookie" on Customer's device
     * "Error": Integer indicating the error, 0 if success
     *
     *
     */
    public static class CreateUser implements HttpHandler {
        @Override
        public void handle(HttpExchange httpsExchange) throws IOException {
            System.out.println("adsfasdfasd");
            HttpsExchange t = (HttpsExchange) httpsExchange;
            JSONObject responseJSON = new JSONObject();

            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONParser parser = new JSONParser();
            try {
                Driver dr = new Driver();
                JSONObject json = (JSONObject) parser.parse(sb.toString());

                String username = extractString(json, "Username");
                String password = extractString(json, "Password");

                User user = new User(username);
                dr.createUser(user, password);
                String identityToken = dr.createIdentity(username, password);

                responseJSON.put("Error", 0);
                responseJSON.put("IdentityToken", identityToken);

                dr.close();
            } catch (DuplicateIdentifierException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (DuplicateObjectException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidCredentialsException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidValueException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (MissingKeyException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (SQLException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ParseException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            }

            isr.close();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseJSON.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(responseJSON.toString().getBytes());
            os.close();
        }
    }


    /**
     * CheckUser
     * @author alexcoundouriotis
     *
     * Functionality:
     * - Creates a User in the database and returns an Authentication token
     *
     * Parameters:
     * (none)
     *
     * POST JSON Contents:
     * "Username": User's username
     *
     * Response JSON Contents:
     * "Error": Integer indicating the error, 0 if success
     *
     *
     */
    public static class CheckUser implements HttpHandler {
        @Override
        public void handle(HttpExchange httpsExchange) throws IOException {
            HttpsExchange t = (HttpsExchange) httpsExchange;
            JSONObject responseJSON = new JSONObject();

            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONParser parser = new JSONParser();
            try {
                Driver dr = new Driver();
                JSONObject json = (JSONObject) parser.parse(sb.toString());

                String username = extractString(json, "Username");
                dr.verifyUsernameDoesNotExist(username);

                responseJSON.put("Error", 0);

                dr.close();
            } catch (DuplicateObjectException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidValueException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (MissingKeyException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (SQLException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ParseException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            }
            isr.close();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseJSON.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(responseJSON.toString().getBytes());
            os.close();
        }
    }


    /**
     * UserLogin
     * @author alexcoundouriotis
     *
     * Functionality:
     * - Logs in a User in the database and returns an Authentication token
     *
     * Parameters:
     * (none)
     *
     * POST JSON Contents:
     * "Username": User's username
     * "Password": User's password
     *
     * Response JSON Contents:
     * "IdentityToken": Identity token to store as "cookie" on Customer's device
     * "Error": Integer indicating the error, 0 if success
     *
     *
     */
    public static class UserLogin implements HttpHandler {
        @Override
        public void handle(HttpExchange httpsExchange) throws IOException {
            HttpsExchange t = (HttpsExchange) httpsExchange;
            JSONObject responseJSON = new JSONObject();

            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONParser parser = new JSONParser();
            try {
                Driver dr = new Driver();
                JSONObject json = (JSONObject) parser.parse(sb.toString());

                String username = extractString(json, "Username");
                String password = extractString(json, "Password");
                String identityToken = dr.createIdentity(username, password);

                responseJSON.put("Error", 0);
                responseJSON.put("IdentityToken", identityToken);

                dr.close();
            } catch (InvalidCredentialsException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidValueException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (MissingKeyException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (SQLException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ParseException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            }
            isr.close();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseJSON.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(responseJSON.toString().getBytes());
            os.close();
        }
    }


    /**
     * UserLogout
     * @author alexcoundouriotis
     *
     * Functionality:
     * - Creates a User in the database and returns an Authentication token
     *
     * Parameters:
     * (none)
     *
     * POST JSON Contents:
     * "IdentityToken": User's Identity Token
     *
     * Response JSON Contents:
     * "Error": Integer indicating the error, 0 if success
     *
     *
     */
    public static class UserLogout implements HttpHandler {
        @Override
        public void handle(HttpExchange httpsExchange) throws IOException {
            HttpsExchange t = (HttpsExchange) httpsExchange;
            JSONObject responseJSON = new JSONObject();

            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONParser parser = new JSONParser();
            try {
                Driver dr = new Driver();
                JSONObject json = (JSONObject) parser.parse(sb.toString());

                String identityToken = extractString(json, "IdentityToken");
                dr.removeIdentity(identityToken);

                responseJSON.put("Error", 0);
                responseJSON.put("IdentityToken", identityToken);

                dr.close();
            } catch (InvalidValueException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (MissingKeyException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (SQLException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ParseException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            }
            isr.close();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseJSON.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(responseJSON.toString().getBytes());
            os.close();
        }
    }

    /**
     * AddDeviceToken
     * @author alexcoundouriotis
     *
     * Functionality:
     * - Creates a User in the database and returns an Authentication token
     *
     * Parameters:
     * (none)
     *
     * POST JSON Contents:
     * "IdentityToken": User's Identity Token
     * "DeviceToken": User's Device's Token
     *
     * Response JSON Contents:
     * "Error": Integer indicating the error, 0 if success
     *
     *
     */
    public static class AddDeviceToken implements HttpHandler {
        @Override
        public void handle(HttpExchange httpsExchange) throws IOException {
            HttpsExchange t = (HttpsExchange) httpsExchange;
            JSONObject responseJSON = new JSONObject();

            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONParser parser = new JSONParser();
            try {
                Driver dr = new Driver();
                JSONObject json = (JSONObject) parser.parse(sb.toString());

                String identityToken = extractString(json, "IdentityToken");
                String deviceToken = extractString(json, "DeviceToken");

                User user = dr.getUser(dr.getUserIDForIdentityToken(identityToken));

                dr.addDeviceToken(user.getUserID(), deviceToken);

                dr.close();

                responseJSON.put("Error", 0);
            } catch (ExpiredIdentityException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidCredentialsException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidIdentifierException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidValueException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (MissingKeyException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (SQLException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ParseException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            }

            isr.close();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseJSON.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(responseJSON.toString().getBytes());
            os.close();
        }
    }


    /**
     * RemoveDeviceToken
     * @author alexcoundouriotis
     *
     * Functionality:
     * - Creates a User in the database and returns an Authentication token
     *
     * Parameters:
     * (none)
     *
     * POST JSON Contents:
     * "IdentityToken": User's Authentication Token
     * "DeviceToken": User's Device's Token
     *
     * Response JSON Contents:
     * "Error": Integer indicating the error, 0 if success
     *
     *
     */
    public static class RemoveDeviceToken implements HttpHandler {
        @Override
        public void handle(HttpExchange httpsExchange) throws IOException {
            HttpsExchange t = (HttpsExchange) httpsExchange;
            JSONObject responseJSON = new JSONObject();

            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONParser parser = new JSONParser();
            try {
                Driver dr = new Driver();
                JSONObject json = (JSONObject) parser.parse(sb.toString());

                String identityToken = extractString(json, "IdentityToken");
                String deviceToken = extractString(json, "DeviceToken");

                User user = dr.getUser(dr.getUserIDForIdentityToken(identityToken));

                dr.removeDeviceToken(user.getUserID(), deviceToken);

                dr.close();

                responseJSON.put("Error", 0);
            } catch (ExpiredIdentityException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidCredentialsException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidIdentifierException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidValueException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (MissingKeyException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (SQLException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ParseException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            }
            isr.close();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseJSON.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(responseJSON.toString().getBytes());
            os.close();
        }
    }


    /**
     * GetWatchlists
     * @author alexcoundouriotis
     *
     * Functionality:
     * - Creates a User in the database and returns an Authentication token
     *
     * Parameters:
     * (none)
     *
     * POST JSON Contents:
     * "IdentityToken": User's IdentityToken
     *
     * Response JSON Contents:
     * "Watchlists": Array of watchlists for User associated with Identity associated with IdentityToken
     *   "WatchlistName": Name of the Watchlist
     *   "WatchlistID": Unique identifier of the Watchlist
     *   "IsDefault": Boolean value indicating if the Watchlist is the default Watchlist
     *   "Stocks": Array of stocks in Watchlist
     *     "Symbol": Symbol of the stock, Primary Key
     *     "Company": Company that the Stock symbol represents
     *     "LastPrice": The most recent price of the Stock
     * "Error": Integer indicating the error, 0 if success
     *
     *
     */
    public static class GetWatchlists implements HttpHandler {
        @Override
        public void handle(HttpExchange httpsExchange) throws IOException {
            HttpsExchange t = (HttpsExchange) httpsExchange;
            JSONObject responseJSON = new JSONObject();

            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONParser parser = new JSONParser();
            try {
                Driver dr = new Driver();
                JSONObject json = (JSONObject) parser.parse(sb.toString());

                String identityToken = extractString(json, "IdentityToken");

                User user = dr.getUser(dr.getUserIDForIdentityToken(identityToken));

                JSONArray watchlistArray = new JSONArray();
                for (Watchlist watchlist : user.getWatchlists()) {
                    JSONObject watchlistObject = new JSONObject();
                    JSONArray stockArray = new JSONArray();

                    for (Stock stock : watchlist.getStocks()) {
                        JSONObject stockObject = new JSONObject();

                        stockObject.put("Symbol", stock.getSymbol());
                        stockObject.put("Company", stock.getCompany());
                        stockObject.put("LastPrice", stock.getLastPrice());

                        stockArray.add(stockObject);
                    }

                    watchlistObject.put("Stocks", stockArray);
                    watchlistObject.put("WatchlistName", watchlist.getName());
                    watchlistObject.put("WatchlistID", watchlist.getWatchlistID());
                    watchlistObject.put("IsDefault", watchlist.isDefault());
                    watchlistArray.add(watchlistObject);
                }

                dr.close();

                responseJSON.put("Watchlists", watchlistArray);
                responseJSON.put("Error", 0);

            } catch (ExpiredIdentityException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidCredentialsException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidIdentifierException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidValueException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (MissingKeyException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (SQLException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ParseException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            }
            isr.close();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseJSON.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(responseJSON.toString().getBytes());
            os.close();
        }
    }


    /**
     * AddWatchlist
     * @author alexcoundouriotis
     *
     * Functionality:
     * - Creates a User in the database and returns an Authentication token
     *
     * Parameters:
     * (none)
     *
     * POST JSON Contents:
     * "IdentityToken": User's IdentityToken
     * "WatchlistName": Watchlist's Name
     * "IsDefault": Boolean value indicating if the Watchlist is the default Watchlist
     *
     * Response JSON Contents:
     * "WatchlistName": Watchlist's Name
     * "WatchlistID": Watchlist's ID
     * "Error": Integer indicating the error, 0 if success
     *
     *
     */
    public static class AddWatchlist implements HttpHandler {
        @Override
        public void handle(HttpExchange httpsExchange) throws IOException {
            HttpsExchange t = (HttpsExchange) httpsExchange;
            JSONObject responseJSON = new JSONObject();

            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONParser parser = new JSONParser();
            try {
                Driver dr = new Driver();
                JSONObject json = (JSONObject) parser.parse(sb.toString());

                String identityToken = extractString(json, "IdentityToken");
                String watchlistName = extractString(json, "WatchlistName");
                boolean isDefault = extractBoolean(json, "IsDefault");

                User user = dr.getUser(dr.getUserIDForIdentityToken(identityToken));

                if (isDefault) dr.defaultWatchlistExistsForUser(user);

                Watchlist watchlist = new Watchlist(watchlistName, isDefault);

                dr.createWatchlist(watchlist, user);

                dr.close();

                responseJSON.put("WatchlistName", watchlist.getName());
                responseJSON.put("WatchlistID", watchlist.getWatchlistID());
                responseJSON.put("Error", 0);

            } catch (DuplicateObjectException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (DuplicateIdentifierException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ExpiredIdentityException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidCredentialsException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidIdentifierException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidValueException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (MissingKeyException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (SQLException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ParseException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            }

            isr.close();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseJSON.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(responseJSON.toString().getBytes());
            os.close();
        }
    }

    /**
     * AddStockToWatchlist
     * @author alexcoundouriotis
     *
     * Functionality:
     * - Creates a User in the database and returns an Authentication token
     *
     * Parameters:
     * (none)
     *
     * POST JSON Contents:
     * "IdentityToken": User's IdentityToken
     * "WatchlistID": Watchlist's ID
     * "StockSymbol": The Symbol representing the Stock
     *
     * Response JSON Contents:
     * "Error": Integer indicating the error, 0 if success
     *
     *
     */
    public static class AddStockToWatchlist implements HttpHandler {
        @Override
        public void handle(HttpExchange httpsExchange) throws IOException {
            HttpsExchange t = (HttpsExchange) httpsExchange;
            JSONObject responseJSON = new JSONObject();

            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONParser parser = new JSONParser();
            try {
                Driver dr = new Driver();
                JSONObject json = (JSONObject) parser.parse(sb.toString());

                String identityToken = extractString(json, "IdentityToken");
                String watchlistID = extractString(json, "WatchlistID");
                String stockSymbol = extractString(json, "StockSymbol").toUpperCase();

                User user = dr.getUser(dr.getUserIDForIdentityToken(identityToken));
                Watchlist watchlist = null;

                for (Watchlist userWatchlist : user.getWatchlists())
                    if (userWatchlist.getWatchlistID().equals(watchlistID)) watchlist = userWatchlist;

                if (watchlist == null) throw new InvalidIdentifierException("Watchlist ID");

                if (dr.symbolExists(stockSymbol)) dr.associateSymbolWithWatchlist(stockSymbol, watchlist);
                else {
                    yahoofinance.Stock yahoofinanceStock = YahooFinance.get(stockSymbol);
                    if (yahoofinanceStock == null) throw new InvalidIdentifierException("Stock Symbol");

                    double price = yahoofinanceStock.getQuote().getPrice().doubleValue();
                    String company = yahoofinanceStock.getName();

                    Stock stock = new Stock(stockSymbol, company, price);

                    dr.createStock(stock);
                    dr.associateSymbolWithWatchlist(stock.getSymbol(), watchlist);
                }

                dr.close();

                responseJSON.put("Error", 0);

            } catch (DuplicateIdentifierException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (DuplicateObjectException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ExpiredIdentityException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidCredentialsException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidIdentifierException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidValueException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (MissingKeyException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (IOException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (SQLException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ParseException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            }
            isr.close();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseJSON.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(responseJSON.toString().getBytes());
            os.close();
        }
    }

    /**
     * RemoveStockFromWatchlist
     * @author alexcoundouriotis
     *
     * Functionality:
     * - Creates a User in the database and returns an Authentication token
     *
     * Parameters:
     * (none)
     *
     * POST JSON Contents:
     * "IdentityToken": User's IdentityToken
     * "WatchlistID": Watchlist's ID
     * "StockSymbol": The Symbol representing the Stock
     *
     * Response JSON Contents:
     * "Error": Integer indicating the error, 0 if success
     *
     *
     */
    public static class RemoveStockFromWatchlist implements HttpHandler {
        @Override
        public void handle(HttpExchange httpsExchange) throws IOException {
            HttpsExchange t = (HttpsExchange) httpsExchange;
            JSONObject responseJSON = new JSONObject();

            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONParser parser = new JSONParser();
            try {
                Driver dr = new Driver();
                JSONObject json = (JSONObject) parser.parse(sb.toString());

                String identityToken = extractString(json, "IdentityToken");
                String watchlistID = extractString(json, "WatchlistID");
                String stockSymbol = extractString(json, "StockSymbol").toUpperCase();

                User user = dr.getUser(dr.getUserIDForIdentityToken(identityToken));
                Watchlist watchlist = null;

                for (Watchlist userWatchlist : user.getWatchlists())
                    if (userWatchlist.getWatchlistID().equals(watchlistID)) watchlist = userWatchlist;

                if (watchlist == null) throw new InvalidIdentifierException("Watchlist ID");

                dr.removeStockFromWatchlist(stockSymbol, watchlist);

                dr.close();

                responseJSON.put("Error", 0);

            } catch (ExpiredIdentityException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidCredentialsException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidIdentifierException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidValueException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (MissingKeyException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (SQLException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ParseException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            }
            isr.close();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseJSON.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(responseJSON.toString().getBytes());
            os.close();
        }
    }

    /**
     * GetStock
     * @author alexcoundouriotis
     *
     * Functionality:
     * - Creates a User in the database and returns an Authentication token
     *
     * Parameters:
     * (none)
     *
     * POST JSON Contents:
     * "IdentityToken": User's IdentityToken
     * "StockSymbol": The Symbol representing the Stock
     *
     * Response JSON Contents:
     * "StockSymbol": The Symbol representing the Stock
     * "Company": The Company that the Stock represents
     * "LastPrice": The last received Price of the Stock
     * "Error": Integer indicating the error, 0 if success
     *
     *
     */
    public static class GetStock implements HttpHandler {
        @Override
        public void handle(HttpExchange httpsExchange) throws IOException {
            HttpsExchange t = (HttpsExchange) httpsExchange;
            JSONObject responseJSON = new JSONObject();

            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONParser parser = new JSONParser();
            try {
                Driver dr = new Driver();
                JSONObject json = (JSONObject) parser.parse(sb.toString());

                String identityToken = extractString(json, "IdentityToken");
                String stockSymbol = extractString(json, "StockSymbol").toUpperCase();
                Stock stock = null;

                if (dr.getUser(dr.getUserIDForIdentityToken(identityToken)) == null)
                    throw new InvalidCredentialsException();

                if (dr.symbolExists(stockSymbol)) stock = dr.getStock(stockSymbol);
                else {
                    yahoofinance.Stock yahoofinanceStock = YahooFinance.get(stockSymbol);
                    if (yahoofinanceStock == null) throw new InvalidIdentifierException("Stock Symbol");

                    double price = yahoofinanceStock.getQuote().getPrice().doubleValue();
                    String company = yahoofinanceStock.getName();

                    stock = new Stock(stockSymbol, company, price);

                    dr.createStock(stock);
                }

                dr.close();

                responseJSON.put("StockSymbol", stock.getSymbol());
                responseJSON.put("Company", stock.getCompany());
                responseJSON.put("LastPrice", stock.getLastPrice());
                responseJSON.put("Error", 0);
            } catch (DuplicateObjectException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ExpiredIdentityException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidCredentialsException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidIdentifierException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidValueException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (MissingKeyException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (SQLException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ParseException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            }
            isr.close();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseJSON.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(responseJSON.toString().getBytes());
            os.close();
        }
    }

    /**
     * GetStocks
     * @author alexcoundouriotis
     *
     * Functionality:
     * - Creates a User in the database and returns an Authentication token
     *
     * Parameters:
     * (none)
     *
     * POST JSON Contents:
     * "IdentityToken": User's IdentityToken
     * "StockSymbols": An array of symbols as Strings representing the Stocks to get
     *
     * Response JSON Contents:
     * "Stocks": The Stocks to send to the User
     *  "StockSymbol": The Symbol representing the Stock
     *  "Company": The Company the Stock represents
     *  "LastPrice": The last received Price of the Stock
     * "Error": Integer indicating the error, 0 if success
     *
     *
     */
    public static class GetStocks implements HttpHandler {
        @Override
        public void handle(HttpExchange httpsExchange) throws IOException {
            HttpsExchange t = (HttpsExchange) httpsExchange;
            JSONObject responseJSON = new JSONObject();

            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONParser parser = new JSONParser();
            try {
                Driver dr = new Driver();
                JSONObject json = (JSONObject) parser.parse(sb.toString());

                String identityToken = extractString(json, "IdentityToken");
                JSONArray stockSymbols = extractJSONArray(json, "StockSymbols");

                JSONArray stocksJSONArray = new JSONArray();

                for(int i = 0; i < stockSymbols.size(); i++) {
                    String stockSymbol = ((String)stockSymbols.get(i)).toUpperCase();
                    Stock stock = null;

                    if (dr.getUser(dr.getUserIDForIdentityToken(identityToken)) == null)
                        throw new InvalidCredentialsException();

                    if (dr.symbolExists(stockSymbol)) stock = dr.getStock(stockSymbol);
                    else {
                        yahoofinance.Stock yahoofinanceStock = YahooFinance.get(stockSymbol);
                        if (yahoofinanceStock == null) throw new InvalidIdentifierException("Stock Symbol");

                        double price = yahoofinanceStock.getQuote().getPrice().doubleValue();
                        String company = yahoofinanceStock.getName();

                        stock = new Stock(stockSymbol, company, price);

                        dr.createStock(stock);
                    }

                    JSONObject stockJSON = new JSONObject();
                    stockJSON.put("StockSymbol", stock.getSymbol());
                    stockJSON.put("Company", stock.getCompany());
                    stockJSON.put("LastPrice", stock.getLastPrice());

                    stocksJSONArray.add(stockJSON);
                }

                dr.close();

                responseJSON.put("Stocks", stocksJSONArray);
                responseJSON.put("Error", 0);
            } catch (DuplicateObjectException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ExpiredIdentityException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidCredentialsException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidIdentifierException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidValueException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (MissingKeyException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (SQLException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ParseException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            }
            isr.close();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseJSON.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(responseJSON.toString().getBytes());
            os.close();
        }
    }


    /**
     * AddAlert
     * @author alexcoundouriotis
     *
     * Functionality:
     * - Creates a User in the database and returns an Authentication token
     *
     * Parameters:
     * (none)
     *
     * POST JSON Contents:
     * "IdentityToken": User's IdentityToken
     * "StockSymbol": The Symbol representing the Stock
     * "Price": The Price of the Alert
     *
     * Response JSON Contents:
     * "Error": Integer indicating the error, 0 if success
     *
     *
     */
    public static class AddAlert implements HttpHandler {
        @Override
        public void handle(HttpExchange httpsExchange) throws IOException {
            HttpsExchange t = (HttpsExchange) httpsExchange;
            JSONObject responseJSON = new JSONObject();

            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONParser parser = new JSONParser();
            try {
                Driver dr = new Driver();
                JSONObject json = (JSONObject) parser.parse(sb.toString());

                String identityToken = extractString(json, "IdentityToken");
                String stockSymbol = extractString(json, "StockSymbol").toUpperCase();
                double price = extractDouble(json, "Price");

                User user = dr.getUser(dr.getUserIDForIdentityToken(identityToken));
                if (!dr.symbolExists(stockSymbol)) {
                    yahoofinance.Stock yahoofinanceStock = YahooFinance.get(stockSymbol);
                    if (yahoofinanceStock == null) throw new InvalidIdentifierException("Stock Symbol");

                    double stockPrice = yahoofinanceStock.getQuote().getPrice().doubleValue();
                    String company = yahoofinanceStock.getName();

                    Stock stock = new Stock(stockSymbol, company, stockPrice);

                    dr.createStock(stock);
                }

                double currentPrice = dr.getStock(stockSymbol).getLastPrice();
                boolean overPrice = price > currentPrice ? true : false;

                Alert alert = new Alert(stockSymbol, user.getUserID(), price, overPrice, false);
                dr.createAlert(alert);

                dr.close();

                responseJSON.put("Error", 0);

            } catch (DuplicateObjectException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ExpiredIdentityException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidCredentialsException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidIdentifierException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidValueException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (MissingKeyException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (IOException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (SQLException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ParseException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            }
            isr.close();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseJSON.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(responseJSON.toString().getBytes());
            os.close();
        }
    }


    /**
     * ModifyAlert
     * @author alexcoundouriotis
     *
     * Functionality:
     * - Creates a User in the database and returns an Authentication token
     *
     * Parameters:
     * (none)
     *
     * POST JSON Contents:
     * "IdentityToken": User's IdentityToken
     * "AlertID": The unique identifier representing the Alert
     * "Price": The updated Price of the Alert
     * "Executed": The Boolean value that denotes whether the Alert has been executed or not
     *
     * Response JSON Contents:
     * "AlertID": The unique identifier of the Alert
     * "Symbol": The symbol the Alert is acting on
     * "Price": The price threshold in which the alert activates
     * "OverPrice": A Boolean value that denotes whether the Price set was over the current stock price or not
     * "Executed": A Boolean value that denotes whether the Alert has been executed or not
     *
     *
     */
    public static class ModifyAlert implements HttpHandler {
        @Override
        public void handle(HttpExchange httpsExchange) throws IOException {
            HttpsExchange t = (HttpsExchange) httpsExchange;
            JSONObject responseJSON = new JSONObject();

            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONParser parser = new JSONParser();
            try {
                Driver dr = new Driver();
                JSONObject json = (JSONObject) parser.parse(sb.toString());

                String identityToken = extractString(json, "IdentityToken");
                String alertID = extractString(json, "AlertID");
                double price = extractDouble(json, "Price");
                boolean executed = extractBoolean(json, "Executed");

                dr.updateAlert(dr.getUserIDForIdentityToken(identityToken), alertID, price, executed);
                Alert alert = dr.getAlert(alertID);

                responseJSON.put("AlertID", alertID);
                responseJSON.put("Symbol", alert.getSymbol());
                responseJSON.put("Price", alert.getPrice());
                responseJSON.put("OverPrice", alert.isOverPrice());
                responseJSON.put("Executed", alert.isExecuted());
                responseJSON.put("Error", 0);

            } catch (ExpiredIdentityException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidIdentifierException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidValueException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (MissingKeyException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (IOException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (SQLException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ParseException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            }
            isr.close();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseJSON.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(responseJSON.toString().getBytes());
            os.close();
        }
    }


    /**
     * GetAlerts
     * @author alexcoundouriotis
     *
     * Functionality:
     * - Creates a User in the database and returns an Authentication token
     *
     * Parameters:
     * (none)
     *
     * POST JSON Contents:
     * "IdentityToken": User's IdentityToken
     *
     * Response JSON Contents:
     * "Alerts": JSON Array consisting of Alert objects
     *  "AlertID": The unique identifier of the Alert
     *  "Symbol": The symbol the Alert is acting on
     *  "Price": The price threshold in which the alert activates
     *  "OverPrice": A Boolean value that denotes whether the Price set was over the current stock price or not
     *  "Executed": A Boolean value that denotes whether the Alert has been executed or not
     * "Error": Integer indicating the error, 0 if success
     *
     */
    public static class GetAlerts implements HttpHandler {
        @Override
        public void handle(HttpExchange httpsExchange) throws IOException {
            HttpsExchange t = (HttpsExchange) httpsExchange;
            JSONObject responseJSON = new JSONObject();

            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONParser parser = new JSONParser();
            try {
                Driver dr = new Driver();
                JSONObject json = (JSONObject) parser.parse(sb.toString());

                String identityToken = extractString(json, "IdentityToken");

                User user = dr.getUser(dr.getUserIDForIdentityToken(identityToken));

                ArrayList<Alert> alerts = dr.getAlerts(user);
                JSONArray alertsJSONArray = new JSONArray();
                for (Alert alert : alerts) {
                    JSONObject alertJSONObject = new JSONObject();
                    alertJSONObject.put("AlertID", alert.getAlertID());
                    alertJSONObject.put("Symbol", alert.getSymbol());
                    alertJSONObject.put("Price", alert.getPrice());
                    alertJSONObject.put("OverPrice", alert.isOverPrice());
                    alertJSONObject.put("Executed", alert.isExecuted());

                    alertsJSONArray.add(alertJSONObject);
                }

                dr.close();

                responseJSON.put("Alerts", alertsJSONArray);
                responseJSON.put("Error", 0);

            } catch (ExpiredIdentityException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidCredentialsException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidIdentifierException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidValueException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (MissingKeyException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (SQLException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ParseException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            }
            isr.close();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseJSON.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(responseJSON.toString().getBytes());
            os.close();
        }
    }

    /**
     * RemoveAlert
     * @author alexcoundouriotis
     *
     * Functionality:
     * - Creates a User in the database and returns an Authentication token
     *
     * Parameters:
     * (none)
     *
     * POST JSON Contents:
     * "IdentityToken": User's IdentityToken
     * "AlertID": Alert's unique identifier
     *
     * Response JSON Contents:
     * "Error": Integer indicating the error, 0 if success
     *
     *
     */
    public static class RemoveAlert implements HttpHandler {
        @Override
        public void handle(HttpExchange httpsExchange) throws IOException {
            HttpsExchange t = (HttpsExchange) httpsExchange;
            JSONObject responseJSON = new JSONObject();

            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            JSONParser parser = new JSONParser();
            try {
                Driver dr = new Driver();
                JSONObject json = (JSONObject) parser.parse(sb.toString());

                String identityToken = extractString(json, "IdentityToken");
                String alertID = extractString(json, "AlertID");

                User user = dr.getUser(dr.getUserIDForIdentityToken(identityToken));
                ArrayList<Alert> alerts = dr.getAlerts(user);

                int i = 0;
                for (; i < alerts.size(); i++)
                    if (alerts.get(i).getAlertID().equals(alertID)) dr.removeAlert(alerts.get(i));
                if (i == 0) throw new InvalidIdentifierException("Alert ID");

                dr.close();

                responseJSON.put("Error", 0);

            } catch (ExpiredIdentityException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidCredentialsException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidIdentifierException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (InvalidValueException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (MissingKeyException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (FileNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (SQLException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            } catch (ParseException e) {
                responseJSON.put("Error", getErrorNumber(e));
                responseJSON.put("Reason", e.getLocalizedMessage());
            }
            isr.close();

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, responseJSON.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(responseJSON.toString().getBytes());
            os.close();
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            //Socket address setup
            InetSocketAddress address = new InetSocketAddress(443);

            //HTTPS Initialization
            HttpsServer httpsServer = HttpsServer.create(address, 0);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            //Keystore Initialization
            char[] password = Passwords.keystorePass;
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream("theRealKeystore.jks");

            ks.load(fis, password);

            //KeyManagerFactory Initialization
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            //TrustManagerFactory Initialization
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            //HTTPS Context and Parameters Setup
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters parameters) {
                    try {
                        //SSL Context Initialization
                        SSLContext context = SSLContext.getDefault();
                        SSLEngine engine = context.createSSLEngine();
                        parameters.setNeedClientAuth(false);
                        parameters.setCipherSuites(engine.getEnabledCipherSuites());
                        parameters.setProtocols(engine.getEnabledProtocols());

                        //Get Default Parameters
                        SSLParameters defaultSSLParameters = context.getDefaultSSLParameters();
                        parameters.setSSLParameters(defaultSSLParameters);
                    } catch (Exception e) {
                        System.out.println("Error creating HTTPS port.");
                        e.printStackTrace();
                    }
                }
            });

            httpsServer.createContext("/createUser", new CreateUser());
            httpsServer.createContext("/checkUser", new CheckUser());
            httpsServer.createContext("/userLogin", new UserLogin());
            httpsServer.createContext("/userLogout", new UserLogout());
            httpsServer.createContext("/addDeviceToken", new AddDeviceToken());
            httpsServer.createContext("/removeDeviceToken", new RemoveDeviceToken());
            httpsServer.createContext("/getWatchlists", new GetWatchlists());
            httpsServer.createContext("/addWatchlist", new AddWatchlist());
            httpsServer.createContext("/addStockToWatchlist", new AddStockToWatchlist());
            httpsServer.createContext("/removeStockFromWatchlist", new RemoveStockFromWatchlist());
            httpsServer.createContext("/getStock", new GetStock());
            httpsServer.createContext("/getStocks", new GetStocks());
            httpsServer.createContext("/addAlert", new AddAlert());
            httpsServer.createContext("/modifyAlert", new ModifyAlert());
            httpsServer.createContext("/getAlerts", new GetAlerts());
            httpsServer.createContext("/removeAlert", new RemoveAlert());

            httpsServer.setExecutor(null);
            httpsServer.start();

        } catch (Exception exception) {
            System.out.println("Failed to create HTTPS server on port " + 8000 + " of localhost");
            exception.printStackTrace();
        }
    }
}
