package samples.contentaware;

import ballerina.net.http;
import ballerina.lang.jsons;
import ballerina.lang.messages;
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
      jsonMsg = jsons:getPayload(m);
      iterate(json stock : jsons:get(jsonMsg, "$.stock.quote.exchange")){
          messages:setPayload(stock, m);
          response = http:sendPost(stockEP, m);
          log:info(response);
      }
  }
}
