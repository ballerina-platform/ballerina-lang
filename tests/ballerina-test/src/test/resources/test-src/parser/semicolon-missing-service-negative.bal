import ballerina.net.http;

service<http:Service> HelloService {

  @POST {}
  @Path {value:"/tweet"}
  resource tweet (message m) {
      // Following line is invalid.
      int b
      return m;
  }
}
