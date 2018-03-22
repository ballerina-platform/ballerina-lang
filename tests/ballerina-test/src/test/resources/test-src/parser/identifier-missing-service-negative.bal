import ballerina/connectors.twitter;
import ballerina/connectors.salesforce as sf;

service<http> HelloService {

  @POST {}
  @Path {value:"/tweet"}
   tweet (message m) {
      // Following line is invalid.
      int;
      reply m;
  }
}
