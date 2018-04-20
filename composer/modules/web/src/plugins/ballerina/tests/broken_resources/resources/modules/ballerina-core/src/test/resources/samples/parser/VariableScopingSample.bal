
import ballerina/connectors.twitter;
import ballerina/connectors.salesforce as sf;

service HelloService {

  int a;

  @POST {}
  @Path {value:"/tweet"}
  resource tweet (message m) {
      int b;
      a = 10;
      b = 12;
  }
}
