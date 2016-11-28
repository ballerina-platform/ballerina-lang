package samples.contentaware;

import ballerina.net.http;
import ballerina.lang.json;
import ballerina.lang.message;

@BasePath ("/stock")
@Service(title = "StockExchangeService", description = "Exchange stock based on message content")
Service ContentBasedRouteService{

http.HttpConnector nyseEP = new http.HttpConnector("http://localhost:8080/exchange/nyse/", {"timeOut" : 30000});
http.HttpConnector nasdaqEP = new http.HttpConnector("http://localhost:8080/exchange/nasdaq/", {"timeOut" : 60000});

@POST
@Produces ("application/json")
@Consumes ("application/json")
@Path("/exchange")
resource echoResource (message m) {
    message response;
    json jsonMsg = json.getPayload(m);
    try {
        if (json.get(jsonMsg, "$.stock.quote.exchange") == "NYSE") {
            response = http.sendPost(nyseEP, m);
        } else {
            response = http.sendPost(nasdaqEP, m);
        }
    } catch (exception e) {
        json errorMsg = `{"error" : "Error while sending to backend"}`;
        message.setPayload(response, errorMsg);
        message.setHeader(response, "Status", 500);
    }
    reply response;
}
}
