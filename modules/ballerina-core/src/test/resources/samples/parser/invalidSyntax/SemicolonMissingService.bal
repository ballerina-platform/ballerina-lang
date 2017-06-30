package samples.parser;

import ballerina.connectors.twitter;
import ballerina.connectors.salesforce as sf;
import ballerina.net.http;

service<http> HelloService {

  @POST {}
  @Path {value:"/tweet"}
  resource tweet (message m) {
      // Following line is invalid.
      int b
      reply m;
  }
}
