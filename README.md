# Stockie HTTPS Server

Stockie HTTPS Server is the backend for the Stockie mobile app and web service. 

# Errors

| Error Number | Description |
| --- | --- |
| 0 | Success |
| 1 | Unknown |
| 40 | Missing Key |
| 41 | Invalid Value |
| 50 | Invalid Credentials |
| 51 | Invalid Identifier |
| 53 | Duplicate Object |
| 54 | Duplicate Identifier |
| 55 | Issue Associating Objects |
| 56 | Isse Parsing Date Time |
| 57 | Expired Identity |

# Reference

## Create User
Creates a user in the database and returns an IdentityToken.

#### Endpoint:
`/createUser`

#### POST JSON Contents:
```json5
{
  "Username": "", //User's proposed username
  "Password": "" //User's proposed password
}
```

#### Response JSON Contents:
```json5
{
  "IdentityToken": "", //Token associated with the current Identity of the User
  "Error": 0 //54, 53, 50, 41, 40, 59, 1
}
```


## Check User
Checks to see if a Username exists in database

#### Endpoint:
`/checkUser`

#### POST JSON Contents:
```json5
{
  "Username": "" //User's proposed username
}
```

#### Response JSON Contents:
```json5
{
  "Error": 0 //53, 41, 40, 59, 1
}
```


## User Login
Generates new Identity for User and returns its IdentityToken.

#### Endpoint:
`/userLogin`

#### POST JSON Contents:
```json5
{
  "Username": "", //User's username  
  "Password": "" //User's password
}
```

#### Response JSON Contents:
```json5
{
  "IdentityToken": "", //Token associated with the current identity of the User
  "Error": 0 //51, 41, 40, 59, 1
}
```


## User Logout
Deletes the identity for the IdentityToken the User is currently using.

#### Endpoint:
`/userLogout`

#### POST JSON Contents:
```json5
{
  "IdentityToken": "" //Token associated with the current identity of the User
}
```

#### Response JSON Contents:
```json5
{
  "Error": 0 //41, 40, 59, 1
}
```


## Add Device Token
Associates an Apple Push Notification Service (APNS) DeviceToken with the User associated with the Identity that is associated with the IdentityToken.

#### Endpoint:
`/addDeviceToken`

#### POST JSON Contents:
```json5
{
  "IdentityToken": "", //Token associated with the current identity of the User
  "DeviceToken": "" //Token associated with the Device, registered with APNS
}
```

#### Response JSON Contents:
```json5
{
  "Error": 0 //57, 50, 51, 41, 40, 59, 1
}
```


## Remove Device token
Removes an APNS DeviceToken from the User associated with the Identity that is associated with the IdentityToken.

#### Endpoint:
`/removeDeviceToken`

#### POST JSON Contents:
```json5
{
  "IdentityToken": "", //Token associated with the current identity of the User
  "DeviceToken": "" //Token associated with the Device, registered with APNS
}
```

#### Response JSON Contents:
```json5
{
  "Error": 0 //57, 50, 51, 41, 40, 59, 1
}
```


## Get Watchlists
Gets all Watchlists associated with the User that the Identiy associated with the IdentityToken contains. This includes all Stocks in these Watchlists.

#### Endpoint:
`/getWatchlists`

#### POST JSON Contents:
```json5
{
  "IdentityToken": "" //Token associated with the current identity of the User
}
```

