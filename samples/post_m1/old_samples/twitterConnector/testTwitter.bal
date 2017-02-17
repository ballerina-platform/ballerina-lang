package samples.twitterConnector;

service TestTwitterService {

  twitterConnector:TwitterConnector twitter = new twitterConnector:TwitterConnector(nil, nil, "clientkey", "clientsecret", nil);

  @POST
  @Path ("/tweet")
  resource tweet (message m) {
      twitterConnector:tweet(twitter, (string) messages:getPayload(m));
  }
}
