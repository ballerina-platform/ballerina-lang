package samples.message_routing.header_based_routing;

import ballerina.lang.message;
import ballerina.net.http;


@BasePath ("/stockexchange")
@Source (interface = "ballerina_default_http_listener")
@Service(description = "Service to route request between NYSE and NASDAQ stock exchanges")
service StockExchangeRouterService {

    http:HttpConnector nyseEP = new http:HttpConnector("http://localhost:8080/exchange/nyse/", {"timeOut" : 30000});
    http:HttpConnector nasdaqEP = new http:HttpConnector("http://localhost:8080/exchange/nasdaq/", {"timeOut" : 60000});

    @POST
    @Path ("/stock")
    resource passthrough (message m) {
        message response;
        string routingId;
        routingId = message:getHeader(m, "X-STOCK-EX-ID");
        if (string:equals(routingId, "NYSE")) {
            response = http:HttpConnector.sendPost (nyseEP, "/us", m);
        } else {
            response = http:HttpConnector.sendPost (nasdaqEP, "/us/en", m);
        }
        reply response;
    }
}