#### Response JSON Contents:
```json5
{
  "Watchlists": [   //Array of Watchlists associated with the User
    {
      "WatchlistName": "", //User specified name of the Watchlist
      "WatchlistID": "", //Java HTTPS Server generated unique identifier for the Watchlist`  ,
      "IsDefault": true, //Boolean value indicating if the Watchlist is the default Watchlist for the User (only 1 per User allowed)  
      "Stocks": [ //Array of Stocks in Watchlist
        {
          "Symbol": "", //The symbol of the Stock
          "Company": "", //Name of the Company that the Stock represents
          "LastPrice": 100.0 //Double value representing the most recent price of the Stock
        }
      ]  
    }
  ],
  "Error": 0 //57, 50, 51, 41, 40, 59, 1
}
```


## Add Watchlist
Creates a new Watchlist with data passed in POST JSON and associates it with the User associated with the Identity associated with the IdentityToken. Stocks are added in a different call.

#### Endpoint:
`/addWatchlist`

#### POST JSON Contents:
```json5
{
  "IdentityToken": "", //Token associated with the current identity of the User
  "WatchlistName": "", //User specified name of the Watchlist
  "IsDefault": true //Boolean value indicating if the Watchlist is the default Watchlist for the User (only 1 per User allowed)
}
```

#### Response JSON Contents:
```json5
{
  "WatchlistName": "", //User specified name of the Watchlist
  "WatchlistID": "", //Java HTTPS Server generated unique identifier for the Watchlist
  "Error": 0 //54, 57, 50, 51, 41, 40, 59, 1
}
```


## Add Stock to Watchlist
Associates a Stock symbol with the Watchlist associated with the Watchlist ID, as long as it is associated with the User that is associated with the Identity that is associated with the Identity Token.

#### Endpoint:
`/addStockToWatchlist`

#### POST JSON Contents:
```json5
{
  "IdentityToken": "", //Token associated with the current identity of the User
  "WatchlistID": "", //Unique identifier associated with the Watchlist to associate the Stock with
  "StockSymbol": "" //The symbol of the Stock that is to be associated with the given Watchlist
}
```

#### Response JSON Contents:
```json5
{
  "Error": 0 //54, 53, 57, 50, 51, 41, 40, 59, 1
}
```


## Remove Stock From Watchlist
Removes the association between a Stock symbol and the Watchlist associated with the Watchlist ID, as long as it is associated with the User that is associated with the Identity that is associated with the Identity Token.

#### Endpoint:
`/removeStockFromWatchlist`

#### POST JSON Contents:
```json5
{
  "IdentityToken": "", //Token associated with the current identity of the User
  "WatchlistID": "", //Unique identifier associated with the Watchlist to associate the Stock with
  "StockSymbol": "" //The symbol of the Stock that is to be associated with the given Watchlist
}
```

#### Response JSON Contents:
```json5
{
  "Error": 0 //57, 50, 51, 41, 40, 59, 1
}
```


## Get Stock
Gets a Stock from the database, making sure the User is authenticated by checking if the Identity Token is associated with a valid user and not expired.

#### Endpoint:
`/getStock`

#### POST JSON Contents
```json5
{
  "IdentityToken": "", //Token associated with the current identity of the User
  "StockSymbol": "" //The symbol of the Stock that is to be retrieved
}
```

#### Response JSON Contents
```json5
{
  "StockSymbol": "", //The symbol of the Stock requested
  "Company": "", //The company that the Stock requested represents
  "LastPrice": "", //The most recent price of the Stock
  "Error": 0 //53, 57, 50, 51, 41, 40, 59, 1
}
```


## Get Stocks
Gets requested Stocks from the database, making sure the User is authenticated by checking if the Identity Token is associated with a valid user and not expired.

#### Endpoint:
`/getStocks`

#### POST JSON Contents
```json5
{
  "IdentityToken": "", //Token associated with the current identity of the User
  "StockSymbols": [] //An array of symbols as Strings of the Stocks that are to be retrieved
}
```

#### Response JSON Contents
```json5
{
  "Stocks": [ //JSON Array of all Stocks to requested
    {
      "StockSymbol": "", //The symbol of the Stock requested
      "Company": "", //The company that the Stock requested represents
      "LastPrice": "", //The most recent price of the Stock
    }
  ],
  "Error": 0 //53, 57, 50, 51, 41, 40, 59, 1
}
```


## Add Alert
Creates an Alert in the database that is associated with a User that is associated with the Identity associated with the Identity Token and a Stock that is associated with a symbol at a certain price. Automatically determines if the Alert will trigger if the price is above or below the Alert price. Let me know a better way to phrase this :).

#### Endpoint:
`/addAlert`

#### POST JSON Contents:
```json5
{
  "IdentityToken": "", //Token associated with the current identity of the User
  "StockSymbol": "", //The symbol of the Stock that is to be associated with the given Watchlist
  "Price": "" //The price of the Stock specified that triggers the Alert
}
```

#### Response JSON Contents:
```json5
{
  "Error": 0 //53, 57, 50, 51, 41, 40, 59, 1
}
```


## Modify Alert
Modifies an Alert price in the database. IdentityToken is included to validate the User and the association with that Alert. 

#### Endpoint:
`/modifyAlert`

#### POST JSON Contents:
```json5
{
  "IdentityToken": "", //Token associated with the current identity of the User
  "AlertID": "", //The unique identifier associated with the Alert
  "Price": "" //The price of the Stock specified that triggers the Alert
}
```

#### Response JSON Contents:
```json5
{
  "AlertID": "", //The unique identifier of the Alert,
  "Symbol": "", //The symbol the Alert is acting on,
  "Price": "", //The price threshold in which the alert activates,
  "OverPrice": "", //A Boolean value that denotes whether the Price set was over the current stock price or not,
  "Executed": "", //A Boolean value that denotes whether the Alert has been executed or not,
  "Error": 0 //53, 57, 50, 51, 41, 40, 59, 1
}
```


## Get Alerts
Gets all of the Alerts associated with the User associated with the Identity associated with the Identity Token.

#### Endpoint:
`/getAlerts`

#### POST JSON Contents:
```json5
{
  "IdentityToken": "", //Token associated with the current identity of the User
}
```

#### Response JSON Contents:
```json5
{
  "Alerts": [   //JSON Array consisting of all the Alert objects associated with the User
    {
      "AlertID": "", //The unique identifier of the Alert object
      "Symbol": "", //The symbol of the Stock that the Alert will act on
      "Price": 100.0, //Double value denoting the price that the Alert will activate on
      "OverPrice": true, //Boolean value that denotes if the price set was over the current stock price
      "Executed": false, //Boolean value that denotes if the Alert has been executed
    }
  ],
  "Error": 0 //57, 50, 51, 41, 40, 59, 1
}
```


## Remove Alert
Removes the Alert associated with the Alert ID specified, as long as it is associated with the User that is associated with the Identity that is associated with the Identity Token.

#### Endpoint:
`/removeAlert`

#### POST JSON Contents:
```json5
{
  "IdentityToken": "", //Token associated with the current identity of the User
  "AlertID": "" //The unique identifier of the Alert object
}
```

#### Response JSON Contents:
```json5
{
  "Error": 0 //57, 50, 51, 41, 40, 59, 1
}
```