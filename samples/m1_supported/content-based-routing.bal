package samples.contentaware;

import ballerina.net.http;
import ballerina.lang.json;
import ballerina.lang.message;
import ballerina.lang.system;

@BasePath ("/stock")
service ContentBasedRouteService {

  @POST
  @Path("/exchange")
  resource cbrResource (message m) {
    // Connector declarations
    http:HTTPConnector nyseEP = new http:HTTPConnector("http://localhost:8280/services", 30000);
    http:HTTPConnector nasdaqEP = new http:HTTPConnector("http://localhost:8280/services", 60000);

    // Variable declarations
    message response;
    json jsonMsg;
    json errorMsg;
    string result;
    string nameString;
    string nyseString;

    // Assignment statements
    nyseString = "NYSE";

    // Extract the JSON message
    jsonMsg = message:getJsonPayload(m);

    // Convert to string
    result = json:toString(jsonMsg);
    system:println(result);

    // Evaluate JSON path and get the result
    nameString = json:getString(jsonMsg, "$.name");
    system:println(nameString);


    if (nameString == nyseString) {
        response = http:HTTPConnector.post(nyseEP, "/NYSEProxy", m);
    } else {
        response = http:HTTPConnector.post(nasdaqEP, "/NASDAQProxy", m);
    }
    reply response;
  }
}
