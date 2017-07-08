package samples.parser;

import ballerina.net.http;

service<http> HelloService {

  @Path {value:"/tweet"}
  resource {
      int b;
      reply m;
  }
}
