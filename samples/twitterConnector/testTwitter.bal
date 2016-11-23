package samples.twitterConnector;

service testTwitter;

actor samples.twitterConnector.TwitterData  td = new samples.twitterConnector.TwitterData("clientkey", "clientsecret");

var boolean loggedIn = false;

@POST
@Path ("/tweet")
resource tweet (message m) {
    if(!loggedIn) {
        samples.twitterConnector.init(td);
        loggedIn = true;
    }
    samples.twitterConnector.tweet(td, (string) message.getPayload(m));
}