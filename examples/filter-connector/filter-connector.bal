import ballerina/io;

@Description {value:"This is the base connector you are going to decorate."}
connector StockQuoteConnector (int i) {
    action getStock (string ID) (int stockPrice) {
        //This value is returned by the base connector.
        //In a real world scenario, a backend service is called to get the result.
        return 999;
    }
}

@Description {value:"This is the filter connector, which will be decorating the base connector."}
connector CacheConnector<StockQuoteConnector stockC> (string j) {
    //For this example, the connector is pre-loaded with sample cache values that are stored in a map.
    map cachedKeys = {"IBM":350, "WSO2":300};
    action getStock (string ID) (int stockPrice) {
        int result = -1;
        // If the ID that was called is defined in the cache map, it returns the value assigned to the ID.
        //Else, it calls the functions in the base connector.
        if (cachedKeys[ID] != null) {
            result, _ = (int)cachedKeys[ID];
        } else {
            // If the ID that was called does not match the values in the cache, which is the functionality of the
            //filter connector, the base connectors functionality is called to check for the ID and its corresponding
            //value.
            //Once the value is found, it is stored in the 'cachedKeys' map.
            result = stockC.getStock(ID);
            cachedKeys[ID] = result;
        }
        return result;
    }
}

function main (string[] args) {
    // Create the 'StockQuoteConnector' that acts as the base connector and decorate it using the 'CacheConnector'
    // that acts as the filter connector.
    StockQuoteConnector stockQC = create StockQuoteConnector(5)
                                  with CacheConnector("Bob");

    // Invoke the action of the 'StockQuoteConnector' by passing 'WSO2' as the ID. Since WSO2 is stored
    //in the 'cachedKeys' map of the filter connector the respective price value is printed.
    int price = stockQC.getStock("WSO2");
    io:println(price);

    // Invoke the action of the 'StockQuoteConnector' by passing 'IBM' as the ID. Since WSO2 is stored in the
    // 'cachedKeys' map of the filter connector the respective price value is printed.
    price = stockQC.getStock("IBM");
    io:println(price);

    // Invoke the action of the 'StockQuoteConnector' by passing 'Ballerina' as the ID. Since 'Ballerina' is not stored
    // in the 'cachedKeys' map of the filter connector, the base connector functionality is called and the price defined
    //for it is printed.
    price = stockQC.getStock("Ballerina");
    io:println(price);
}
