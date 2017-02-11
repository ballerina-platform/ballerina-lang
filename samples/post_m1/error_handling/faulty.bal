package samples.error_handling;

import ballerina.net.http;

//
//Work in progress
//

service FaultyService {

  http:ClientConnector nyseEP = new http:ClientConnector("http://localhost:8080/exchange/nyse/", {"timeOut" : 30000});
  http:ClientConnector nasdaqEP = new http:ClientConnector("http://localhost:8080/exchange/nasdaq/", {"timeOut" : 60000});

  @GET
  @Path ("/*")
  resource faultyResource (message m) {
      // exception thrown by faultyFunction will be handled by ballerina runtime.
      reply faultyFunction(m);
  }

}

function faultyFunction(message in) (message) throws exception {
      http:ClientConnector e1 = new http:HttpEndpoint ("http://localhost:2222");
      message response;
      response = http:ClientConnector.get (e1, "/test", in);
      return response;
  }
