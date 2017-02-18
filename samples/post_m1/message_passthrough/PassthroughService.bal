package samples.message_passthrough;

import ballerina.lang.messages;
import ballerina.net.http;


@BasePath ("/passthrough")
service PassthroughService {

    @POST
    @Path ("/stocks")
    resource passthrough (message m) {
        http:ClientConnector nyseEP = new http:ClientConnector("http://localhost:9000");

        message response;
        response = http:ClientConnector.post(nyseEP, "/services/nyse", m);
        reply response;
    }
}