package samples.parser;

import ballerina.net.http;

service<http> HelloService {

  @Path {value:"/tweet"}
  resource tweet {
      int b;
      reply m;
  }
}
