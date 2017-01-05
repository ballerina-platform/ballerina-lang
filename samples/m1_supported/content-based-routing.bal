package samples.contentaware;

import ballerina.net.http;
import ballerina.lang.json;
import ballerina.lang.message;
import ballerina.lang.system;

@BasePath ("/stock")
service ContentBasedRouteService {

  @POST
  @Path("/*")
  resource cbrResource (message m) {
    // Connector declarations
    http:HTTPConnector nyseEP = new http:HTTPConnector("http://localhost:9090/NYSEStocks");
    http:HTTPConnector nasdaqEP = new http:HTTPConnector("http://localhost:9090/NASDAQStocks");

    // Variable declarations
    message response;
    json jsonMsg;
    json errorMsg;
    string result;
    string nameString;
    string nyseString;
    message request;
    json requestJson;

    // Assignment statements
    nyseString = "NYSE";

    // Extract the JSON message
    jsonMsg = message:getJsonPayload(m);

    // Convert to string
    result = json:toString(jsonMsg);
    system:println(result);

    // Evaluate JSON path and get the result
    nameString = json:getString(jsonMsg, "$.name");
    system:println(nameString);

    // Extract the outgoing json message from request
    requestJson = json:getJson(jsonMsg, "$");
    message:setJsonPayload(m, requestJson);

    if (nameString == nyseString) {
        response = http:HTTPConnector.post(nyseEP, "/", m);
    } else {
        response = http:HTTPConnector.post(nasdaqEP, "/", m);
    }
    reply response;
  }
}

@BasePath("/NYSEStocks")
service NYSEStockQuote {

  @POST
  @Path("/*")
  resource stocks (message m) {
    message response;
    json payload;
    response = new message;
    payload = `{"exchange":"nyse", "name":"IBM", "value":"127.50"}`;
    message:setJsonPayload(response, payload);
    reply response;
  }
}

@BasePath("/NASDAQStocks")
service NASDAQStockQuote {

  @POST
  @Path("/*")
  resource stocks (message m) {
    message response;
    json payload;
    response = new message;
    payload = `{"exchange":"nasdaq", "name":"IBM", "value":"127.50"}`;
    message:setJsonPayload(response, payload);
    reply response;
  }
}