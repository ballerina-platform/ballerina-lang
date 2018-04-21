import ballerina/lang.system;
import ballerina/doc;

@doc:Description {value:"This is the base connector we are going to decorate"}
connector StockQuoteConnector (int i) {
    action getStock (StockQuoteConnector stockConnector, string ID)(int stockPrice) {
        // Here we return a default value from base connector. In real world, this will call the
        // actual back end service and get the result
        return 999;
    }
}

@doc:Description {value:"This is the filter connector which will be decorating the base connector"}
connector CacheConnector<StockQuoteConnector stockC> (string j) {
    map cachedKeys = {"IBM":350, "WSO2":300};
    action getStock (CacheConnector cacheConnector, string ID)(int stockPrice) {
        int result = -1;
        // If the ID is not present in the cache, call the action of the base connector.
        // Otherwise, return the value from the cache (map)
        if (cachedKeys[ID] != null) {
            result, _ = (int) cachedKeys[ID];
        } else {
            // Calling the action of the base connector
            result = stockC.getStock(ID);
            cachedKeys[ID] = result;
        }
        return result;
    }
}

function main(string... args) {
    // Create the 'StockQuoteConnector' with 'CacheConnector' as the filter connector
    StockQuoteConnector stockQC = create StockQuoteConnector(5) with CacheConnector("Bob");

    // Invoke the action of the 'StockQuoteConnector' with a cached key 'WSO2'
    int price = stockQC.getStock("WSO2");
    system:println(price);

    // Invoke the action of the 'StockQuoteConnector' with a cached key 'IBM'
    price = stockQC.getStock("IBM");
    system:println(price);

    // Invoke the action of the 'StockQuoteConnector' with a non-cached key 'Ballerina'
    price = stockQC.getStock("Ballerina");
    system:println(price);
    

}