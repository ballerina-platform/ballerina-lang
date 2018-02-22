import ballerina.io;
import ballerina.net.http;
import ballerina.net.uri;
import ballerina.security.crypto;
import ballerina.util;

function main (string[] args) {
    endpoint<http:HttpClient> tweeterEP {
        create http:HttpClient("https://api.twitter.com", {});
    }
    endpoint<http:HttpClient> mediumEP {
        create http:HttpClient("https://medium.com", {});
    }
    int argumentLength = lengthof args;
    if (argumentLength < 4) {
        io:println("Incorrect number of arguments");
        io:println("Please specify: consumerKey consumerSecret accessToken accessTokenSecret");
    } else {
        string consumerKey = args[0];
        string consumerSecret = args[1];
        string accessToken = args[2];
        string accessTokenSecret = args[3];
        http:OutRequest request = {};
        http:InResponse mediumResponse = {};
        http:HttpConnectorError err;
        mediumResponse, err = mediumEP.get("/feed/@wso2", request);
        xml feedXML = mediumResponse.getXmlPayload();
        string title = feedXML.selectChildren("channel").selectChildren("item")[1].selectChildren("title").getTextValue();

        string oauthHeader = constructOAuthHeader(consumerKey, consumerSecret, accessToken, accessTokenSecret, title);

        http:OutRequest twitterRequest = {};
        twitterRequest.setHeader("Authorization", oauthHeader);
        string tweetPath = "/1.1/statuses/update.json?status=" + uri:encode(title);
        http:InResponse response = {};
        response, err = tweeterEP.post(tweetPath, twitterRequest);

        int statusCd = response.statusCode;
        if (statusCd == 200) {
            io:println("Successfully tweeted: '" + title + "'");
        } else {
            io:println("Failed to do the tweet: " + statusCd);
        }
    }
}

function constructOAuthHeader (string consumerKey, string consumerSecret, string accessToken, string accessTokenSecret, string tweetMessage) (string) {
    string timeStamp = <string>(currentTime().time/1000);
    string nonceString = util:uuid();
    string paramStr = "oauth_consumer_key=" + consumerKey + "&oauth_nonce=" + nonceString + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp=" + timeStamp + "&oauth_token=" + accessToken + "&oauth_version=1.0&status=" + uri:encode(tweetMessage);
    string baseString = "POST&" + uri:encode("https://api.twitter.com/1.1/statuses/update.json") + "&" + uri:encode(paramStr);
    string keyStr = uri:encode(consumerSecret) + "&" + uri:encode(accessTokenSecret);
    string signature = util:base16ToBase64Encode(crypto:getHmac(baseString, keyStr, crypto:Algorithm.SHA1));
    string oauthHeader = "OAuth oauth_consumer_key=\"" + consumerKey + "\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\"" + timeStamp + "\",oauth_nonce=\"" + nonceString + "\",oauth_version=\"1.0\",oauth_signature=\"" + uri:encode(signature) + "\",oauth_token=\"" + uri:encode(accessToken) + "\"";
    return oauthHeader.unescape();
}