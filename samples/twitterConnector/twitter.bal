package samples.twitterConnector;

import ballerina.net.http;

connector TwitterConnector(
    string username, string password, string clientKey, string clientSecret, string oAuthToken,
    options {"autoReConnect" : false}
);

http.HttpConnector httpConnection = new http.HttpConnector("https://api.twitter.com", {"timeOut" : 300});

action init(TwitterConnector t) throws exception {
    json loginReq;

    if (t.username == nil) {
        loginReq = `{"clientKey" : "$t.clientKey", "clientSecret" : "$t.clientSecret"}`;
    } else {
         loginReq = `{"userName" : "$t.userName", "password" : "$t.password"}`;
    }

    message loginMessage = new message;
    message.setPayload(loginMessage, loginReq);
    message response = http.post(twitterEP, "/token", loginMessage);
    t.oAuthToken = json.get(message.getPayload(response), "$.oAuthToken");
}

action tweet(TwitterConnector t, string tweet) throws exception {
    json tweetJson = `{"message" : "$tweet"}`;
    message tweetMsg = new message;
    message.setPayload(tweetMsg, tweetJson);
    message.setHeader(tweetMsg, "Authorization", "Bearer " + t.oAuthToken);
    http.post(twitterEP, "/tweet", tweetMsg);
}
