package samples.contentaware;

import ballerina.net.http;
import ballerina.lang.json;
import ballerina.lang.message;
import ballerina.lang.system.log;

@Path("/iterate")
@Source(interface="localhost")
@Service(description = "Iterates through the message")
service IteratorService {

  http:ClientConnector stockEP = new http:ClientConnector("http://localhost:8080/exchange/", {"timeOut" : 30000});

  @POST
  @Consumes("application/json")
  @Path("/*")
  resource stockIterate (message m) {
      json jsonMsg;
      message response;
      jsonMsg = json:getPayload(m);
      iterate(json stock : json:get(jsonMsg, "$.stock.quote.exchange")){
          message:setPayload(stock, m);
          response = http:sendPost(stockEP, m);
          log:info(response);
      }
  }
}
