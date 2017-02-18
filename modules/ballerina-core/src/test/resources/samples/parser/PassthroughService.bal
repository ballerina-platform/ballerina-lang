package samples.message_passthrough;

import ballerina.lang.messages;
import ballerina.net.http as http;


@BasePath ("/passthrough")
service PassthroughService {

    @POST
    @Path ("/stocks")
    resource passthrough (message m) {
        http:ClientConnector nyseEP = new http:ClientConnector("http://localhost:8280/exchange/nyse/");

        message response;
        response = http:ClientConnector.post(nyseEP, "/us", m);
        reply response;
    }
}
