package samples.connectors.twitter version 1.0.0;

import ballerina.net.http version 1.1 as http11;

function init(TwitterConnector t) throws exception {
    json loginReq;

    if (t.username == nil) {
        loginReq = `{"clientKey" : "$t.clientKey", "clientSecret" : "$t.clientSecret"}`;
    } else {
        loginReq = `{"userName" : "$t.userName", "password" : "$t.password"}`;
    }

    message loginMessage = new message;
    message:setPayload(loginMessage, loginReq);
    message response = http:post(twitterEP, "/token", loginMessage);
    t.oAuthToken = json:get(message:getPayload(response), "$.oAuthToken");
}

connector Twitter(string username, string password,
        string clientKey, string clientSecret, string oAuthToken, map options) {

    boolean loggedIn = false;
    http:HttpConnector h = new http:HttpConnector("https://api.twitter.com", {"timeOut" : 300});

    action tweet(Twitter t, string tweet) throws exception {
        if(!loggedIn){
            init(t);
            loggedIn = true;
        }
        json tweetJson = `{"message" : "$tweet"}`;
        message tweetMsg = new message;
        message:setPayload(tweetMsg, tweetJson);
        message:setHeader(tweetMsg, "Authorization", "Bearer " + t.oAuthToken);
        http:HttpConnector.post(h, "/tweet", tweetMsg);
    }
}