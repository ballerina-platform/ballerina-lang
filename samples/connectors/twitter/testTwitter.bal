package samples.connector.twitter;

service TestTwitterService {

  twitterConnector:TwitterConnector twitter = new twitterConnector:TwitterConnector(nil, nil, "clientkey", "clientsecret", nil);

  @POST
  @Path ("/tweet")
  resource tweet (message m) {
      twitterConnector:TwitterConnector.tweet(twitter, (string) message:getPayload(m));
  }
}
