package samples.contentaware;

import ballerina.net.http;
import ballerina.lang.json;
import ballerina.lang.message;
import ballerina.lang.system.log;

@Path ("/iterate")
@Source (interface="localhost")
@Service (description = "Iterates through the message")
service IteratorService;

actor HttpEndpoint stockEP = new HttpEndpoint("http://localhost:8080/exchange/");

@POST
@Consumes ("application/json")
@Path("/exchange")
resource iterate (message m) {
    var json jsonMsg = json.getPayload(m);
    foreach (message stock : json.get(jsonMsg, "$.stock.quote.exchange")){
        var message response = http.sendPost(stockEP, stock);
        log.info(response);
    }
}