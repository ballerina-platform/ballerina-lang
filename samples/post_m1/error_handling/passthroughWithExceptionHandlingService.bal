package samples.error_handling;

import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.lang.jsons;


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
        messages:setHeader(m, HTTP.StatusCode, strings:valueOf(500));// need to discuss
        error = `{"error":"backend failed", "causedby":e.message}`;
        messages:setJsonPayload(m, error);
    }
    reply response;
  }
}
