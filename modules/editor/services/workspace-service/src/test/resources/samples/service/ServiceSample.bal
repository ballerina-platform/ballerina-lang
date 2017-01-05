package samples.service;

import ballerina.connectors.twitter;
import ballerina.connectors.salesforce as sf;

service HelloService {

  @GET
  @Path ("/tweet")
  resource tweet (message m) {
      reply m;
  }

}

function test(int a) (int) {
   return a+2;
}