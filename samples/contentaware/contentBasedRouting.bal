package samples.contentaware;

import ballerina.net.http;
import ballerina.lang.json;
import ballerina.lang.message;

@BasePath ("/stock")
@Service(title = "StockExchangeService", description = "Exchange stock based on message content")
service ContentBasedRouteService;

actor HttpEndpoint nyseEP = new Endpoint("http://localhost:8080/exchange/nyse/");
actor HttpEndpoint nasdaqEP = new Endpoint("http://localhost:8081/exchange/nasdaq/");

@POST
@Produces ("application/json")
@Consumes ("application/json")
@Path("/exchange")
resource echoResource (message m) {
    var message response;
    var json jsonMsg = json.getPayload(m);
    try {
        if (json.get(jsonMsg, "$.stock.quote.exchange") == "NYSE") {
            response = http.sendPost(nyseEP, m);
        } else {
            response = http.sendPost(nasdaqEP, m);
        }
    } catch (exception e) {
        var json errorMsg = `{"error" : "Error while sending to backend"}`;
        message.setPayload(response, errorMsg);
        message.setHeader(response, "Status", 500);
    }
    reply response;
}