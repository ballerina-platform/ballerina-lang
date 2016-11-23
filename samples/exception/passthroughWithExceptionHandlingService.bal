package samples.passthrough;

import ballerina.lang.message;
import ballerina.net.http;
import ballerina.lang.json;


@BasePath ("/stock")
@Source (interface = "passthroughinterface")
@Service(title = "NYSEService", description = "NYSE service")
service PassthroughWithExceptionHandlingService;

actor HttpEndpoint nyse_ep = new HttpEndpoint ("http://localhost:8080/exchange/");

@GET
@PUT
@POST
@Path ("/passthrough")
resource passthrough (message m) {
  var  message response;
    try {
        response = http.sendPost (nyse_ep, m);
    } catch (exception e) {
        message.setHeader(m, HTTP.StatusCode, 500);// need to discuss
        var json error = `{"error":"backend failed"}`;
        message.setPayload(m, error);
    }
    reply response;

}

