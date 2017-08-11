package samples.contentaware;

import ballerina.net.http;
import ballerina.lang.jsons;
import ballerina.lang.messages;

@BasePath ("/stock")
@Service(title = "Content Based Routing Service", description = "Routing a message based on the content")
service ContentBasedRouteService {

  http:ClientConnector nyseEP = new http:ClientConnector("http://localhost:8080/exchange/nyse/", {"timeOut" : 30000});
  http:ClientConnector nasdaqEP = new http:ClientConnector("http://localhost:8080/exchange/nasdaq/", {"timeOut" : 60000});

  @POST
  @Produces ("application/json")
  @Consumes ("application/json")
  @Path("/exchange")
  resource cbrResource (message m) {
      message response;
      json jsonMsg;
      json errorMsg;
      jsonMsg = jsons:getPayload(m);
      try {
          if (jsons:get(jsonMsg, "$.stock.quote.exchange") == "NYSE") {
              response = http:sendPost(nyseEP, m);
          } else {
              response = http:sendPost(nasdaqEP, m);
          }
      } catch (exception e) {
         errorMsg = `{"error" : "Error while sending to backend"}`;
         messages:setPayload(response, errorMsg);
         messages:setHeader(response, "Status", 500);
      }
      reply response;
  }
}
