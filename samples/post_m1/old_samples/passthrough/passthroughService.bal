package samples.passthrough;

import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.lang.jsons;


@BasePath ("/stock")
@Source (interface = "passthroughinterface")
@Service(title = "NYSEService", description = "NYSE service")
service PassthroughService {

  http:ClientConnector nyseEP = new http:ClientConnector("http://localhost:8080/exchange/nyse/", {"timeOut" : 30000});

  @GET
  @PUT
  @POST
  @Path ("/passthrough")
  resource passthrough (message m) {
    message response;
    response = http:sendPost (nyseEP, m);
    reply response;
  }

}
