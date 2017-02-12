package samples.exception;

import ballerina.lang.message;
import ballerina.net.http;
import ballerina.lang.json;


@BasePath ("/stock")
@Source (interface = "passthroughinterface")
@Service(title = "NYSEService", description = "NYSE service")
service PassthroughWithExceptionHandlingService {

  http:ClientConnector nyseEP = new http:ClientConnector("http://localhost:8080/exchange/", {"timeOut" : 30000});

  @GET
  @PUT
  @POST
  @Path ("/passthrough")
  resource passthrough (message m) {
    message response;
    json error;
    try {
        response = http:ClientConnector.sendPost (nyse_ep, m);
    } catch (exception e) {
        message:setHeader(m, HTTP.StatusCode, 500);// need to discuss
        error = `{"error":"backend failed", "causedby":e.message}`;
        message:setPayload(m, error);
    }
    reply response;
  }
}
