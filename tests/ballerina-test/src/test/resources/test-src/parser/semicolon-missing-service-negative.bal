import ballerina/http;

service<http:Service> HelloService {

  @POST {}
  @Path {value:"/tweet"}
  tweet (message m) {
      // Following line is invalid.
      int b
      return m;
  }
}
