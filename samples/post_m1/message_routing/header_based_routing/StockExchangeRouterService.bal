package samples.message_routing.header_based_routing;

import ballerina.lang.messages;
import ballerina.net.http;


@BasePath ("/stockexchange")
@Source (interface = "ballerina_default_http_listener")
@Service(description = "Service to route request between NYSE and NASDAQ stock exchanges")
service StockExchangeRouterService {

    http:ClientConnector nyseEP = new http:ClientConnector("http://localhost:8080/exchange/nyse/", {"timeOut" : 30000});
    http:ClientConnector nasdaqEP = new http:ClientConnector("http://localhost:8080/exchange/nasdaq/", {"timeOut" : 60000});

    @POST
    @Path ("/stock")
    resource passthrough (message m) {
        message response;
        string routingId;
        routingId = messages:getHeader(m, "X-STOCK-EX-ID");
        if (strings:equals(routingId, "NYSE")) {
            response = http:ClientConnector.sendPost (nyseEP, "/us", m);
        } else {
            response = http:ClientConnector.sendPost (nasdaqEP, "/us/en", m);
        }
        reply response;
    }
}
