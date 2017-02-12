package samples.connectors.twitter;

import ballerina.net.http;

function init(TwitterConnector t) throws exception {
    json loginReq;
    message loginMessage;
    message response;

    if (t.username == nil) {
        loginReq = `{"clientKey" : "$t.clientKey", "clientSecret" : "$t.clientSecret"}`;
    } else {
        loginReq = `{"userName" : "$t.userName", "password" : "$t.password"}`;
    }

    loginMessage = new message;
    message:setPayload(loginMessage, loginReq);
    response = http:post(twitterEP, "/token", loginMessage);
    t.oAuthToken = json:get(message:getPayload(response), "$.oAuthToken");
}

connector Twitter(string username, string password,
                string clientKey, string clientSecret, string oAuthToken, map options) {

    http:ClientConnector h = new http:ClientConnector("https://api.twitter.com", {"timeOut" : 300});

    boolean loggedIn; // default value get assigned
    action tweet(Twitter t, string tweet) throws exception {
        json tweetJson;
        message tweetMsg;

        if(!loggedIn){
            init(t);
            loggedIn = true;
        }
        tweetJson = `{"message" : "$tweet"}`;
        tweetMsg = new message;
        message:setPayload(tweetMsg, tweetJson);
        message:setHeader(tweetMsg, "Authorization", "Bearer " + t.oAuthToken);
        http:ClientConnector.post(h, "/tweet", tweetMsg);
    }

}