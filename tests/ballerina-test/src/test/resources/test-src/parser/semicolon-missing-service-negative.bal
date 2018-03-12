import ballerina/net.http;

service<http> HelloService {

  @POST {}
  @Path {value:"/tweet"}
  resource tweet (message m) {
      // Following line is invalid.
      int b
      return m;
  }
}
