import org.wso2.ballerina.connectors.twitter;

import ballerina.lang.jsons;
import ballerina.lang.system;
import ballerina.net.http;

function main (string[] args) {

    twitter:ClientConnector twitterConnector;
    // Create a Twitter Client Connector with the arguments consumerKey, consumerSecret, accessToken, accessTokenSecret of your Twitter account
    twitterConnector = create twitter:ClientConnector(args[0], args[1], args[2], args[3]);

    // Call tweet action from Twitter Connector with a message to tweet in above Twitter account
    http:Response tweetResponse = twitterConnector.tweet ("Hello Ballerina!!");
    json tweetJSONResponse = tweetResponse.getJsonPayload();

    //Print the response
    system:println("====== Response from tweet action=====");
    system:println(jsons:toString(tweetJSONResponse));
    string tweetId;
    // Get the tweet id form response if the tweet is successful.
    if(tweetJSONResponse.errors == null){
        tweetId = jsons:toString(tweetJSONResponse.id);
    }

    // Call search action to retrieve the collection of relevant Tweets matching a specified query
    tweetResponse = twitterConnector.search ("Hello Ballerina!!");

    tweetJSONResponse = tweetResponse.getJsonPayload();
    system:println("====== Response from search action =====");
    system:println(jsons:toString(tweetJSONResponse));

    if(tweetId != "") {
        // Call retweet action to retweet the above tweet
        tweetResponse = twitterConnector.retweet (tweetId);
        tweetJSONResponse = tweetResponse.getJsonPayload();

        system:println("====== Response from retweet action =====");
        system:println(jsons:toString(tweetJSONResponse));

        // Call unretweet action to untweet a retweeted status
        tweetResponse = twitterConnector.unretweet (tweetId);
        tweetJSONResponse = tweetResponse.getJsonPayload();

        system:println("====== Response from unretweet action =====");
        system:println(jsons:toString(tweetJSONResponse));

        // Call showStatus action to retrive a single status
        tweetResponse = twitterConnector.showStatus (tweetId);
        tweetJSONResponse = tweetResponse.getJsonPayload();

        system:println("====== Response from showStatus action =====");
        system:println(jsons:toString(tweetJSONResponse));

        // Call destroyStatus action to distroy a status
        tweetResponse = twitterConnector.destroyStatus (tweetId);
        tweetJSONResponse = tweetResponse.getJsonPayload();

        system:println("====== Response from destroyStatus action =====");
        system:println(jsons:toString(tweetJSONResponse));
    }

}