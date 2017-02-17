package samples.connectors.twitter;

service TestTwitterConnector {

  twitter:TwitterConnector t = new twitter:TwitterConnector(nil, nil, "clientkey", "clientsecret", nil);

  @POST
  @Path ("/tweet")
  resource tweet (message m) {
      twitter:TwitterConnector.tweet(t, (string) messages:getPayload(m));
  }
}
