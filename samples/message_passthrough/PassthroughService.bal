package samples.message_passthrough;

import ballerina.lang.message;
import ballerina.net.http as http;


@BasePath ("/passthrough")
@Source (interface = "default_http_listener")
@Service(description = "Passthrough service for NYSE service")
service PassthroughService {

    http:HttpConnector nyseEP = new http:HttpConnector("http://localhost:8080/exchange/nyse/", {"timeOut" : 30000});

    @POST
    @Path ("/stocks")
    resource passthrough (message m) {
        message response;
        response = http:HttpConnector.sendPost (nyseEP, "/us", m);
        reply response;
    }
}
