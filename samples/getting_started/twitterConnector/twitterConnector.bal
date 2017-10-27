import ballerina.lang.jsons;

import ballerina.net.http;
import ballerina.net.uri;
import ballerina.util;

connector Twitter (string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret) {
    http:ClientConnector tweeterEP = create http:ClientConnector("https://api.twitter.com", {});
    action tweet (string msg) (http:Response) {
        http:Request request = {};
        string oauthHeader = constructOAuthHeader(consumerKey, consumerSecret, accessToken, accessTokenSecret, msg);
        string tweetPath = "/1.1/statuses/update.json?status=" + uri:encode(msg);
        request.setHeader("Authorization", oauthHeader);
        http:Response response = tweeterEP.post(tweetPath, request);
        return response;

    }

}
function constructOAuthHeader (string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret, string tweetMessage) (string) {
    string timeStamp = <string>currentTime().time;
    string nonceString = util:uuid();
    string paramStr = "oauth_consumer_key=" + consumerKey + "&oauth_nonce=" + nonceString + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp=" + timeStamp + "&oauth_token=" + accessToken + "&oauth_version=1.0&status=" + uri:encode(tweetMessage);
    string baseString = "POST&" + uri:encode("https://api.twitter.com/1.1/statuses/update.json") + "&" + uri:encode(paramStr);
    string keyStr = uri:encode(consumerSecret) + "&" + uri:encode(accessTokenSecret);
    string signature = util:getHmac(baseString, keyStr, "SHA1");
    string oauthHeader = "OAuth oauth_consumer_key=\"" + consumerKey + "\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\"" + timeStamp + "\",oauth_nonce=\"" + nonceString + "\",oauth_version=\"1.0\",oauth_signature=\"" + uri:encode(signature) + "\",oauth_token=\"" + uri:encode(accessToken) + "\"";
    return oauthHeader.unescape();

}
function main (string[] args) {
    Twitter twitterConnector = create Twitter(args[0], args[1], args[2], args[3]);
    http:Response tweetResponse = twitterConnector.tweet(args[4]);
    json tweetJSONResponse = tweetResponse.getJsonPayload();
    println(jsons:toString(tweetJSONResponse));
}
