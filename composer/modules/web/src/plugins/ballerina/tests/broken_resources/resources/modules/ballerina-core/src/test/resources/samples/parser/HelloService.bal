
import ballerina/connectors.twitter;
import ballerina/connectors.salesforce as sf;

service HelloService {

  twitter:TwitterConnector t = create twitter:TwitterConnector(nil, nil, "clientkey", "clientsecret", nil);

  @POST {}
  @Path {value:"/tweet"}
  resource tweet (message m) {
      twitter:TwitterConnector.tweet(t, messages:getPayload(m));
  }
}
