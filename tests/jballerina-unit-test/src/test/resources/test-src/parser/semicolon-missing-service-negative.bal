import ballerina/http;

service HelloService on new http:MockListener(9090){

  @POST {}
  @Path {value:"/tweet"}
  resource function tweet (message m) {
      // Following line is invalid.
      int b
      return m;
  }
}
