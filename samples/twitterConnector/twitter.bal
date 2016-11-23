package samples.twitterConnector;

import ballerina.net.http;

type TwitterData {
    string clientKey;
    string clientSecret;
    string oAuthToken;
}

actor http.HttpEndpoint twitterEP = alloc http.HttpEndpoint ("https://api.twitter.com");

action init(TwitterData td) throws exception {
    var json loginReq = `{"clientKey" : "$td.clientKey", "clientSecret" : "$td.clientSecret"}`;
    var message loginMessage = new message;

    message.setPayload(loginMessage, loginReq);

    var message response = http.post(twitterEP, "/token", loginMessage);

    td.oAuthToken = json.get(message.getPayload(response), "$.oAuthToken");
}

action tweet(TwitterData td, string tweet) throws exception {
    var json tweetJson = `{"message" : "$tweet"}`;
    var message tweetMsg = new message;

    message.setPayload(tweetMsg, tweetJson);
    message.setHeader(tweetMsg, "Authorization", "Bearer " + td.oAuthToken);
    http.post(twitterEP, "/tweet", tweetMsg);
}

