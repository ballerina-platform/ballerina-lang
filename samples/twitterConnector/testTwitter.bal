package samples.twitterConnector;

Service TestTwitterService {

twitterConnector.TwitterConnector twitter = new twitterConnector.TwitterConnector(nil, nil, "clientkey", "clientsecret", nil);

boolean loggedIn = false;

@POST
@Path ("/tweet")
resource tweet (message m) {
    if(!loggedIn) {
        twitterConnector.init(twitter);
        loggedIn = true;
    }
    twitterConnector.tweet(twitter, (string) message.getPayload(m));
}

}
