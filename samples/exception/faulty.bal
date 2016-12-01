package samples.exception;

import ballerina.net.http;

service FaultyService {

  http.HttpConnector nyseEP = new http.HttpConnector("http://localhost:8080/exchange/nyse/", {"timeOut" : 30000});
  http.HttpConnector nasdaqEP = new http.HttpConnector("http://localhost:8080/exchange/nasdaq/", {"timeOut" : 60000});

  @GET
  @Path ("/*")
  resource faultyResource (message m) {
      // exception thrown by faultyFunction will be handled by ballerina runtime.
      reply faultyFunction(m);
  }

}

function faultyFunction(message in) (message) throws exception {
      http.HttpConnector e1 = new http.HttpEndpoint ();
      http:setURL(e1, "http://localhost:2222");
      message response = http:get (e1, "/test", in);
      return response;
  }
